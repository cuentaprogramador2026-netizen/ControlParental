package com.controlparental.app.domain.model

data class ChildDevice(
    val deviceId: String,
    val alias: String = "",
    val phoneNumber: String = "",
    val fcmToken: String = "",
    val lastSeen: Long = System.currentTimeMillis(),
    val linkedAt: Long = System.currentTimeMillis()
)
