package com.example.funinactionproto.pose

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.sqrt


enum class RepState { IDLE, GET_READY, WATCHING, RECORDING, SCORING }


data class RepDetectorConfig(
    val getReadyDurationMs: Long = 2000L,
    val motionStartThreshold: Float = 0.04f,
    val motionStopThreshold: Float = 0.015f,
    val stopDebounceFrames: Int = 10,   // consecutive low-motion frames needed to end a rep
    val minRepFrames: Int = 10,         // reps shorter than this are treated as noise, not real
    val maxRepDurationMs: Long = 8000L  // safety cutoff so a rep can't buffer forever
)


class RepDetector(
    private val config: RepDetectorConfig = RepDetectorConfig(),
    private val onStateChanged: (RepState) -> Unit,
    private val onRepCompleted: (List<Map<String, Float>>) -> Unit
) {
    private var state = RepState.IDLE
    private var previousPoints: List<Point>? = null
    private var lowMotionFrameCount = 0
    private var repStartTimeMs = 0L
    private val bufferedFeatures = mutableListOf<Map<String, Float>>()

    // Which landmarks to track for motion energy - wrists and ankles cover most
    // of what moves during these skills (arm action, footwork)
    private val motionLandmarkIndices = listOf(
        Landmark.L_WRIST, Landmark.R_WRIST, Landmark.L_ANKLE, Landmark.R_ANKLE
    )

    /** Call when the player selects this skill and the capture screen opens. */
    fun start() {
        state = RepState.GET_READY
        onStateChanged(state)
        // The Fragment is responsible for waiting config.getReadyDurationMs (e.g. with a
        // Handler.postDelayed) and then calling transitionToWatching() - kept separate
        // so this class doesn't need its own timer/Handler dependency.
    }

    fun transitionToWatching() {
        state = RepState.WATCHING
        previousPoints = null
        onStateChanged(state)
    }

    /**
     * Call once per frame. landmarks is the raw frame's landmarks (needed for motion
     * tracking), features is that same frame's already-computed PoseFeatureCalculator
     * output (buffered as-is if this frame ends up part of a rep).
     */
    fun onFrame(landmarks: List<NormalizedLandmark>, features: Map<String, Float>) {
        val torsoSize = PoseFeatureCalculator.computeTorsoSize(landmarks)
        when (state) {
            RepState.WATCHING -> handleWatching(landmarks, features, torsoSize)
            RepState.RECORDING -> handleRecording(landmarks, features, torsoSize)
            else -> { /* IDLE, GET_READY, SCORING - ignore frames */ }
        }
    }

    private fun motionEnergy(landmarks: List<NormalizedLandmark>, torsoSize: Float): Float {
        val currentPoints = motionLandmarkIndices.map { Point(landmarks[it].x(), landmarks[it].y()) }
        val previous = previousPoints
        previousPoints = currentPoints
        if (previous == null) return 0f

        var totalDisplacement = 0f
        for (i in currentPoints.indices) {
            val dx = currentPoints[i].x - previous[i].x
            val dy = currentPoints[i].y - previous[i].y
            totalDisplacement += sqrt(dx * dx + dy * dy)
        }
        return (totalDisplacement / currentPoints.size) / torsoSize
    }

    private fun handleWatching(landmarks: List<NormalizedLandmark>, features: Map<String, Float>, torsoSize: Float) {
        val energy = motionEnergy(landmarks, torsoSize)
        if (energy > config.motionStartThreshold) {
            state = RepState.RECORDING
            bufferedFeatures.clear()
            bufferedFeatures.add(features)
            lowMotionFrameCount = 0
            repStartTimeMs = System.currentTimeMillis()
            onStateChanged(state)
        }
    }

    private fun handleRecording(landmarks: List<NormalizedLandmark>, features: Map<String, Float>, torsoSize: Float) {
        bufferedFeatures.add(features)
        val energy = motionEnergy(landmarks, torsoSize)

        lowMotionFrameCount = if (energy < config.motionStopThreshold) lowMotionFrameCount + 1 else 0

        val elapsed = System.currentTimeMillis() - repStartTimeMs
        val shouldStop = lowMotionFrameCount >= config.stopDebounceFrames || elapsed >= config.maxRepDurationMs

        if (shouldStop) {
            if (bufferedFeatures.size >= config.minRepFrames) {
                state = RepState.SCORING
                onStateChanged(state)
                onRepCompleted(bufferedFeatures.toList())
            } else {
                // Too short to be a real rep (likely noise/a twitch) - go back to
                // watching instead of trying to score almost nothing.
                state = RepState.WATCHING
                bufferedFeatures.clear()
                onStateChanged(state)
            }
        }
    }

    /** Call after displaying results, to go again (back to watching for the next attempt). */
    fun reset() {
        state = RepState.WATCHING
        bufferedFeatures.clear()
        previousPoints = null
        onStateChanged(state)
    }
}
