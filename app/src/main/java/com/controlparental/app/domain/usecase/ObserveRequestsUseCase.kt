package com.controlparental.app.domain.usecase

import com.controlparental.app.domain.model.TimeRequest
import com.controlparental.app.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveParentRequestsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<List<TimeRequest>> = repository.observeRequests()
}

class ObserveChildRequestsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<List<TimeRequest>> = repository.observeChildRequests()
}
