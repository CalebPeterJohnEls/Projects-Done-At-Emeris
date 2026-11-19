package com.example.funinactionproto.pose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(
    private val context: Context,

    private val onResult:
        (landmarks: List<NormalizedLandmark>?, timestampMs: Long) -> Unit,

    private val onError: (String) -> Unit,

    private val onFrame: ((Bitmap, Long) -> Unit)? = null
) {

    private var poseLandmarker: PoseLandmarker? = null

    private val yuvToRgbConverter = YuvToRgbConverter()

    fun setup() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_full.task")
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setResultListener(::onPoseResult)
            .setErrorListener { error ->
                onError(error.message ?: "Unknown PoseLandmarker error")
            }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    private fun onPoseResult(
        result: PoseLandmarkerResult,
        @Suppress("UNUSED_PARAMETER") input: MPImage
    ) {
        val firstPerson = result.landmarks().firstOrNull()

        onResult(
            firstPerson,
            System.currentTimeMillis()
        )
    }

    /**
     * rotationDegrees defaults to the frame's own rotation, so callers can just
     * write detectAsync(imageProxy). It's read here, at call time, before the
     * proxy is closed.
     */
    fun detectAsync(
        imageProxy: ImageProxy,
        rotationDegrees: Int = imageProxy.imageInfo.rotationDegrees
    ) {
        // try/finally guarantees the proxy is ALWAYS closed. If it isn't, CameraX
        // stops delivering frames after the first exception and the preview/skeleton
        // appear to freeze.
        try {
            // 1. Convert the raw YUV camera frame to RGB.
            val rawBitmap = imageProxy.toBitmap()

            // 2. Rotate the raw camera frame upright.
            val uprightBitmap = rotateBitmap(rawBitmap, rotationDegrees)

            val timestampMs = System.currentTimeMillis()

            // 3. Send the ALREADY-UPRIGHT bitmap to the overlay / hand tracker.
            onFrame?.invoke(uprightBitmap, timestampMs)

            // 4. Give MediaPipe the same upright bitmap.
            val mpImage = BitmapImageBuilder(uprightBitmap).build()
            poseLandmarker?.detectAsync(mpImage, timestampMs)

            // 5. Free the original only if rotation created a new bitmap.
            if (uprightBitmap !== rawBitmap && !rawBitmap.isRecycled) {
                rawBitmap.recycle()
            }
        } finally {
            imageProxy.close()
        }
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        yuvToRgbConverter.yuvToRgb(bitmap, this)

        return bitmap
    }
}

private fun rotateBitmap(
    bitmap: Bitmap,
    degrees: Int
): Bitmap {
    if (degrees == 0) return bitmap

    val matrix = Matrix().apply {
        postRotate(degrees.toFloat())
    }

    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}