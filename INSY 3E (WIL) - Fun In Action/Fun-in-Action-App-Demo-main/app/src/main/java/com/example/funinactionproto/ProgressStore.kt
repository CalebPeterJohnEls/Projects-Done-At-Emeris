package com.example.funinactionproto

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

object ProgressStore {

    private const val PREFS_NAME = "fun_in_action_progress"
    private const val ATTEMPTS_KEY = "practice_attempts"

    private const val XP_PER_LEVEL = 200

    /**
     * Saves one completed practice attempt.
     */
    fun recordAttempt(
        context: Context,
        skillId: String,
        score: Float
    ) {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val attempts = JSONArray(
            prefs.getString(ATTEMPTS_KEY, "[]")
        )

        val safeScore = score.coerceIn(0f, 100f)

        val attempt = JSONObject().apply {
            put("skillId", skillId)
            put("score", safeScore)
            put("timestamp", System.currentTimeMillis())
        }

        attempts.put(attempt)

        prefs.edit()
            .putString(ATTEMPTS_KEY, attempts.toString())
            .apply()
    }

    /**
     * Returns all saved attempts.
     */
    fun getAttempts(
        context: Context
    ): List<PracticeAttempt> {

        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val json = JSONArray(
            prefs.getString(ATTEMPTS_KEY, "[]")
        )

        val attempts = mutableListOf<PracticeAttempt>()

        for (i in 0 until json.length()) {

            val item = json.getJSONObject(i)

            attempts.add(
                PracticeAttempt(
                    skillId = item.getString("skillId"),
                    score = item.getDouble("score").toFloat(),
                    timestamp = item.getLong("timestamp")
                )
            )
        }

        return attempts
    }

    /**
     * Returns progress for one skill.
     */
    fun getSkillProgress(
        context: Context,
        skillId: String
    ): SkillProgress {

        val scores = getAttempts(context)
            .filter { it.skillId == skillId }
            .map { it.score }

        if (scores.isEmpty()) {
            return SkillProgress(
                skillId = skillId,
                attempts = 0,
                averageScore = 0f,
                bestScore = 0f
            )
        }

        return SkillProgress(
            skillId = skillId,
            attempts = scores.size,
            averageScore = scores.average().toFloat(),
            bestScore = scores.maxOrNull() ?: 0f
        )
    }

    /**
     * Returns the player's progress for every configured skill.
     */
    fun getAllProgress(
        context: Context
    ): List<SkillProgress> {

        return com.example.funinactionproto.pose.Skills.ALL.map {
            getSkillProgress(
                context,
                it.skillId
            )
        }
    }

    /**
     * Every score contributes XP.
     *
     * Example:
     * 80% attempt = 80 XP
     * 65% attempt = 65 XP
     */
    fun getTotalXp(
        context: Context
    ): Int {

        return getAttempts(context)
            .sumOf {
                it.score.roundToInt()
            }
    }

    /**
     * Calculates the player's level.
     *
     * Level 1 = 0 - 199 XP
     * Level 2 = 200 - 399 XP
     * Level 3 = 400 - 599 XP
     * etc.
     */
    fun getPlayerLevel(
        context: Context
    ): PlayerLevel {

        val xp = getTotalXp(context)

        val level =
            (xp / XP_PER_LEVEL) + 1

        val xpIntoLevel =
            xp % XP_PER_LEVEL

        return PlayerLevel(
            level = level,
            xp = xp,
            xpIntoLevel = xpIntoLevel,
            xpRequiredForNextLevel = XP_PER_LEVEL
        )
    }

    /**
     * Number of completed practice attempts.
     */
    fun getTotalAttempts(
        context: Context
    ): Int {
        return getAttempts(context).size
    }

    /**
     * Converts the z-score used by ReferenceScorer
     * into a user-friendly percentage.
     *
     * Lower z-score = better technique.
     *
     * z = 0    -> 100%
     * z = 1    -> 50%
     * z = 2    -> 33%
     */
    fun zScoreToPercentage(
        zScore: Float
    ): Int {

        if (zScore.isNaN() || zScore.isInfinite()) {
            return 0
        }

        return (
                100f / (1f + zScore)
                )
            .coerceIn(0f, 100f)
            .roundToInt()
    }

    /**
     * Used while testing.
     *
     * This lets you completely remove all locally
     * saved progress from the app.
     */
    fun clearProgress(
        context: Context
    ) {

        context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(ATTEMPTS_KEY)
            .apply()
    }
}