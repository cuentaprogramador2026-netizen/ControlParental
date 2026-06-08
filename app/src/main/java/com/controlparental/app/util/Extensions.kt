package com.controlparental.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

fun Context.isUsageStatsPermissionGranted(): Boolean {
    return PermissionHelper.hasUsageStatsPermission(this)
}

fun Context.isOverlayPermissionGranted(): Boolean {
    return PermissionHelper.hasOverlayPermission(this)
}

fun Context.isBatteryOptimizationIgnored(): Boolean {
    return PermissionHelper.hasBatteryOptimizationPermission(this)
}
