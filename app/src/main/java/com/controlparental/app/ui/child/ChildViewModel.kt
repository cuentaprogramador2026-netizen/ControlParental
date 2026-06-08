package com.controlparental.app.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlparental.app.domain.model.TimeLimits
import com.controlparental.app.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildUiState(
    val timeConsumedToday: Int = 0,
    val timeConsumedThisWeek: Int = 0,
    val timeLimits: TimeLimits = TimeLimits(),
    val extraTimeRemaining: Int = 0,
    val isMonitoringEnabled: Boolean = false
)

@HiltViewModel
class ChildViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildUiState())
    val uiState: StateFlow<ChildUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.timeLimits.collect { limits ->
                _uiState.value = _uiState.value.copy(
                    timeLimits = limits,
                    timeConsumedToday = repository.getTimeConsumedToday(),
                    timeConsumedThisWeek = repository.getTimeConsumedThisWeek(),
                    extraTimeRemaining = repository.getExtraTimeRemaining()
                )
            }
        }
        viewModelScope.launch {
            repository.monitoringEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(isMonitoringEnabled = enabled)
            }
        }
    }

    fun toggleMonitoring() {
        viewModelScope.launch {
            val newState = !_uiState.value.isMonitoringEnabled
            repository.setMonitoringEnabled(newState)
        }
    }
}
