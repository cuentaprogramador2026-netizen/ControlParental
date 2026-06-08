package com.controlparental.app.domain.usecase

import com.controlparental.app.domain.repository.AppRepository
import javax.inject.Inject

class ApproveTimeRequestUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> {
        return try {
            repository.approveTimeRequest(requestId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
