package com.yury.recyclerview.presentation.commissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yury.recyclerview.database.Commission
import com.yury.recyclerview.domain.usecase.CreateCommissionUseCase
import com.yury.recyclerview.domain.usecase.GetCommissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommissionsViewModel @Inject constructor(
    private val getCommissionsUseCase: GetCommissionsUseCase,
    private val createCommissionUseCase: CreateCommissionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionsUiState())
    val uiState: StateFlow<CommissionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCommissionsUseCase().collect { commissions ->
                _uiState.value = _uiState.value.copy(commissions = commissions)
            }
        }
    }

    fun createCommission(title: String) {
        viewModelScope.launch {
            val id = createCommissionUseCase(title)
            _uiState.value = _uiState.value.copy(lastCreatedCommissionId = id)
            // After creation the flow above will automatically update the list
        }
    }
}

data class CommissionsUiState(
    val commissions: List<Commission> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastCreatedCommissionId: Long? = null
)
