package com.controlparental.app.util

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Helper centralizado para la gestión de permisos.
 * Todos los permisos críticos de la aplicación se gestionan desde aquí.
 */
object PermissionHelper {

    data class PermissionState(
        val usageStats: Boolean = false,
        val overlay: Boolean = false,
        val notifications: Boolean = false,
        val batteryOptimization: Boolean = false,
        val deviceAdmin: Boolean = false
    ) {
        val allGranted: Boolean get() = usageStats && overlay && notifications && batteryOptimization
    }

    fun getPermissionState(context: Context): PermissionState {
        return PermissionState(
            usageStats = hasUsageStatsPermission(context),
            overlay = hasOverlayPermission(context),
            notifications = hasNotificationPermission(context),
            batteryOptimization = hasBatteryOptimizationPermission(context),
            deviceAdmin = com.controlparental.app.service.AdminReceiver.isAdminActive(context)
        )
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return true
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun hasBatteryOptimizationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    // ─── Intents para solicitar permisos ───

    fun getUsageStatsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getOverlayIntent(context: Context): Intent {
        return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getBatteryOptimizationIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
