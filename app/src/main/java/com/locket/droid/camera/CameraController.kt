package com.locket.droid.camera

import android.animation.ValueAnimator
import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.animation.DecelerateInterpolator
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ZoomState
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class FlashMode(val imageCaptureMode: Int) {
    OFF(ImageCapture.FLASH_MODE_OFF),
    ON(ImageCapture.FLASH_MODE_ON),
    AUTO(ImageCapture.FLASH_MODE_AUTO);

    fun next(): FlashMode = entries[(ordinal + 1) % entries.size]
}

/** CameraX vendor extension applied on the back/front camera when the device supports it. */
enum class CaptureEffect(internal val extensionMode: Int) {
    STANDARD(ExtensionMode.NONE),
    NIGHT(ExtensionMode.NIGHT),
    PORTRAIT(ExtensionMode.BOKEH),
}

data class CameraOption(
    val index: Int,
    val id: String,
    val isFront: Boolean,
    /** Equivalent 35mm focal length in mm, null if unknown. */
    val focalLength35mm: Float?,
    val label: String,
    val cameraInfo: CameraInfo? = null,
)

/** Emitted on every tap-to-focus; x/y are in PreviewView pixels so the UI can draw a ring. */
data class FocusPoint(val x: Float, val y: Float, val token: Long)

/**
 * Owns the CameraX session. All methods must be called on the main thread.
 *
 * Lifecycle: call [init] when the PreviewView is created, and [release] when it is disposed.
 * The controller keeps references to the PreviewView and LifecycleOwner between those calls,
 * so a ViewModel holding it must call [release] (CameraViewModel does so in onCleared).
 */
@OptIn(ExperimentalCamera2Interop::class)
class CameraController {

    private val _availableCameraInfos = MutableStateFlow<List<CameraInfo>>(emptyList())
    private val _cameras = MutableStateFlow<List<CameraOption>>(emptyList())
    private val _currentIndex = MutableStateFlow(0)
    private val _flashMode = MutableStateFlow(FlashMode.OFF)
    private val _hasFlashUnit = MutableStateFlow(false)
    private val _zoomRatio = MutableStateFlow(1f)
    private val _minZoom = MutableStateFlow(1f)
    private val _maxZoom = MutableStateFlow(1f)
    private val _effect = MutableStateFlow(CaptureEffect.STANDARD)
    private val _availableEffects = MutableStateFlow(listOf(CaptureEffect.STANDARD))
    private val _ready = MutableStateFlow(false)
    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _focusEvents = MutableSharedFlow<FocusPoint>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** Exposes all available CameraInfo instances enumerated from ProcessCameraProvider */
    val availableCameraInfos: StateFlow<List<CameraInfo>> = _availableCameraInfos.asStateFlow()
    val cameraInfos: List<CameraInfo> get() = _availableCameraInfos.value

    val cameras: StateFlow<List<CameraOption>> = _cameras.asStateFlow()
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()
    /** False e.g. on most front cameras; UI can dim the flash button. */
    val hasFlashUnit: StateFlow<Boolean> = _hasFlashUnit.asStateFlow()
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()
    val minZoomRatio: StateFlow<Float> = _minZoom.asStateFlow()
    val maxZoomRatio: StateFlow<Float> = _maxZoom.asStateFlow()
    val effect: StateFlow<CaptureEffect> = _effect.asStateFlow()
    val availableEffects: StateFlow<List<CaptureEffect>> = _availableEffects.asStateFlow()
    /** True once a camera is bound. */
    val isReady: StateFlow<Boolean> = _ready.asStateFlow()
    val errors: SharedFlow<Throwable> = _errors.asSharedFlow()
    val focusEvents: SharedFlow<FocusPoint> = _focusEvents.asSharedFlow()

    /** Mirror the saved JPEG for the front camera (matches the selfie preview). */
    var mirrorFrontCapture: Boolean = true
    /** Crop the saved photo to exactly what the PreviewView shows (WYSIWYG with rounded viewfinder). */
    var cropToViewport: Boolean = true

    private var appContext: Context? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var previewView: PreviewView? = null
    private var provider: ProcessCameraProvider? = null
    private var extensionsManager: ExtensionsManager? = null
    private var camera: Camera? = null

