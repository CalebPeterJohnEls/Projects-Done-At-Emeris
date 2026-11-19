package com.example.mediapipeposelandmarker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(
    val context: Context,
    val listener: LandmarkerListener
) {
    private var poseLandmarker: PoseLandmarker? = null

    private var frameWidth = 640   // ← must be here, at class level
    private var frameHeight = 480  // ← must be here, at class level

    // ---- Listener interface your Activity/Fragment will implement ----
    interface LandmarkerListener {
        fun onResults(result: PoseLandmarkerResult,  imageWidth: Int, imageHeight: Int)
        fun onError(error: String)
    }

    // ---- Call this once to initialise the landmarker ----
    fun setup() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_full.task")
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, _ -> listener.onResults(result, frameWidth, frameHeight) }  // ← updated
            .setErrorListener { error -> listener.onError(error.message ?: "Unknown error") }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    // ---- Called every frame from CameraX ----
    fun detectLiveStream(imageProxy: ImageProxy, isFrontCamera: Boolean) {

        // Rotate the frame to the correct orientation
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            if (isFrontCamera) postScale(-1f, 1f, imageProxy.width / 2f, imageProxy.height / 2f)
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
        )

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        // Send frame for async detection
        poseLandmarker?.detectAsync(mpImage, imageProxy.imageInfo.timestamp)
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    fun detectBitmap(bitmap: Bitmap, timestamp: Long) {
        frameWidth = bitmap.width
        frameHeight = bitmap.height
        val mpImage = BitmapImageBuilder(bitmap).build()
        poseLandmarker?.detectAsync(mpImage, timestamp)
    }
}