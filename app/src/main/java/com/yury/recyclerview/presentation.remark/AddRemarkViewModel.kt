package com.yury.recyclerview.presentation.remark

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yury.recyclerview.domain.usecase.AddRemarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRemarkViewModel @Inject constructor(
    private val addRemarkUseCase: AddRemarkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRemarkUiState())
    val uiState: StateFlow<AddRemarkUiState> = _uiState.asStateFlow()

    fun setContext(context: Context) {
        _uiState.value = _uiState.value.copy(context = context)
    }

    fun addRemark(
        commissionId: Long,
        location: String,
        objectName: String,
        remarkType: String,
        description: String,
        remarkLat: Double,
        remarkLng: Double,
        userLat: Double,
        userLng: Double,
        photos: List<Uri>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val remarkId = addRemarkUseCase.invokeWithContext(
                    context = _uiState.value.context ?: throw IllegalStateException("Context is not set"),
                    commissionId = commissionId,
                    location = location,
                    objectName = objectName,
                    remarkType = remarkType,
                    description = description,
                    remarkLat = remarkLat,
                    remarkLng = remarkLng,
                    userLat = userLat,
                    userLng = userLng,
                    photos = photos
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    savedRemarkId = remarkId,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка сохранения замечания"
                )
            }
        }
    }

    fun addPhoto(photo: Uri) {
        _uiState.value = _uiState.value.copy(photos = _uiState.value.photos + photo)
    }

    fun removePhoto(photo: Uri) {
        _uiState.value = _uiState.value.copy(photos = _uiState.value.photos - photo)
    }
}

data class AddRemarkUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedRemarkId: Long? = null,
    val context: Context? = null,
    val photos: List<Uri> = emptyList()
)
