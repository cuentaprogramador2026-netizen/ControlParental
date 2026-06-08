package com.controlparental.app.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlparental.app.domain.repository.AppRepository
import com.controlparental.app.domain.usecase.RequestExtraTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockUiState(
    val extraTimeRemaining: Int = 0,
    val hasExtraTime: Boolean = false,
    val requestSent: Boolean = false,
    val requestError: String? = null,
    val unlocked: Boolean = false
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val repository: AppRepository,
    private val requestExtraTimeUseCase: RequestExtraTimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    init {
        checkExtraTime()
    }

    private fun checkExtraTime() {
        viewModelScope.launch {
            val extraTime = repository.getExtraTimeRemaining()
            _uiState.value = _uiState.value.copy(
                extraTimeRemaining = extraTime,
                hasExtraTime = extraTime > 0
            )
        }
    }

    fun requestExtraTime(minutes: Int, packageName: String) {
        viewModelScope.launch {
            val result = requestExtraTimeUseCase(minutes, packageName)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(requestSent = true, requestError = null)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    requestSent = false,
                    requestError = error.message ?: "Error al enviar solicitud"
                )
            }
        }
    }

    fun verifyPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val valid = repository.verifyPin(pin)
            if (valid) {
                _uiState.value = _uiState.value.copy(unlocked = true)
            }
            onResult(valid)
        }
    }
}
