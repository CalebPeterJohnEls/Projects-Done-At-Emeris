package com.example.funinactionproto

data class KeyPoint(
    val x: Float,
    val y: Float,
    val z: Float = 0f,
    val visibility: Float = 1f
)

data class PoseSkeleton(
    val landmarks: List<KeyPoint> // ideally 33 in same order as MediaPipe
)
