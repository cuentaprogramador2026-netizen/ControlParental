package com.controlparental.app.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlparental.app.domain.model.ChildDevice
import com.controlparental.app.domain.model.TimeLimits
import com.controlparental.app.domain.model.TimeRequest
import com.controlparental.app.domain.repository.AppRepository
import com.controlparental.app.domain.usecase.ApproveTimeRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentUiState(
    val childDevices: List<ChildDevice> = emptyList(),
    val pendingRequests: List<TimeRequest> = emptyList(),
    val restrictedPackages: Set<String> = emptySet(),
    val timeLimits: TimeLimits = TimeLimits(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParentViewModel @Inject constructor(
    private val repository: AppRepository,
    private val approveTimeRequestUseCase: ApproveTimeRequestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentUiState())
    val uiState: StateFlow<ParentUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeRequests()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.fetchChildDevicesFromFirestore()
                repository.childDevices.collect { children ->
                    _uiState.value = _uiState.value.copy(childDevices = children)
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            repository.restrictedPackages.collect { packages ->
                _uiState.value = _uiState.value.copy(restrictedPackages = packages)
            }
        }
        viewModelScope.launch {
            repository.timeLimits.collect { limits ->
                _uiState.value = _uiState.value.copy(timeLimits = limits)
            }
        }
    }

    private fun observeRequests() {
        viewModelScope.launch {
            repository.observeRequests().collect { requests ->
                _uiState.value = _uiState.value.copy(
                    pendingRequests = requests,
                    isLoading = false
                )
            }
        }
    }

    fun approveRequest(requestId: String) {
        viewModelScope.launch {
            approveTimeRequestUseCase(requestId)
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            repository.rejectTimeRequest(requestId)
        }
    }

    fun updateRestrictedPackages(packages: Set<String>) {
        viewModelScope.launch {
            repository.setRestrictedPackages(packages)
        }
    }

    fun updateTimeLimits(limits: TimeLimits) {
        viewModelScope.launch {
            repository.setTimeLimits(limits)
        }
    }

    fun unlinkChild(deviceId: String) {
        viewModelScope.launch {
            repository.unlinkChildDevice(deviceId)
        }
    }
}
