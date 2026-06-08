package com.controlparental.app.domain.model

data class TimeLimits(
    val dailyMinutes: Int = 120,
    val weeklyMinutes: Int = 840
)
