package com.controlparental.app.service

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.controlparental.app.R
import com.controlparental.app.domain.repository.AppRepository
import com.controlparental.app.ui.lock.LockOverlayActivity
import com.controlparental.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringService : Service() {

    @Inject lateinit var appRepository: AppRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(Constants.TAG, "MonitoringService started")

        startForeground(Constants.NOTIF_ID_MONITORING, createNotification())
        startMonitoring()

        // AlarmManager para auto-reinicio periódico
        scheduleAlarmRestart()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
        Log.d(Constants.TAG, "MonitoringService destroyed")
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    val mode = appRepository.appMode.first()
                    if (mode != com.controlparental.app.domain.model.AppMode.CHILD) {
                        delay(5000)
                        continue
                    }

                    val foregroundApp = appRepository.getForegroundApp()
                    if (foregroundApp != null && appRepository.shouldLockApp(foregroundApp)) {
                        Log.d(Constants.TAG, "LOCK triggered for: $foregroundApp")
                        launchLockOverlay(foregroundApp)
                    }
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Monitoring error", e)
                }

                // Intervalo variado 1-1.5s para evitar patrones detectables
                val baseDelay = Constants.MONITOR_INTERVAL_MS
                val flexDelay = (Math.random() * Constants.MONITOR_INTERVAL_FLEX_MS).toLong()
                delay(baseDelay + flexDelay)
            }
        }
    }

    private fun launchLockOverlay(packageName: String) {
        val intent = Intent(this, LockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(Constants.EXTRA_PACKAGE_NAME, packageName)
        }
        startActivity(intent)
    }

    private fun createNotification(): Notification {
        val openIntent = Intent(this, MonitoringService::class.java).apply {
            action = Constants.ACTION_CHECK_FOREGROUND
        }
        val openPendingIntent = PendingIntent.getService(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.NOTIF_CHANNEL_SERVICE)
            .setContentTitle(getString(R.string.notification_monitoring_title))
            .setContentText(getString(R.string.notification_monitoring_text))
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openPendingIntent)
            .build()
    }

    private fun scheduleAlarmRestart() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MonitoringService::class.java)
        val pendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + Constants.ALARM_INTERVAL_MS,
            Constants.ALARM_INTERVAL_MS,
            pendingIntent
        )
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "${Constants.TAG}:MonitoringWakeLock"
        ).apply {
            acquire(30 * 60 * 1000L) // 30 min max
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MonitoringService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MonitoringService::class.java))
        }
    }
}
