package com.example.funinactionproto

data class PracticeAttempt(
    val skillId: String,
    val score: Float,
    val timestamp: Long
)

data class SkillProgress(
    val skillId: String,
    val attempts: Int,
    val averageScore: Float,
    val bestScore: Float
)

data class PlayerLevel(
    val level: Int,
    val xp: Int,
    val xpIntoLevel: Int,
    val xpRequiredForNextLevel: Int
)