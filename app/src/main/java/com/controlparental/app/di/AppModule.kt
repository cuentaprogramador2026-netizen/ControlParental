package com.controlparental.app.di

import android.app.Application
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.work.WorkManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.controlparental.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeviceId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DevicesCollection

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RequestsCollection

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUsageStatsManager(app: Application): UsageStatsManager =
        app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    @Provides
    @Singleton
    fun provideNotificationManager(app: Application): NotificationManager =
        app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideDevicePolicyManager(app: Application): DevicePolicyManager =
        app.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    @Provides
    @Singleton
    fun providePowerManager(app: Application): PowerManager =
        app.getSystemService(Context.POWER_SERVICE) as PowerManager

    @Provides
    @Singleton
    fun providePackageManager(app: Application): PackageManager =
        app.packageManager

    @Provides
    @Singleton
    fun provideWorkManager(app: Application): WorkManager =
        WorkManager.getInstance(app)

    @Provides
    @Singleton
    @DeviceId
    suspend fun provideDeviceId(app: Application): String {
        return try {
            val id = FirebaseInstallations.getInstance().id.await()
            if (id.isNotEmpty()) id
            else Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (_: Exception) {
            Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    @DevicesCollection
    fun provideDevicesCollection(firestore: FirebaseFirestore): CollectionReference =
        firestore.collection(Constants.FS_DEVICES)

    @Provides
    @Singleton
    @RequestsCollection
    fun provideRequestsCollection(firestore: FirebaseFirestore): CollectionReference =
        firestore.collection(Constants.FS_REQUESTS)
}
