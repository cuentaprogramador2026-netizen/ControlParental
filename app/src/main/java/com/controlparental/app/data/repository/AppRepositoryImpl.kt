package com.controlparental.app.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.controlparental.app.data.local.DataStoreManager
import com.controlparental.app.data.remote.FirebaseRepository
import com.controlparental.app.di.DeviceId
import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.domain.model.ChildDevice
import com.controlparental.app.domain.model.RequestStatus
import com.controlparental.app.domain.model.TimeLimits
import com.controlparental.app.domain.model.TimeRequest
import com.controlparental.app.domain.repository.AppRepository
import com.controlparental.app.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val dataStore: DataStoreManager,
    private val firebaseRepository: FirebaseRepository,
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
    @DeviceId private val deviceId: String,
    @ApplicationContext private val context: Context
) : AppRepository {

    // ── App Mode ──

    override val appMode: Flow<AppMode?> = dataStore.appMode

    override suspend fun setAppMode(mode: AppMode) = dataStore.setAppMode(mode)

    // ── PIN ──

    override suspend fun setMasterPin(pin: String) {
        val hash = hashPin(pin)
        dataStore.setMasterPinHash(hash)
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val storedHash = dataStore.masterPinHash.first() ?: return false
        return hashPin(pin) == storedHash
    }

    override suspend fun isPinConfigured(): Boolean = dataStore.isPinConfigured()

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    // ── Device ──

    override suspend fun getDeviceId(): String = deviceId

    override suspend fun registerDeviceInFirestore() {
        val mode = dataStore.appMode.first() ?: return
        val phone = dataStore.phoneNumber.first() ?: ""
        val token = dataStore.fcmToken.first() ?: ""
        firebaseRepository.registerDevice(mode, phone, token)

        if (mode == AppMode.PARENT) {
            dataStore.setParentDeviceId(deviceId)
        }
    }

    override suspend fun updateFcmToken(token: String) {
        dataStore.setFcmToken(token)
        firebaseRepository.updateFcmToken(token)
    }

    // ── Child Devices ──

    override val childDevices: Flow<List<ChildDevice>> = dataStore.childDevices

    override suspend fun linkChildDevice(childId: String, alias: String) {
        firebaseRepository.linkChildToParent(childId)
        dataStore.addChildDevice(ChildDevice(deviceId = childId, alias = alias))
    }

    override suspend fun unlinkChildDevice(deviceId: String) {
        firebaseRepository.unlinkChild(deviceId)
        dataStore.removeChildDevice(deviceId)
    }

    override suspend fun fetchChildDevicesFromFirestore() {
        val children = firebaseRepository.getChildDevicesInfo()
        children.forEach { dataStore.addChildDevice(it) }
    }

    // ── Restricted Apps ──

    override val restrictedPackages: Flow<Set<String>> = dataStore.restrictedPackages

    override suspend fun setRestrictedPackages(packages: Set<String>) {
        dataStore.setRestrictedPackages(packages)
        firebaseRepository.updateDeviceInfo(restrictedPackages = packages)
    }

    // ── Time Limits ──

    override val timeLimits: Flow<TimeLimits> = dataStore.timeLimits

    override suspend fun setTimeLimits(limits: TimeLimits) {
        dataStore.setTimeLimits(limits)
        firebaseRepository.updateDeviceInfo(timeLimits = limits)
    }

    override suspend fun getTimeConsumedToday(): Int = dataStore.getTimeConsumedToday()

    override suspend fun getTimeConsumedThisWeek(): Int = dataStore.getTimeConsumedThisWeek()

    override suspend fun hasExceededLimit(): Boolean {
        val limits = dataStore.timeLimits.first()
        val consumedToday = dataStore.getTimeConsumedToday()
        val consumedThisWeek = dataStore.getTimeConsumedThisWeek()
        return consumedToday >= limits.dailyMinutes || consumedThisWeek >= limits.weeklyMinutes
    }

    // ── Extra Time ──

    override suspend fun requestExtraTime(minutes: Int, packageName: String): String {
        val parentId = dataStore.parentDeviceId.first()
            ?: firebaseRepository.getParentDeviceId()
            ?: throw IllegalStateException("No parent device linked")

        return firebaseRepository.createRequest(minutes, packageName, parentId)
    }

    override suspend fun approveTimeRequest(requestId: String) {
        firebaseRepository.updateRequestStatus(requestId, RequestStatus.APPROVED)
    }

    override suspend fun rejectTimeRequest(requestId: String) {
        firebaseRepository.updateRequestStatus(requestId, RequestStatus.REJECTED)
    }

    override suspend fun getExtraTimeRemaining(): Int = dataStore.getExtraTimeRemaining()

    override suspend fun consumeExtraTime() = dataStore.consumeExtraTime()

    // ── Real-time requests ──

    override fun observeRequests(): Flow<List<TimeRequest>> {
        return firebaseRepository.observeRequestsForParent()
    }

    override fun observeChildRequests(): Flow<List<TimeRequest>> {
        return firebaseRepository.observeRequestsForChild()
    }

    // ── Monitoring ──

    override val monitoringEnabled: Flow<Boolean> = dataStore.monitoringEnabled

    override suspend fun setMonitoringEnabled(enabled: Boolean) {
        dataStore.setMonitoringEnabled(enabled)
    }

    override suspend fun getForegroundApp(): String? {
        val currentTime = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(currentTime - 2000, currentTime)
        var currentApp: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentApp = event.packageName
            }
        }
        return currentApp
    }

    override suspend fun shouldLockApp(foregroundPackage: String): Boolean {
        if (!dataStore.monitoringEnabled.first()) return false
        val mode = dataStore.appMode.first()
        if (mode != AppMode.CHILD) return false

        // Never lock system settings or launcher
        if (foregroundPackage == Constants.PACKAGE_SELF) return false
        if (foregroundPackage == Constants.PACKAGE_SETTINGS) return true // Block settings access

        // Check if it's the launcher
        if (isLauncherPackage(foregroundPackage)) return false

        val restricted = dataStore.restrictedPackages.first()
        if (foregroundPackage !in restricted) return false

        val exceeded = hasExceededLimit()
        val extraTime = dataStore.getExtraTimeRemaining()

        return exceeded && extraTime <= 0
    }

    private fun isLauncherPackage(packageName: String): Boolean {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    // ── Phone ──

    override suspend fun setPhoneNumber(phone: String) = dataStore.setPhoneNumber(phone)

    // ── Reset ──

    override suspend fun clearAllData() {
        dataStore.clearAll()
    }
}
