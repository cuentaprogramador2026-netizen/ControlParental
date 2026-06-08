package com.controlparental.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.domain.usecase.RegisterDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val selectedMode: AppMode? = null,
    val pin: String = "",
    val pinConfirm: String = "",
    val pinError: String? = null,
    val phoneNumber: String = "",
    val parentDeviceId: String = "",
    val isLoading: Boolean = false,
    val registrationComplete: Boolean = false,
    val error: String? = null
)

enum class OnboardingStep {
    WELCOME,
    CHOOSE_MODE,
    SETUP_PIN,
    SETUP_PHONE,
    LINK_PARENT,
    PERMISSIONS,
    COMPLETE
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val registerDeviceUseCase: RegisterDeviceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectMode(mode: AppMode) {
        _uiState.value = _uiState.value.copy(
            selectedMode = mode,
            step = OnboardingStep.SETUP_PIN,
            parentDeviceId = ""
        )
    }

    fun updatePin(pin: String) {
        _uiState.value = _uiState.value.copy(pin = pin, pinError = null)
    }

    fun updatePinConfirm(confirm: String) {
        _uiState.value = _uiState.value.copy(pinConfirm = confirm, pinError = null)
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone)
    }

    fun updateParentDeviceId(id: String) {
        _uiState.value = _uiState.value.copy(parentDeviceId = id)
    }

    fun goToNextStep() {
        val current = _uiState.value
        when (current.step) {
            OnboardingStep.SETUP_PIN -> {
                if (current.pin.length < 4) {
                    _uiState.value = current.copy(pinError = "El PIN debe tener al menos 4 dígitos")
                    return
                }
                if (current.pin != current.pinConfirm) {
                    _uiState.value = current.copy(pinError = "Los PIN no coinciden")
                    return
                }
                _uiState.value = current.copy(
                    step = OnboardingStep.SETUP_PHONE,
                    pinError = null
                )
            }
            OnboardingStep.SETUP_PHONE -> {
                val nextStep = when (current.selectedMode) {
                    AppMode.CHILD -> OnboardingStep.LINK_PARENT
                    AppMode.PARENT -> OnboardingStep.PERMISSIONS
                    null -> OnboardingStep.CHOOSE_MODE
                }
                _uiState.value = current.copy(step = nextStep)
            }
            OnboardingStep.LINK_PARENT -> {
                if (current.parentDeviceId.isBlank()) {
                    _uiState.value = current.copy(error = "Ingresa el ID del dispositivo padre")
                    return
                }
                _uiState.value = current.copy(step = OnboardingStep.PERMISSIONS, error = null)
            }
            OnboardingStep.PERMISSIONS -> {
                completeRegistration()
            }
            else -> {}
        }
    }

    fun skipPhone() {
        _uiState.value = _uiState.value.copy(step = when (_uiState.value.selectedMode) {
            AppMode.CHILD -> OnboardingStep.LINK_PARENT
            else -> OnboardingStep.PERMISSIONS
        })
    }

    fun completeRegistration() {
        val state = _uiState.value
        val mode = state.selectedMode ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = registerDeviceUseCase(
                mode = mode,
                pin = state.pin,
                phoneNumber = state.phoneNumber
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    registrationComplete = true,
                    step = OnboardingStep.COMPLETE
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error de registro"
                )
            }
        }
    }
}
