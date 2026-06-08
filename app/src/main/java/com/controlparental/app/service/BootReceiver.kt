package com.controlparental.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.controlparental.app.data.local.DataStoreManager
import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var dataStoreManager: DataStoreManager

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(Constants.TAG, "BootReceiver: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val mode = dataStoreManager.appMode.first()
                    val monitoringEnabled = dataStoreManager.monitoringEnabled.first()

                    if (mode == AppMode.CHILD && monitoringEnabled) {
                        // Pequeño retraso para asegurar que el sistema termino de cargar
                        kotlinx.coroutines.delay(5000)
                        MonitoringService.start(context)
                        Log.d(Constants.TAG, "Service auto-started after boot")
                    }
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "BootReceiver error", e)
                }
            }
        }
    }
}
