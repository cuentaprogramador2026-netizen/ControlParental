package com.controlparental.app.domain.repository

import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.domain.model.ChildDevice
import com.controlparental.app.domain.model.TimeLimits
import com.controlparental.app.domain.model.TimeRequest
import com.controlparental.app.domain.model.RequestStatus
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    // ── App Mode ──
    val appMode: Flow<AppMode?>
    suspend fun setAppMode(mode: AppMode)

    // ── PIN ──
    suspend fun setMasterPin(pin: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun isPinConfigured(): Boolean

    // ── Device ──
    suspend fun getDeviceId(): String
    suspend fun registerDeviceInFirestore()
    suspend fun updateFcmToken(token: String)

    // ── Child Devices (parent mode) ──
    val childDevices: Flow<List<ChildDevice>>
    suspend fun linkChildDevice(childId: String, alias: String = "")
    suspend fun unlinkChildDevice(deviceId: String)
    suspend fun fetchChildDevicesFromFirestore()

    // ── Restricted Apps ──
    val restrictedPackages: Flow<Set<String>>
    suspend fun setRestrictedPackages(packages: Set<String>)

    // ── Time Limits ──
    val timeLimits: Flow<TimeLimits>
    suspend fun setTimeLimits(limits: TimeLimits)
    suspend fun getTimeConsumedToday(): Int
    suspend fun getTimeConsumedThisWeek(): Int
    suspend fun hasExceededLimit(): Boolean

    // ── Extra Time ──
    suspend fun requestExtraTime(minutes: Int, packageName: String = ""): String
    suspend fun approveTimeRequest(requestId: String)
    suspend fun rejectTimeRequest(requestId: String)
    suspend fun getExtraTimeRemaining(): Int
    suspend fun consumeExtraTime()

    // ── Real-time requests ──
    fun observeRequests(): Flow<List<TimeRequest>>
    fun observeChildRequests(): Flow<List<TimeRequest>>

    // ── Monitoring ──
    val monitoringEnabled: Flow<Boolean>
    suspend fun setMonitoringEnabled(enabled: Boolean)
    suspend fun getForegroundApp(): String?
    suspend fun shouldLockApp(foregroundPackage: String): Boolean

    // ── Phone ──
    suspend fun setPhoneNumber(phone: String)

    // ── Reset ──
    suspend fun clearAllData()
}
