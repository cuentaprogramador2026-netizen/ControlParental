package com.controlparental.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.domain.model.ChildDevice
import com.controlparental.app.domain.model.TimeLimits
import com.controlparental.app.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "control_parental_prefs"
)

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val APP_MODE = stringPreferencesKey(Constants.DS_APP_MODE)
        val MASTER_PIN_HASH = stringPreferencesKey(Constants.DS_MASTER_PIN_HASH)
        val PHONE_NUMBER = stringPreferencesKey(Constants.DS_PHONE_NUMBER)
        val DEVICE_ID = stringPreferencesKey(Constants.DS_DEVICE_ID)
        val FCM_TOKEN = stringPreferencesKey(Constants.DS_FCM_TOKEN)
        val PARENT_DEVICE_ID = stringPreferencesKey(Constants.DS_PARENT_DEVICE_ID)
        val CHILD_DEVICES = stringPreferencesKey(Constants.DS_CHILD_DEVICES)
        val RESTRICTED_PACKAGES = stringPreferencesKey(Constants.DS_RESTRICTED_PACKAGES)
        val DAILY_LIMIT_MINUTES = intPreferencesKey(Constants.DS_DAILY_LIMIT_MINUTES)
        val WEEKLY_LIMIT_MINUTES = intPreferencesKey(Constants.DS_WEEKLY_LIMIT_MINUTES)
        val TIME_CONSUMED_TODAY = intPreferencesKey(Constants.DS_TIME_CONSUMED_TODAY)
        val TIME_CONSUMED_THIS_WEEK = intPreferencesKey(Constants.DS_TIME_CONSUMED_THIS_WEEK)
        val LAST_RESET_DATE = stringPreferencesKey(Constants.DS_LAST_RESET_DATE)
        val LAST_RESET_WEEK = stringPreferencesKey(Constants.DS_LAST_RESET_WEEK)
        val MONITORING_ENABLED = booleanPreferencesKey(Constants.DS_MONITORING_ENABLED)
        val EXTRA_TIME_GRANTED = intPreferencesKey(Constants.DS_EXTRA_TIME_GRANTED)
        val EXTRA_TIME_EXPIRY = longPreferencesKey(Constants.DS_EXTRA_TIME_EXPIRY)
    }

    // ── App Mode ──

    val appMode: Flow<AppMode?> = context.dataStore.data.map { prefs ->
        prefs[Keys.APP_MODE]?.let { str ->
            try { AppMode.valueOf(str) } catch (_: Exception) { null }
        }
    }

    suspend fun setAppMode(mode: AppMode) {
        context.dataStore.edit { it[Keys.APP_MODE] = mode.name }
    }

    // ── Master PIN (hashed) ──

    val masterPinHash: Flow<String?> = context.dataStore.data.map { it[Keys.MASTER_PIN_HASH] }

    suspend fun setMasterPinHash(hash: String) {
        context.dataStore.edit { it[Keys.MASTER_PIN_HASH] = hash }
    }

    suspend fun isPinConfigured(): Boolean =
        context.dataStore.data.first()[Keys.MASTER_PIN_HASH] != null

    // ── Phone Number ──

    suspend fun setPhoneNumber(phone: String) {
        context.dataStore.edit { it[Keys.PHONE_NUMBER] = phone }
    }

    val phoneNumber: Flow<String?> = context.dataStore.data.map { it[Keys.PHONE_NUMBER] }

    // ── Device ID ──

    suspend fun setDeviceId(id: String) {
        context.dataStore.edit { it[Keys.DEVICE_ID] = id }
    }

    val deviceId: Flow<String> = context.dataStore.data.map {
        it[Keys.DEVICE_ID] ?: ""
    }

    // ── FCM Token ──

    suspend fun setFcmToken(token: String) {
        context.dataStore.edit { it[Keys.FCM_TOKEN] = token }
    }

    val fcmToken: Flow<String?> = context.dataStore.data.map { it[Keys.FCM_TOKEN] }

    // ── Parent Device ID ──

    suspend fun setParentDeviceId(id: String) {
        context.dataStore.edit { it[Keys.PARENT_DEVICE_ID] = id }
    }

    val parentDeviceId: Flow<String?> = context.dataStore.data.map { it[Keys.PARENT_DEVICE_ID] }

    // ── Child Devices (solo en modo PARENT) ──

    private fun childDevicesFromJson(json: String): List<ChildDevice> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ChildDevice(
                    deviceId = obj.optString("deviceId", ""),
                    alias = obj.optString("alias", ""),
                    phoneNumber = obj.optString("phoneNumber", ""),
                    fcmToken = obj.optString("fcmToken", ""),
                    lastSeen = obj.optLong("lastSeen", System.currentTimeMillis()),
                    linkedAt = obj.optLong("linkedAt", System.currentTimeMillis())
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun childDevicesToJson(devices: List<ChildDevice>): String {
        val arr = JSONArray()
        devices.forEach { d ->
            arr.put(JSONObject().apply {
                put("deviceId", d.deviceId)
                put("alias", d.alias)
                put("phoneNumber", d.phoneNumber)
                put("fcmToken", d.fcmToken)
                put("lastSeen", d.lastSeen)
                put("linkedAt", d.linkedAt)
            })
        }
        return arr.toString()
    }

    suspend fun addChildDevice(device: ChildDevice) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CHILD_DEVICES]?.let {
                try { childDevicesFromJson(it) } catch (_: Exception) { emptyList() }
            } ?: emptyList()
            val updated = current.toMutableList().apply {
                removeAll { it.deviceId == device.deviceId }
                add(device)
            }
            prefs[Keys.CHILD_DEVICES] = childDevicesToJson(updated)
        }
    }

    suspend fun removeChildDevice(deviceId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CHILD_DEVICES]?.let {
                try { childDevicesFromJson(it) } catch (_: Exception) { emptyList() }
            } ?: emptyList()
            prefs[Keys.CHILD_DEVICES] = childDevicesToJson(current.filter { it.deviceId != deviceId })
        }
    }

    val childDevices: Flow<List<ChildDevice>> = context.dataStore.data.map { prefs ->
        prefs[Keys.CHILD_DEVICES]?.let {
            try { childDevicesFromJson(it) } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    // ── Restricted Packages ──

    suspend fun setRestrictedPackages(packages: Set<String>) {
        context.dataStore.edit {
            val arr = JSONArray(packages.toList())
            it[Keys.RESTRICTED_PACKAGES] = arr.toString()
        }
    }

    val restrictedPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.RESTRICTED_PACKAGES]?.let {
            try {
                val arr = JSONArray(it)
                (0 until arr.length()).map { arr.getString(it) }.toSet()
            } catch (_: Exception) { emptySet() }
        } ?: emptySet()
    }

    // ── Time Limits ──

    suspend fun setTimeLimits(limits: TimeLimits) {
        context.dataStore.edit {
            it[Keys.DAILY_LIMIT_MINUTES] = limits.dailyMinutes
            it[Keys.WEEKLY_LIMIT_MINUTES] = limits.weeklyMinutes
        }
    }

    val timeLimits: Flow<TimeLimits> = context.dataStore.data.map { prefs ->
        TimeLimits(
            dailyMinutes = prefs[Keys.DAILY_LIMIT_MINUTES] ?: 120,
            weeklyMinutes = prefs[Keys.WEEKLY_LIMIT_MINUTES] ?: 840
        )
    }

    // ── Time Consumed (with auto-reset) ──

    suspend fun getTimeConsumedToday(): Int {
        ensureDailyReset()
        return context.dataStore.data.first()[Keys.TIME_CONSUMED_TODAY] ?: 0
    }

    suspend fun getTimeConsumedThisWeek(): Int {
        ensureWeeklyReset()
        return context.dataStore.data.first()[Keys.TIME_CONSUMED_THIS_WEEK] ?: 0
    }

    suspend fun addTimeConsumed(minutes: Int) {
        ensureDailyReset()
        ensureWeeklyReset()
        context.dataStore.edit { prefs ->
            prefs[Keys.TIME_CONSUMED_TODAY] = (prefs[Keys.TIME_CONSUMED_TODAY] ?: 0) + minutes
            prefs[Keys.TIME_CONSUMED_THIS_WEEK] = (prefs[Keys.TIME_CONSUMED_THIS_WEEK] ?: 0) + minutes
        }
    }

    suspend fun addExtraTime(minutes: Int) {
        context.dataStore.edit { prefs ->
            val currentExtra = prefs[Keys.EXTRA_TIME_GRANTED] ?: 0
            val currentExpiry = prefs[Keys.EXTRA_TIME_EXPIRY] ?: 0L
            val now = System.currentTimeMillis()
            val remaining = if (currentExpiry > now) currentExtra else 0
            prefs[Keys.EXTRA_TIME_GRANTED] = remaining + minutes
            prefs[Keys.EXTRA_TIME_EXPIRY] = now + (minutes * 60_000L)
        }
    }

    suspend fun getExtraTimeRemaining(): Int {
        val prefs = context.dataStore.data.first()
        val expiry = prefs[Keys.EXTRA_TIME_EXPIRY] ?: 0L
        if (expiry < System.currentTimeMillis()) return 0
        return prefs[Keys.EXTRA_TIME_GRANTED] ?: 0
    }

    suspend fun consumeExtraTime() {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.EXTRA_TIME_GRANTED] ?: return@edit
            if (current > 0) {
                prefs[Keys.EXTRA_TIME_GRANTED] = current - 1
            }
        }
    }

    private suspend fun ensureDailyReset() {
        val today = LocalDate.now().toString()
        context.dataStore.edit { prefs ->
            val lastReset = prefs[Keys.LAST_RESET_DATE]
            if (lastReset != today) {
                prefs[Keys.TIME_CONSUMED_TODAY] = 0
                prefs[Keys.LAST_RESET_DATE] = today
            }
        }
    }

    private suspend fun ensureWeeklyReset() {
        val today = LocalDate.now()
        val currentWeek = "${today.year}-W${"%02d".format(today.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR))}"
        context.dataStore.edit { prefs ->
            val lastWeek = prefs[Keys.LAST_RESET_WEEK]
            if (lastWeek != currentWeek) {
                prefs[Keys.TIME_CONSUMED_THIS_WEEK] = 0
                prefs[Keys.LAST_RESET_WEEK] = currentWeek
            }
        }
    }

    // ── Monitoring Enabled ──

    val monitoringEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.MONITORING_ENABLED] ?: false }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MONITORING_ENABLED] = enabled }
    }

    // ── Reset all (for unlink/logout) ──

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
