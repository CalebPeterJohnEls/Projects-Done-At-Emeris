package com.example.mediapipeposelandmarker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkerHelper(
    val context: Context,
    val listener: HandLandmarkerListener
) {
    private var handLandmarker: HandLandmarker? = null

    interface HandLandmarkerListener {
        fun onHandResults(result: HandLandmarkerResult)
        fun onHandError(error: String)
    }

    fun setup() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(2)                    // track both hands
            .setMinHandDetectionConfidence(0.5f)
            .setMinHandPresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, _ -> listener.onHandResults(result) }
            .setErrorListener { error -> listener.onHandError(error.message ?: "Unknown error") }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun detectLiveStream(imageProxy: ImageProxy, isFrontCamera: Boolean) {
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
        handLandmarker?.detectAsync(mpImage, imageProxy.imageInfo.timestamp)
    }

    fun close() {
        handLandmarker?.close()
        handLandmarker = null
    }

    object HandLandmarks {
        const val WRIST = 0
        const val THUMB_CMC = 1;  const val THUMB_MCP = 2
        const val THUMB_IP = 3;   const val THUMB_TIP = 4
        const val INDEX_MCP = 5;  const val INDEX_PIP = 6
        const val INDEX_DIP = 7;  const val INDEX_TIP = 8
        const val MIDDLE_MCP = 9; const val MIDDLE_PIP = 10
        const val MIDDLE_DIP = 11; const val MIDDLE_TIP = 12
        const val RING_MCP = 13;  const val RING_PIP = 14
        const val RING_DIP = 15;  const val RING_TIP = 16
        const val PINKY_MCP = 17; const val PINKY_PIP = 18
        const val PINKY_DIP = 19; const val PINKY_TIP = 20
    }

    fun detectBitmap(bitmap: Bitmap, timestamp: Long) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        handLandmarker?.detectAsync(mpImage, timestamp)
    }
}