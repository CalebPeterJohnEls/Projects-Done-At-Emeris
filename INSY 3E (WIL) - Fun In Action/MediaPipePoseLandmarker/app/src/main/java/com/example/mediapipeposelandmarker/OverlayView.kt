package com.example.mediapipeposelandmarker
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var poseResults: PoseLandmarkerResult? = null
    private var handResults: HandLandmarkerResult? = null
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val pointPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 12f
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val handPointPaint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    private val handLinePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    // Hand connections — each pair is a line between two landmark indices
    private val handConnections = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4,       // thumb
        0 to 5, 5 to 6, 6 to 7, 7 to 8,        // index
        0 to 9, 9 to 10, 10 to 11, 11 to 12,   // middle
        0 to 13, 13 to 14, 14 to 15, 15 to 16, // ring
        0 to 17, 17 to 18, 18 to 19, 19 to 20, // pinky
        5 to 9, 9 to 13, 13 to 17              // palm
    )

    fun setResults(
        poseLandmarkerResult: PoseLandmarkerResult,
        imageWidth: Int,
        imageHeight: Int
    ) {
        this.poseResults = poseLandmarkerResult
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        invalidate()
    }

    fun setHandResults(handLandmarkerResult: HandLandmarkerResult) {
        this.handResults = handLandmarkerResult
        invalidate()
    }

    fun clear() {
        poseResults = null
        handResults = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Scale to fit the view while maintaining aspect ratio
        val scaleX = width.toFloat() / imageWidth
        val scaleY = height.toFloat() / imageHeight
        val scale = minOf(scaleX, scaleY)  // ← use uniform scale to preserve proportions

        // Center the scaled content
        val offsetX = (width - imageWidth * scale) / 2f
        val offsetY = (height - imageHeight * scale) / 2f

        // Draw pose skeleton
        val poseLandmarks = poseResults?.landmarks()
        if (!poseLandmarks.isNullOrEmpty()) {
            val bodyLandmarks = poseLandmarks[0]

            for (connection in PoseLandmarker.POSE_LANDMARKS) {
                val start = bodyLandmarks[connection!!.start()]
                val end = bodyLandmarks[connection.end()]
                canvas.drawLine(
                    offsetX + start.x() * imageWidth * scale,
                    offsetY + start.y() * imageHeight * scale,
                    offsetX + end.x() * imageWidth * scale,
                    offsetY + end.y() * imageHeight * scale,
                    linePaint
                )
            }

            for (landmark in bodyLandmarks) {
                canvas.drawCircle(
                    offsetX + landmark.x() * imageWidth * scale,
                    offsetY + landmark.y() * imageHeight * scale,
                    8f,
                    pointPaint
                )
            }
        }

        // Draw hand skeleton
        val handLandmarks = handResults?.landmarks()
        if (!handLandmarks.isNullOrEmpty()) {
            for (hand in handLandmarks) {
                for ((start, end) in handConnections) {
                    val s = hand[start]
                    val e = hand[end]
                    canvas.drawLine(
                        offsetX + s.x() * imageWidth * scale,
                        offsetY + s.y() * imageHeight * scale,
                        offsetX + e.x() * imageWidth * scale,
                        offsetY + e.y() * imageHeight * scale,
                        handLinePaint
                    )
                }
                for (landmark in hand) {
                    canvas.drawCircle(
                        offsetX + landmark.x() * imageWidth * scale,
                        offsetY + landmark.y() * imageHeight * scale,
                        6f,
                        handPointPaint
                    )
                }
            }
        }
    }
}