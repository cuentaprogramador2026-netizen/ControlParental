package com.controlparental.app.domain.usecase

import com.controlparental.app.domain.repository.AppRepository
import javax.inject.Inject

class RequestExtraTimeUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(minutes: Int, packageName: String = ""): Result<String> {
        if (minutes <= 0) return Result.failure(IllegalArgumentException("Minutes must be positive"))
        if (minutes > 480) return Result.failure(IllegalArgumentException("Max 8 hours"))
        return try {
            val requestId = repository.requestExtraTime(minutes, packageName)
            Result.success(requestId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
