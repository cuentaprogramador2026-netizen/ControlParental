package com.controlparental.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.controlparental.app.data.local.DataStoreManager
import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.domain.repository.AppRepository
import com.controlparental.app.service.AdminReceiver
import com.controlparental.app.service.MonitoringService
import com.controlparental.app.ui.navigation.AppNavGraph
import com.controlparental.app.ui.navigation.Routes
import com.controlparental.app.ui.theme.ControlParentalTheme
import com.controlparental.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var dataStoreManager: DataStoreManager
    @Inject lateinit var appRepository: AppRepository

    private var isOnboarded by mutableStateOf(false)
    private var appMode by mutableStateOf<AppMode?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Permiso necesario para el funcionamiento", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val mode = dataStoreManager.appMode.first()
            val hasPin = dataStoreManager.isPinConfigured()
            isOnboarded = mode != null && hasPin
            appMode = mode

            setContent {
                ControlParentalTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val startDest = if (isOnboarded) {
                            when (appMode) {
                                AppMode.PARENT -> Routes.PARENT_HOME
                                AppMode.CHILD -> Routes.CHILD_HOME
                                else -> Routes.ONBOARDING
                            }
                        } else {
                            Routes.ONBOARDING
                        }

                        if (isOnboarded && appMode == AppMode.CHILD) {
                            startMonitoringService()
                        }

                        AppNavGraph(
                            navController = navController,
                            repository = appRepository,
                            startDestination = startDest
                        )
                    }
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Constants.ACTION_OPEN_REQUESTS -> {
                // Navegar a solicitudes
            }
            Constants.ACTION_UNLOCK_APP -> {
                // Desbloquear app
            }
        }
    }

    private fun startMonitoringService() {
        if (hasUsageStatsPermission() && hasOverlayPermission()) {
            MonitoringService.start(this)
        }
    }

    // ─── Permisos ───

    fun requestUsageStatsPermission() {
        startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun hasUsageStatsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return true
        try {
            val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), packageName
            )
            return mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            return false
        }
    }

    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun requestBatteryOptimizationPermission() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    fun requestDeviceAdmin() {
        if (!AdminReceiver.isAdminActive(this)) {
            AdminReceiver.requestAdmin(this)
        }
    }
}