    private var zoomLive: LiveData<ZoomState>? = null
    private var zoomObserver: Observer<ZoomState>? = null
    private var zoomAnimator: ValueAnimator? = null
    private val zoomByCamera = mutableMapOf<Int, Float>()
    private val zoomByFacing = mutableMapOf<Boolean, Float>()
    private var focusToken = 0L

    private val imageCapture: ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setJpegQuality(95)
        .setFlashMode(ImageCapture.FLASH_MODE_OFF)
        .build()

    // ---------------------------------------------------------------- lifecycle

    /**
     * Binds Preview + ImageCapture to [lifecycleOwner]. Safe to call again with a new PreviewView
     * (e.g. after recomposition / config change): it rebinds on the same camera.
     */
    fun init(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val ctx = context.applicationContext
        appContext = ctx
        this.lifecycleOwner = lifecycleOwner
        this.previewView = previewView
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        installTouch(previewView)

        val existing = provider
        if (existing != null) {
            bind()
            return
        }
        val future = ProcessCameraProvider.getInstance(ctx)
        future.addListener({
            try {
                val p = future.get()
                provider = p
                _availableCameraInfos.value = p.availableCameraInfos
                _cameras.value = enumerate(p)
                if (_currentIndex.value !in _cameras.value.indices) {
                    _currentIndex.value = _cameras.value.indexOfFirst { !it.isFront }.coerceAtLeast(0)
                }
                bind()
                loadExtensions(ctx, p)
            } catch (e: Exception) {
                _errors.tryEmit(e)
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    /** Unbinds and drops every reference to the view and lifecycle owner. */
    fun release() {
        zoomAnimator?.cancel()
        detachZoomObserver()
        previewView?.setOnTouchListener(null)
        provider?.unbindAll()
        camera = null
        previewView = null
        lifecycleOwner = null
        _ready.value = false
    }

    // ---------------------------------------------------------------- cameras

    /** Switches to cameras.value[index]. Each camera remembers its own zoom. */
    fun switchToCamera(index: Int) {
        val list = _cameras.value
        if (index !in list.indices || index == _currentIndex.value) return
        zoomAnimator?.cancel()
        val current = list.getOrNull(_currentIndex.value)
        if (current != null) {
            zoomByCamera[_currentIndex.value] = _zoomRatio.value
            zoomByFacing[current.isFront] = _zoomRatio.value
        }
        _currentIndex.value = index
        bind()
    }

    /**
     * Front <-> back flip. Preserves zoom state between front and back camera switches.
     */
    fun flipCamera() {
        val list = _cameras.value
        val current = list.getOrNull(_currentIndex.value) ?: return
        val targetFacing = !current.isFront
        val target = lastIndexByFacing[targetFacing]
            ?.takeIf { it in list.indices && list[it].isFront == targetFacing }
            ?: list.indexOfFirst { it.isFront == targetFacing }
        if (target >= 0) switchToCamera(target)
    }

    private val lastIndexByFacing = mutableMapOf<Boolean, Int>()

    private fun enumerate(p: ProcessCameraProvider): List<CameraOption> {
        val raw = p.availableCameraInfos.mapNotNull { info ->
            runCatching {
                val c2 = Camera2CameraInfo.from(info)
                val front = info.lensFacing == CameraSelector.LENS_FACING_FRONT
                val f = c2.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.minOrNull()
                val sensor = c2.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                val f35 = if (f != null && sensor != null && sensor.width > 0f && sensor.height > 0f) {
                    f * 43.27f / kotlin.math.hypot(sensor.width, sensor.height)
                } else null
                Triple(c2.cameraId, front, f35) to info
            }.getOrNull()
        }.distinctBy { it.first.first }
        // Back cameras first, then front; within a side keep provider order.
        val ordered = raw.filter { !it.first.second } + raw.filter { it.first.second }
        return ordered.mapIndexed { i, (meta, info) ->
            val (id, front, f35) = meta
            val label = when {
                front -> "Front"
                f35 == null -> "Back"
                f35 < 20f -> "Ultra wide"
                f35 > 40f -> "Telephoto"
                else -> "Main"
            }
            CameraOption(i, id, front, f35, label, info)
        }
    }

    private fun selectorFor(option: CameraOption): CameraSelector =
        CameraSelector.Builder()
            .addCameraFilter { infos ->
                val match = infos.filter { runCatching { Camera2CameraInfo.from(it).cameraId == option.id }.getOrDefault(false) }
                if (match.isNotEmpty()) match
                else infos.filter { (it.lensFacing == CameraSelector.LENS_FACING_FRONT) == option.isFront }
            }
            .build()

    // ---------------------------------------------------------------- binding

    private fun displayRotation(): Int = previewView?.display?.rotation ?: Surface.ROTATION_0

    private fun bind(allowEffect: Boolean = true) {
        val p = provider ?: return
        val owner = lifecycleOwner ?: return
        val pv = previewView ?: return
        val option = _cameras.value.getOrNull(_currentIndex.value) ?: return

        var selector = selectorFor(option)
        var effect = if (allowEffect) _effect.value else CaptureEffect.STANDARD
        extensionsManager?.let { em ->
            val available = runCatching {
                CaptureEffect.entries.filter { it == CaptureEffect.STANDARD || em.isExtensionAvailable(selector, it.extensionMode) }
            }.getOrDefault(listOf(CaptureEffect.STANDARD))
            _availableEffects.value = available
            if (effect !in available) effect = CaptureEffect.STANDARD
            if (effect != CaptureEffect.STANDARD) {
                selector = runCatching { em.getExtensionEnabledCameraSelector(selector, effect.extensionMode) }
                    .getOrElse { effect = CaptureEffect.STANDARD; selector }
            }
        }
        _effect.value = effect

        val rotation = displayRotation()
        imageCapture.targetRotation = rotation
        val preview = Preview.Builder().setTargetRotation(rotation).build()
        preview.surfaceProvider = pv.surfaceProvider

        val group = UseCaseGroup.Builder().addUseCase(preview).addUseCase(imageCapture).apply {
            if (cropToViewport) pv.viewPort?.let { setViewPort(it) }
        }.build()

        try {
            detachZoomObserver()
            p.unbindAll()
            val cam = p.bindToLifecycle(owner, selector, group)
            camera = cam
            lastIndexByFacing[option.isFront] = option.index
            imageCapture.flashMode = _flashMode.value.imageCaptureMode
            _hasFlashUnit.value = cam.cameraInfo.hasFlashUnit()
            attachZoomObserver(cam, owner)
            val zs = cam.cameraInfo.zoomState.value
            val min = zs?.minZoomRatio ?: 1f
            val max = zs?.maxZoomRatio ?: 1f
            _minZoom.value = min
            _maxZoom.value = max
            val saved = (zoomByCamera[option.index] ?: zoomByFacing[option.isFront] ?: 1f).coerceIn(min, max)
            cam.cameraControl.setZoomRatio(saved)
            _zoomRatio.value = saved
            _ready.value = true
        } catch (e: Exception) {
            _ready.value = false
            if (effect != CaptureEffect.STANDARD) {
                // Extension refused to bind: fall back to the plain pipeline instead of failing.
                _effect.value = CaptureEffect.STANDARD
                bind(allowEffect = false)
            } else {
                _errors.tryEmit(e)
            }
        }
    }

    private fun loadExtensions(ctx: Context, p: ProcessCameraProvider) {
        val future = runCatching { ExtensionsManager.getInstanceAsync(ctx, p) }.getOrNull() ?: return
        future.addListener({
            runCatching { extensionsManager = future.get() }
            if (extensionsManager != null && provider != null) {
                // Refresh availableEffects for the current camera (rebind is cheap right after start).
                bind()
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    /** Select NIGHT / PORTRAIT if [availableEffects] contains it; otherwise ignored. */
    fun setEffect(effect: CaptureEffect) {
        if (effect !in _availableEffects.value || effect == _effect.value) return
        _effect.value = effect
        bind()
    }

    // ---------------------------------------------------------------- flash

    fun setFlashMode(mode: FlashMode) {
        _flashMode.value = mode
        imageCapture.flashMode = mode.imageCaptureMode
    }

    /** OFF -> ON -> AUTO -> OFF. */
    fun toggleFlash() = setFlashMode(_flashMode.value.next())

    // ---------------------------------------------------------------- zoom / focus

    fun setZoomRatio(ratio: Float) {
        zoomAnimator?.cancel()
        applyZoom(ratio)
    }

    /** Smooth zoom to a preset (0.5x / 1x / 2x buttons). */
    fun animateZoomTo(ratio: Float, durationMs: Long = 250) {
        val target = ratio.coerceIn(_minZoom.value, _maxZoom.value)
        zoomAnimator?.cancel()
        zoomAnimator = ValueAnimator.ofFloat(_zoomRatio.value, target).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator()
            addUpdateListener { applyZoom(it.animatedValue as Float) }
            start()
        }
    }

    private fun applyZoom(ratio: Float) {
        val cam = camera ?: return
        val z = ratio.coerceIn(_minZoom.value, _maxZoom.value)
        cam.cameraControl.setZoomRatio(z)
        _zoomRatio.value = z
        zoomByCamera[_currentIndex.value] = z
        val option = _cameras.value.getOrNull(_currentIndex.value)
        if (option != null) {
            zoomByFacing[option.isFront] = z
        }
    }

    private fun attachZoomObserver(cam: Camera, owner: LifecycleOwner) {
        val live = cam.cameraInfo.zoomState
        val obs = Observer<ZoomState> { zs ->
            _zoomRatio.value = zs.zoomRatio
            _minZoom.value = zs.minZoomRatio
            _maxZoom.value = zs.maxZoomRatio
        }
        live.observe(owner, obs)
        zoomLive = live
        zoomObserver = obs
    }

    private fun detachZoomObserver() {
        zoomObserver?.let { zoomLive?.removeObserver(it) }
        zoomLive = null
        zoomObserver = null
    }

    fun focusAt(x: Float, y: Float) {
        val pv = previewView ?: return
        val cam = camera ?: return
        val point = pv.meteringPointFactory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        if (!cam.cameraInfo.isFocusMeteringSupported(action)) return
        cam.cameraControl.startFocusAndMetering(action)
        _focusEvents.tryEmit(FocusPoint(x, y, ++focusToken))
    }

    /** Pinch = zoom, single tap = focus. */
    private fun installTouch(pv: PreviewView) {
        val scale = ScaleGestureDetector(pv.context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                setZoomRatio(_zoomRatio.value * detector.scaleFactor)
                return true
            }
        })
        val tap = GestureDetector(pv.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                focusAt(e.x, e.y)
                return true
            }
        })
        pv.setOnTouchListener { v, e ->
            scale.onTouchEvent(e)
            if (!scale.isInProgress && e.pointerCount == 1) tap.onTouchEvent(e)
            if (e.actionMasked == MotionEvent.ACTION_UP) v.performClick()
            true
        }
    }

    // ---------------------------------------------------------------- capture

    /** Saves a JPEG into Pictures/Locket; callbacks run on the main thread. */
    fun takePicture(onSuccess: (Uri) -> Unit, onError: (Throwable) -> Unit) {
        val fail = onError // the callback object below has its own onError(); avoid shadowing
        val ctx = appContext
        val option = _cameras.value.getOrNull(_currentIndex.value)
        if (ctx == null || option == null || camera == null) {
            fail(IllegalStateException("Camera not ready"))
            return
        }
        imageCapture.targetRotation = displayRotation()
        val name = "Locket_" + SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Locket")
        }
        val metadata = ImageCapture.Metadata().apply { isReversedHorizontal = option.isFront && mirrorFrontCapture }
        val options = ImageCapture.OutputFileOptions.Builder(
            ctx.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values,
        ).setMetadata(metadata).build()

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(ctx), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                val uri = result.savedUri
                if (uri != null) onSuccess(uri) else fail(IllegalStateException("MediaStore returned no Uri"))
            }

            override fun onError(exception: ImageCaptureException) = fail(exception)
        })
    }

    /** Suspend flavour of [takePicture]; must be called from the main dispatcher. */
    suspend fun capture(): Uri = suspendCancellableCoroutine { cont ->
        takePicture(
            onSuccess = { if (cont.isActive) cont.resume(it) },
            onError = { if (cont.isActive) cont.resumeWithException(it) },
        )
    }
}
