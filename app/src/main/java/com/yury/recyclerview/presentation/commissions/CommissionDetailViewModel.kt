package com.yury.recyclerview.presentation.commissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yury.recyclerview.database.Commission
import com.yury.recyclerview.database.Remark
import com.yury.recyclerview.domain.usecase.GetCommissionsUseCase
import com.yury.recyclerview.domain.usecase.GetRemarksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommissionDetailViewModel @Inject constructor(
    private val getCommissionsUseCase: GetCommissionsUseCase,
    private val getRemarksUseCase: GetRemarksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionDetailUiState())
    val uiState: StateFlow<CommissionDetailUiState> = _uiState.asStateFlow()

    fun loadCommission(commissionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Load commission (by id we can use a separate query if needed, here we use flow)
            // For simplicity we just start collecting remarks
            getRemarksUseCase(commissionId).collect { remarks ->
                // Try to find the commission in the commissions flow
                // For now we set it to null and we will improve later
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    remarks = remarks,
                    commission = _uiState.value.commission
                )
            }
        }
    }
}

data class CommissionDetailUiState(
    val isLoading: Boolean = false,
    val commission: Commission? = null,
    val remarks: List<Remark> = emptyList(),
    val error: String? = null
)
