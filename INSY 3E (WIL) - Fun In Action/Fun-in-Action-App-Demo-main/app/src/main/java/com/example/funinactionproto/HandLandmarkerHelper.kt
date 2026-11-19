package com.example.funinactionproto.pose

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkerHelper(
    private val context: Context,
    private val onResult:
        (
        landmarks: List<List<NormalizedLandmark>>,
        handedness: List<List<Category>>,
        timestampMs: Long
    ) -> Unit,
    private val onError: (String) -> Unit
) {

    private var handLandmarker: HandLandmarker? = null
    private var lastTimestampMs: Long = 0L

    fun setup() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(2)
            .setMinHandDetectionConfidence(0.5f)
            .setMinHandPresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result: HandLandmarkerResult, image: MPImage ->
                // image already holds the timestamp you passed in detectAsync
                onHandResult(result, lastTimestampMs)
            }
            .setErrorListener { error ->
                onError(error.message ?: "Unknown hand tracking error")
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    private fun onHandResult(
        result: HandLandmarkerResult,
        timestampMs: Long
    ) {
        onResult(
            result.landmarks(),
            result.handedness(),
            timestampMs
        )
    }

    fun detectAsync(bitmap: Bitmap, timestampMs: Long) {
        lastTimestampMs = timestampMs
        val mpImage = BitmapImageBuilder(bitmap).build()
        handLandmarker?.detectAsync(mpImage, timestampMs)
    }

    fun close() {
        handLandmarker?.close()
        handLandmarker = null
    }
}