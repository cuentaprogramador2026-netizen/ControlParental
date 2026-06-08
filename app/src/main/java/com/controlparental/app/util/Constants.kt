package com.controlparental.app.util

object Constants {
    const val TAG = "ControlParental"
    const val PACKAGE_SELF = "com.controlparental.app"
    const val PACKAGE_SETTINGS = "com.android.settings"

    // DataStore keys
    const val DS_APP_MODE = "app_mode"
    const val DS_MASTER_PIN_HASH = "master_pin_hash"
    const val DS_PHONE_NUMBER = "phone_number"
    const val DS_DEVICE_ID = "device_id"
    const val DS_FCM_TOKEN = "fcm_token"
    const val DS_PARENT_DEVICE_ID = "parent_device_id"
    const val DS_CHILD_DEVICES = "child_devices"
    const val DS_RESTRICTED_PACKAGES = "restricted_packages"
    const val DS_DAILY_LIMIT_MINUTES = "daily_limit_minutes"
    const val DS_WEEKLY_LIMIT_MINUTES = "weekly_limit_minutes"
    const val DS_TIME_CONSUMED_TODAY = "time_consumed_today"
    const val DS_TIME_CONSUMED_THIS_WEEK = "time_consumed_this_week"
    const val DS_LAST_RESET_DATE = "last_reset_date"
    const val DS_LAST_RESET_WEEK = "last_reset_week"
    const val DS_MONITORING_ENABLED = "monitoring_enabled"
    const val DS_EXTRA_TIME_GRANTED = "extra_time_granted"
    const val DS_EXTRA_TIME_EXPIRY = "extra_time_expiry"

    // Firestore collections
    const val FS_DEVICES = "devices"
    const val FS_REQUESTS = "requests"
    const val FS_CHILD_DEVICES = "childDevices"

    // Firestore document fields
    const val FS_FIELD_DEVICE_ID = "deviceId"
    const val FS_FIELD_MODE = "mode"
    const val FS_FIELD_PHONE = "phoneNumber"
    const val FS_FIELD_FCM_TOKEN = "fcmToken"
    const val FS_FIELD_PARENT_ID = "parentDeviceId"
    const val FS_FIELD_CHILD_ID = "childDeviceId"
    const val FS_FIELD_RESTRICTED = "restrictedPackages"
    const val FS_FIELD_DAILY_LIMIT = "dailyLimitMinutes"
    const val FS_FIELD_WEEKLY_LIMIT = "weeklyLimitMinutes"
    const val FS_FIELD_STATUS = "status"
    const val FS_FIELD_REQUESTED_MINUTES = "requestedMinutes"
    const val FS_FIELD_TIMESTAMP = "timestamp"
    const val FS_FIELD_PACKAGE_NAME = "packageName"
    const val FS_FIELD_REASON = "reason"
    const val FS_FIELD_CREATED_AT = "createdAt"

    // FCM topics
    const val FCM_TOPIC_PREFIX = "parent_"

    // Intent actions
    const val ACTION_LOCK_APP = "com.controlparental.app.ACTION_LOCK_APP"
    const val ACTION_UNLOCK_APP = "com.controlparental.app.ACTION_UNLOCK_APP"
    const val ACTION_OPEN_REQUESTS = "com.controlparental.app.ACTION_OPEN_REQUESTS"
    const val ACTION_CHECK_FOREGROUND = "com.controlparental.app.ACTION_CHECK_FOREGROUND"
    const val ACTION_APPROVE_REQUEST = "com.controlparental.app.ACTION_APPROVE_REQUEST"
    const val EXTRA_REQUEST_ID = "extra_request_id"
    const val EXTRA_PACKAGE_NAME = "extra_package_name"
    const val EXTRA_MINUTES = "extra_minutes"

    // Service
    const val NOTIF_ID_MONITORING = 1001
    const val NOTIF_CHANNEL_SERVICE = "parental_service"
    const val NOTIF_CHANNEL_ALERTS = "parental_alerts"
    const val NOTIF_CHANNEL_REQUESTS = "parental_requests"

    // Monitoring interval
    const val MONITOR_INTERVAL_MS = 1200L
    const val MONITOR_INTERVAL_FLEX_MS = 300L
    const val ALARM_INTERVAL_MS = 30_000L
    const val FORCE_KILL_CHECK_MS = 10_000L

    // Overlay lock flags
    const val LOCK_FLAG_DISMISS_KEYGUARD = 0x00000020
}
