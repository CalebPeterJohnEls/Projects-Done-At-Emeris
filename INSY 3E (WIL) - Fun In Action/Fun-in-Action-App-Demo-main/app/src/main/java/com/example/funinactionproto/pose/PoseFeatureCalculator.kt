package com.example.funinactionproto.pose

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.*

/**
 * Kotlin port of compute_frame_features() and is_frame_reliable() from
 * extract_clip_features.py. This MUST stay in sync with the Python version -
 * the reference JSON profiles were built using that exact math, so any drift
 * here means live scores won't mean the same thing as the Python-built reference.
 *
 * If you change anything in the Python script's feature calculation, mirror
 * the change here too.
 */


data class Point(val x: Float, val y: Float)


object Landmark {
    const val NOSE = 0
    const val L_SHOULDER = 11
    const val R_SHOULDER = 12
    const val L_ELBOW = 13
    const val R_ELBOW = 14
    const val L_WRIST = 15
    const val R_WRIST = 16
    const val L_INDEX = 19
    const val R_INDEX = 20
    const val L_HIP = 23
    const val R_HIP = 24
    const val L_KNEE = 25
    const val R_KNEE = 26
    const val L_ANKLE = 27
    const val R_ANKLE = 28
}

// Same thresholds as extract_clip_features.py - keep these identical to the Python values
object FeatureConfig {
    const val VISIBILITY_THRESHOLD = 0.3f
    const val MIN_SHOULDER_SEPARATION = 0.04f

    // Full-body landmarks - required for stance/footwork-based skills
    val CORE_LANDMARKS_FULL = listOf(
        Landmark.L_SHOULDER, Landmark.R_SHOULDER, Landmark.L_ELBOW, Landmark.R_ELBOW,
        Landmark.L_WRIST, Landmark.R_WRIST, Landmark.L_HIP, Landmark.R_HIP,
        Landmark.L_KNEE, Landmark.R_KNEE, Landmark.L_ANKLE, Landmark.R_ANKLE
    )

    // Upper-body-only landmarks - for skills like grip, where legs aren't measured
    val CORE_LANDMARKS_UPPER = listOf(
        Landmark.L_SHOULDER, Landmark.R_SHOULDER, Landmark.L_ELBOW, Landmark.R_ELBOW,
        Landmark.L_WRIST, Landmark.R_WRIST, Landmark.L_HIP, Landmark.R_HIP
    )

    // Skills that only need upper-body landmarks checked - must match
    // UPPER_BODY_ONLY_SKILLS in extract_clip_features.py
    val UPPER_BODY_ONLY_SKILLS = setOf("grip")
}

// The 14 features this calculator produces, in the same order/names as FEATURE_NAMES
// in extract_clip_features.py. Each of these gets min/max/mean/range aggregated by
// whatever buffers a rep's frames together (the rep-boundary detector, built next).
val FEATURE_NAMES = listOf(
    "left_elbow_angle", "right_elbow_angle",
    "left_knee_angle", "right_knee_angle",
    "right_wrist_angle", "left_wrist_angle",
    "shoulder_dx", "shoulder_dy", "stance_width", "foot_stagger",
    "hip_center_x", "head_offset",
    "wrist_separation", "hand_height_diff"
)

object PoseFeatureCalculator {

    fun point(landmarks: List<NormalizedLandmark>, index: Int): Point =
        Point(landmarks[index].x(), landmarks[index].y())

    // Not private - HandFeatureCalculator reuses this rather than duplicating it
    fun distance(a: Point, b: Point): Float =
        sqrt((a.x - b.x).pow(2) + (a.y - b.y).pow(2))

    /**
     * Angle at point b, formed by lines b->a and b->c, in degrees.
     * Direct port of calculate_angle() in Python. Not private - reused by
     * HandFeatureCalculator for finger-curl angles too.
     */
    fun calculateAngle(a: Point, b: Point, c: Point): Float {
        val baX = a.x - b.x
        val baY = a.y - b.y
        val bcX = c.x - b.x
        val bcY = c.y - b.y

        val dot = baX * bcX + baY * bcY
        val magBa = sqrt(baX * baX + baY * baY)
        val magBc = sqrt(bcX * bcX + bcY * bcY)

        var cosine = dot / (magBa * magBc + 1e-6f)
        cosine = cosine.coerceIn(-1f, 1f)  // guard against tiny float errors going out of range
        return Math.toDegrees(acos(cosine.toDouble())).toFloat()
    }

    /**
     * Returns false if any required landmark's visibility is too low, or the shoulder
     * line has collapsed too close to a single point for a stable reading.
     * Direct port of is_frame_reliable() in Python - which landmarks are "required"
     * depends on the skill, same as the Python version.
     */
    fun isFrameReliable(landmarks: List<NormalizedLandmark>, skillName: String?): Boolean {
        val required = if (skillName in FeatureConfig.UPPER_BODY_ONLY_SKILLS) {
            FeatureConfig.CORE_LANDMARKS_UPPER
        } else {
            FeatureConfig.CORE_LANDMARKS_FULL
        }

        for (index in required) {
            // NormalizedLandmark.visibility() returns an Optional<Float> in some
            // MediaPipe Tasks versions - adjust this line to match whatever your
            // exact library version exposes if this doesn't compile as-is.
            val visibility = landmarks[index].visibility().orElse(0f)
            if (visibility < FeatureConfig.VISIBILITY_THRESHOLD) return false
        }

        val shoulderDist = distance(
            point(landmarks, Landmark.L_SHOULDER),
            point(landmarks, Landmark.R_SHOULDER)
        )
        if (shoulderDist < FeatureConfig.MIN_SHOULDER_SEPARATION) return false

        return true
    }

