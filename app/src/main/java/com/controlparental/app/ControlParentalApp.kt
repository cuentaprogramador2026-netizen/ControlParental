package com.controlparental.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import com.controlparental.app.util.Constants
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ControlParentalApp : Application() {

    @Inject lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                Constants.NOTIF_CHANNEL_SERVICE,
                getString(R.string.channel_service),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_service_desc)
                setShowBadge(false)
                lockscreenVisibility = NotificationManager.VISIBILITY_PRIVATE
            },
            NotificationChannel(
                Constants.NOTIF_CHANNEL_ALERTS,
                getString(R.string.channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_alerts_desc)
                enableVibration(true)
                lockscreenVisibility = NotificationManager.VISIBILITY_PUBLIC
            },
            NotificationChannel(
                Constants.NOTIF_CHANNEL_REQUESTS,
                getString(R.string.channel_requests),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_requests_desc)
                enableVibration(true)
                lockscreenVisibility = NotificationManager.VISIBILITY_PUBLIC
            }
        )
        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    companion object {
        lateinit var instance: ControlParentalApp
            private set
    }
}
