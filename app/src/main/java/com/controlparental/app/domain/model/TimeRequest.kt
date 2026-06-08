package com.controlparental.app.domain.model

data class TimeRequest(
    val id: String = "",
    val childDeviceId: String,
    val parentDeviceId: String,
    val requestedMinutes: Int,
    val status: RequestStatus = RequestStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String = "",
    val reason: String = "",
    val respondedAt: Long = 0L,
    val childAlias: String = ""
)

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
