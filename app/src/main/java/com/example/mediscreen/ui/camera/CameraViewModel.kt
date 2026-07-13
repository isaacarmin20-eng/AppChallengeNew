package com.example.mediscreen.ui.camera



import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CaptureStep(val stepNumber: Int, val totalSteps: Int) {
    FACE(1, 3),
    ARMS(2, 3),
    SPEECH(3, 3)
}

data class CameraUiState(
    val currentStep: CaptureStep = CaptureStep.FACE,
    val faceImagePath: String? = null,
    val armsImagePath: String? = null,
    val speechText: String = "",
    val isCapturing: Boolean = false,
    val permissionGranted: Boolean = false
)

class CameraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(permissionGranted = granted)
    }

    fun onFaceCaptured(path: String) {
        _uiState.value = _uiState.value.copy(
            faceImagePath = path,
            currentStep = CaptureStep.ARMS
        )
    }

    fun onArmsCaptured(path: String) {
        _uiState.value = _uiState.value.copy(
            armsImagePath = path,
            currentStep = CaptureStep.SPEECH
        )
    }

    fun onSpeechTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(speechText = text)
    }

    fun setCapturing(capturing: Boolean) {
        _uiState.value = _uiState.value.copy(isCapturing = capturing)
    }

    fun isReadyToSubmit(): Boolean {
        val s = _uiState.value
        return s.faceImagePath != null && s.armsImagePath != null && s.speechText.isNotBlank()
    }
}