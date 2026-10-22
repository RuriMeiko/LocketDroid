package com.locket.droid.camera

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraInfo
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.locket.droid.LocketApp
import com.locket.droid.data.LocketRepository
import com.locket.droid.widget.PhotoWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** State holder for the camera screen. Wraps [CameraController]; the UI only talks to this. */
class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val controller = CameraController()
    private val repository: LocketRepository by lazy {
        (getApplication<Application>() as? LocketApp)?.repository ?: LocketRepository(getApplication())
    }

    val availableCameras: StateFlow<List<CameraOption>> = controller.cameras
    val availableCameraInfos: StateFlow<List<CameraInfo>> = controller.availableCameraInfos
    val currentCameraIndex: StateFlow<Int> = controller.currentIndex
    val flashMode: StateFlow<FlashMode> = controller.flashMode
    val hasFlashUnit: StateFlow<Boolean> = controller.hasFlashUnit
    val zoomRatio: StateFlow<Float> = controller.zoomRatio
    val minZoomRatio: StateFlow<Float> = controller.minZoomRatio
    val maxZoomRatio: StateFlow<Float> = controller.maxZoomRatio
    val effect: StateFlow<CaptureEffect> = controller.effect
    val availableEffects: StateFlow<List<CaptureEffect>> = controller.availableEffects
    val isReady: StateFlow<Boolean> = controller.isReady
    val focusEvents: SharedFlow<FocusPoint> = controller.focusEvents
    val cameraErrors: SharedFlow<Throwable> = controller.errors

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _lastPhotoUri = MutableStateFlow<Uri?>(null)
    val lastPhotoUri: StateFlow<Uri?> = _lastPhotoUri.asStateFlow()

    /** One-shot error text for capture failures (UI may show a toast and call [clearError]). */
    private val _captureError = MutableStateFlow<String?>(null)
    val captureError: StateFlow<String?> = _captureError.asStateFlow()

    /** Call from AndroidView `factory` / `update` with the PreviewView. */
    fun bindCamera(context: Context, owner: LifecycleOwner, previewView: PreviewView) =
        controller.init(context, owner, previewView)

    /** Call from DisposableEffect.onDispose. */
    fun unbindCamera() = controller.release()

    fun flip() = controller.flipCamera()
    fun flipCamera() = controller.flipCamera()

    fun toggle() = controller.toggleFlash()
    fun toggleFlash() = controller.toggleFlash()

    fun setFlash(mode: FlashMode) = controller.setFlashMode(mode)

    fun zoom(ratio: Float) = controller.setZoomRatio(ratio)
    fun setZoom(ratio: Float) = controller.setZoomRatio(ratio)
    fun animateZoomTo(ratio: Float) = controller.animateZoomTo(ratio)

    fun switchCamera(index: Int) = controller.switchToCamera(index)
    fun setEffect(effect: CaptureEffect) = controller.setEffect(effect)
    fun focusAt(x: Float, y: Float) = controller.focusAt(x, y)
    fun clearError() { _captureError.value = null }

    /** Takes a photo, saves to Pictures/Locket, adds it to the feed and refreshes the widget. */
    fun capture() {
        if (_isCapturing.value) return
        _isCapturing.value = true
        viewModelScope.launch {
            try {
                val uri = controller.capture()
                _lastPhotoUri.value = uri
                repository.addCapturedPhoto(uri)
                PhotoWidgetProvider.requestUpdate(getApplication(), uri)
            } catch (e: Exception) {
                _captureError.value = e.message ?: "Capture failed"
            } finally {
                _isCapturing.value = false
            }
        }
    }

    fun takePicture() = capture()

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }
}
