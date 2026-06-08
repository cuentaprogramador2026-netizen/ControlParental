package com.controlparental.app.service

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.controlparental.app.R
import com.controlparental.app.util.Constants

class AdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Log.d(Constants.TAG, "Device admin enabled")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        // Mensaje que se muestra al usuario cuando intenta desactivar el admin
        return context.getString(R.string.permission_admin_desc)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Log.w(Constants.TAG, "Device admin disabled")
    }

    override fun onPasswordChanged(context: Context, intent: Intent) {
        Log.d(Constants.TAG, "Password changed")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        Log.d(Constants.TAG, "Lock task mode entering for: $pkg")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        Log.d(Constants.TAG, "Lock task mode exiting")
    }

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, AdminReceiver::class.java)
        }

        fun isAdminActive(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return dpm.isAdminActive(getComponentName(context))
        }

        fun requestAdmin(context: Context) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(R.string.permission_admin_desc)
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        fun removeAdmin(context: Context) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            try {
                dpm.removeActiveAdmin(getComponentName(context))
            } catch (e: SecurityException) {
                Log.e(Constants.TAG, "Cannot remove admin", e)
            }
        }
    }
}