    /**
     * Torso size (shoulder midpoint to hip midpoint) - exposed separately so other
     * classes (like the rep-boundary motion detector) can normalize their own
     * measurements the same way, without duplicating this calculation.
     */
    fun computeTorsoSize(landmarks: List<NormalizedLandmark>): Float {
        val lShoulder = point(landmarks, Landmark.L_SHOULDER)
        val rShoulder = point(landmarks, Landmark.R_SHOULDER)
        val lHip = point(landmarks, Landmark.L_HIP)
        val rHip = point(landmarks, Landmark.R_HIP)
        val shoulderMid = Point((lShoulder.x + rShoulder.x) / 2, (lShoulder.y + rShoulder.y) / 2)
        val hipMid = Point((lHip.x + rHip.x) / 2, (lHip.y + rHip.y) / 2)
        return max(distance(shoulderMid, hipMid), 0.05f)
    }

    /**
     * Computes the 14 features for one frame's landmarks.
     * Direct port of compute_frame_features() in Python - same math, same order.
     */
    fun computeFrameFeatures(landmarks: List<NormalizedLandmark>): Map<String, Float> {
        val lShoulder = point(landmarks, Landmark.L_SHOULDER)
        val rShoulder = point(landmarks, Landmark.R_SHOULDER)
        val lElbow = point(landmarks, Landmark.L_ELBOW)
        val rElbow = point(landmarks, Landmark.R_ELBOW)
        val lWrist = point(landmarks, Landmark.L_WRIST)
        val rWrist = point(landmarks, Landmark.R_WRIST)
        val lIndex = point(landmarks, Landmark.L_INDEX)
        val rIndex = point(landmarks, Landmark.R_INDEX)
        val lHip = point(landmarks, Landmark.L_HIP)
        val rHip = point(landmarks, Landmark.R_HIP)
        val lKnee = point(landmarks, Landmark.L_KNEE)
        val rKnee = point(landmarks, Landmark.R_KNEE)
        val lAnkle = point(landmarks, Landmark.L_ANKLE)
        val rAnkle = point(landmarks, Landmark.R_ANKLE)
        val nose = point(landmarks, Landmark.NOSE)

        // Elbow bend angles
        val leftElbowAngle = calculateAngle(lShoulder, lElbow, lWrist)
        val rightElbowAngle = calculateAngle(rShoulder, rElbow, rWrist)

        // Knee bend angles
        val leftKneeAngle = calculateAngle(lHip, lKnee, lAnkle)
        val rightKneeAngle = calculateAngle(rHip, rKnee, rAnkle)

        // Wrist angles (elbow-wrist-index) - proxy for wrist flex
        val rightWristAngle = calculateAngle(rElbow, rWrist, rIndex)
        val leftWristAngle = calculateAngle(lElbow, lWrist, lIndex)

        // Shoulder orientation as raw vector components (NOT an angle - this avoids
        // the +/-180 wraparound problem the Python version hit and fixed the same way)
        var shoulderDx = rShoulder.x - lShoulder.x
        var shoulderDy = rShoulder.y - lShoulder.y

        // Torso size - used to normalize several features so they're scale-invariant.
        // More stable than shoulder width across different facing directions.
        val shoulderMid = Point((lShoulder.x + rShoulder.x) / 2, (lShoulder.y + rShoulder.y) / 2)
        val hipMid = Point((lHip.x + rHip.x) / 2, (lHip.y + rHip.y) / 2)
        val torsoSize = max(distance(shoulderMid, hipMid), 0.05f)

        shoulderDx /= torsoSize
        shoulderDy /= torsoSize

        // Stance width: distance between ankles, normalized
        val stanceWidth = distance(lAnkle, rAnkle) / torsoSize

        // Which foot is forward
        val footStagger = (lAnkle.x - rAnkle.x) / torsoSize

        // Horizontal hip-center position
        val hipCenterX = (lHip.x + rHip.x) / 2

        // Head position relative to hip center, normalized
        val headOffset = (nose.x - hipCenterX) / torsoSize

        // Grip-related features
        val wristSeparation = distance(lWrist, rWrist) / torsoSize
        val handHeightDiff = (rWrist.y - lWrist.y) / torsoSize

        return mapOf(
            "left_elbow_angle" to leftElbowAngle,
            "right_elbow_angle" to rightElbowAngle,
            "left_knee_angle" to leftKneeAngle,
            "right_knee_angle" to rightKneeAngle,
            "right_wrist_angle" to rightWristAngle,
            "left_wrist_angle" to leftWristAngle,
            "shoulder_dx" to shoulderDx,
            "shoulder_dy" to shoulderDy,
            "stance_width" to stanceWidth,
            "foot_stagger" to footStagger,
            "hip_center_x" to hipCenterX,
            "head_offset" to headOffset,
            "wrist_separation" to wristSeparation,
            "hand_height_diff" to handHeightDiff
        )
    }
}