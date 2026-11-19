package com.example.funinactionproto

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.max
import kotlin.math.min

class SkeletonOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private class Joint(
        val x: Float,
        val y: Float,
        val visibility: Float
    )

    @Volatile private var joints: List<Joint> = emptyList()

    // Raw camera-analysis buffer dimensions, BEFORE rotation.
    @Volatile private var rawWidth = 0
    @Volatile private var rawHeight = 0

    // Clockwise rotation needed to make the raw buffer upright.
    @Volatile private var rotationDegrees = 0

    @Volatile var mirrorX = false
    @Volatile var fillCenter = true

    private val bonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2EC4B6")
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val jointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    fun update(landmarks: List<NormalizedLandmark>?) {
        joints = landmarks?.map {
            Joint(it.x(), it.y(), it.visibility().orElse(0f))
        } ?: emptyList()
        postInvalidate()
    }

    /**
     * Pass the RAW ImageProxy/bitmap dimensions and its CameraX rotation.
     *
     * Example:
     * skeletonOverlay.setFrameGeometry(
     *     imageProxy.width,
     *     imageProxy.height,
     *     imageProxy.imageInfo.rotationDegrees
     * )
     */
    fun setFrameGeometry(width: Int, height: Int, rotation: Int) {
        rawWidth = width
        rawHeight = height
        rotationDegrees = rotation
    }

    fun clear() {
        joints = emptyList()
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val points = joints
        val bufferW = rawWidth
        val bufferH = rawHeight

        if (points.isEmpty() || bufferW <= 0 || bufferH <= 0 ||
            width == 0 || height == 0
        ) return

        // Dimensions of the upright image that MediaPipe actually analysed.
        val uprightW: Int
        val uprightH: Int

        when (rotationDegrees) {
            90, 270 -> {
                uprightW = bufferH
                uprightH = bufferW
            }
            else -> {
                uprightW = bufferW
                uprightH = bufferH
            }
        }

        val scale = if (fillCenter) {
            max(width.toFloat() / uprightW, height.toFloat() / uprightH)
        } else {
            min(width.toFloat() / uprightW, height.toFloat() / uprightH)
        }

        val offsetX = (width - uprightW * scale) / 2f
        val offsetY = (height - uprightH * scale) / 2f

        fun screenX(j: Joint): Float {
            val uprightX = when (rotationDegrees) {
                90 -> 1f - j.y
                270 -> j.y
                180 -> 1f - j.x
                else -> j.x
            }

            val mirroredX = if (mirrorX) 1f - uprightX else uprightX
            return mirroredX * uprightW * scale + offsetX
        }

        fun screenY(j: Joint): Float {
            val uprightY = when (rotationDegrees) {
                90 -> j.x
                270 -> 1f - j.x
                180 -> 1f - j.y
                else -> j.y
            }

            return uprightY * uprightH * scale + offsetY
        }

        for ((a, b) in BONES) {
            if (a >= points.size || b >= points.size) continue

            val ja = points[a]
            val jb = points[b]

            if (ja.visibility < MIN_VISIBILITY || jb.visibility < MIN_VISIBILITY) continue

            canvas.drawLine(
                screenX(ja), screenY(ja),
                screenX(jb), screenY(jb),
                bonePaint
            )
        }

        for (i in JOINT_INDICES) {
            if (i >= points.size) continue

            val j = points[i]
            if (j.visibility < MIN_VISIBILITY) continue

            canvas.drawCircle(screenX(j), screenY(j), 9f, jointPaint)
        }
    }

    companion object {
        private const val MIN_VISIBILITY = 0.3f

        private val BONES = listOf(
            11 to 12,
            11 to 13, 13 to 15,
            12 to 14, 14 to 16,
            15 to 17, 15 to 19, 15 to 21, 17 to 19,
            16 to 18, 16 to 20, 16 to 22, 18 to 20,
            11 to 23, 12 to 24, 23 to 24,
            23 to 25, 25 to 27,
            24 to 26, 26 to 28,
            27 to 29, 29 to 31, 27 to 31,
            28 to 30, 30 to 32, 28 to 32
        )

        private val JOINT_INDICES = listOf(0) + (11..32)
    }
}