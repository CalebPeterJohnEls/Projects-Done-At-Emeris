package com.example.funinactionproto.pose


import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

object HandLandmark {
    const val WRIST = 0
    const val THUMB_TIP = 4
    const val INDEX_MCP = 5; const val INDEX_PIP = 6; const val INDEX_TIP = 8
    const val MIDDLE_MCP = 9; const val MIDDLE_PIP = 10; const val MIDDLE_TIP = 12
    const val RING_MCP = 13; const val RING_PIP = 14; const val RING_TIP = 16
    const val PINKY_MCP = 17; const val PINKY_PIP = 18; const val PINKY_TIP = 20
}

data class HandFeatures(val fingerCurl: Float, val aperture: Float)

object HandFeatureCalculator {

    fun computeOneHandFeatures(landmarks: List<NormalizedLandmark>): HandFeatures {
        val wrist = PoseFeatureCalculator.point(landmarks, HandLandmark.WRIST)
        val middleMcp = PoseFeatureCalculator.point(landmarks, HandLandmark.MIDDLE_MCP)
        val handSize = maxOf(PoseFeatureCalculator.distance(wrist, middleMcp), 0.02f)

        val fingerJoints = listOf(
            Triple(HandLandmark.INDEX_MCP, HandLandmark.INDEX_PIP, HandLandmark.INDEX_TIP),
            Triple(HandLandmark.MIDDLE_MCP, HandLandmark.MIDDLE_PIP, HandLandmark.MIDDLE_TIP),
            Triple(HandLandmark.RING_MCP, HandLandmark.RING_PIP, HandLandmark.RING_TIP),
            Triple(HandLandmark.PINKY_MCP, HandLandmark.PINKY_PIP, HandLandmark.PINKY_TIP)
        )
        val curls = fingerJoints.map { (mcpI, pipI, tipI) ->
            val mcp = PoseFeatureCalculator.point(landmarks, mcpI)
            val pip = PoseFeatureCalculator.point(landmarks, pipI)
            val tip = PoseFeatureCalculator.point(landmarks, tipI)
            PoseFeatureCalculator.calculateAngle(mcp, pip, tip)
        }
        val avgFingerCurl = curls.average().toFloat()

        val fingertipIndices = listOf(
            HandLandmark.THUMB_TIP, HandLandmark.INDEX_TIP, HandLandmark.MIDDLE_TIP,
            HandLandmark.RING_TIP, HandLandmark.PINKY_TIP
        )
        val tipDistances = fingertipIndices.map {
            PoseFeatureCalculator.distance(wrist, PoseFeatureCalculator.point(landmarks, it))
        }
        val aperture = (tipDistances.average().toFloat()) / handSize

        return HandFeatures(fingerCurl = avgFingerCurl, aperture = aperture)
    }


    fun computeHandFeatureMap(
        handLandmarksList: List<List<NormalizedLandmark>>,
        handednessList: List<List<Category>>
    ): Map<String, Float> {
        val result = mutableMapOf<String, Float>()
        for (i in handLandmarksList.indices) {
            val label = handednessList[i].firstOrNull()?.categoryName() ?: continue
            val side = if (label == "Left") "left" else "right"
            val features = computeOneHandFeatures(handLandmarksList[i])
            result["${side}_hand_finger_curl"] = features.fingerCurl
            result["${side}_hand_aperture"] = features.aperture
        }
        return result
    }
}