package com.controlparental.app.domain.usecase

import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.domain.repository.AppRepository
import javax.inject.Inject

class RegisterDeviceUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        mode: AppMode,
        pin: String,
        phoneNumber: String = "",
        parentDeviceId: String? = null
    ): Result<Unit> {
        return try {
            repository.setAppMode(mode)
            repository.setMasterPin(pin)
            if (phoneNumber.isNotBlank()) repository.setPhoneNumber(phoneNumber)
            repository.registerDeviceInFirestore()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
