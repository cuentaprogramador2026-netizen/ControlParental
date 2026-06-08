package com.controlparental.app.data.remote

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.controlparental.app.MainActivity
import com.controlparental.app.data.local.DataStoreManager
import com.controlparental.app.util.Constants
import com.controlparental.app.domain.model.AppMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var dataStoreManager: DataStoreManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.setFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(Constants.TAG, "FCM: ${message.data}")

        when (message.data["type"]) {
            "new_request" -> {
                showNotification(
                    title = "Nueva solicitud de tiempo",
                    body = message.data["childName"] ?: "Un hijo solicita más tiempo",
                    action = Constants.ACTION_OPEN_REQUESTS
                )
            }
            "request_approved" -> {
                val minutes = message.data["minutes"]?.toIntOrNull() ?: 0
                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreManager.addExtraTime(minutes)
                }
                showNotification(
                    title = "Solicitud aprobada",
                    body = "Te han concedido $minutes minutos adicionales",
                    action = Constants.ACTION_UNLOCK_APP
                )
            }
            "request_rejected" -> {
                showNotification(
                    title = "Solicitud rechazada",
                    body = "Tu solicitud de tiempo extra ha sido rechazada",
                    action = null
                )
            }
            "time_limit_updated" -> {
                showNotification(
                    title = "Límites actualizados",
                    body = "El padre ha actualizado los límites de tiempo",
                    action = null
                )
            }
        }
    }

    private fun showNotification(title: String, body: String, action: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action?.let { putExtra("action", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = android.app.Notification.Builder(this, Constants.NOTIF_CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
