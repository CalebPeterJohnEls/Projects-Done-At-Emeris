package com.example.funinactionproto.pose

import android.content.Context
import org.json.JSONObject
import kotlin.math.abs

/** One coaching point's feedback for a scored attempt. */
data class CoachingFeedback(
    val label: String,
    val needsWork: Boolean,
    val deviationScore: Float,
    val achievement: Achievement?      // 1-7 level + percentage for this coaching point
)

/** Full scoring result for one attempt. */
data class ScoreResult(
    val skillId: String,
    val overallScore: Float,           // raw average deviation (lower = closer to correct)
    val coachingFeedback: List<CoachingFeedback>,
    val overall: Achievement?          // the aggregated 1-7 level + percentage
)

/**
 * Loads a skill's reference JSON from assets and scores an aggregated attempt
 * (from RepAggregator) against it, producing both the raw deviations and the
 * 1-7 achievement levels.
 */
object ReferenceScorer {

    private const val FLAG_THRESHOLD = 1.5f

    private fun loadReferenceJson(context: Context, assetPath: String): JSONObject {
        val jsonText = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return JSONObject(jsonText)
    }

    fun score(context: Context, skillConfig: SkillConfig, aggregatedFeatures: Map<String, Float>): ScoreResult {
        val reference = loadReferenceJson(context, skillConfig.referenceAssetPath)
        val features = reference.getJSONObject("features")
        val coachingPoints = reference.getJSONObject("coaching_points")

        val zScores = mutableListOf<Float>()
        val coachingPercents = mutableListOf<Float>()
        val feedback = mutableListOf<CoachingFeedback>()

        val featureNames = features.keys()
        while (featureNames.hasNext()) {
            val featureName = featureNames.next()
            val value = aggregatedFeatures[featureName] ?: continue

            val featureRef = features.getJSONObject(featureName)
            val mean = featureRef.getDouble("mean").toFloat()
            val std = featureRef.getDouble("std").toFloat()
            val z = abs(value - mean) / std
            zScores.add(z)

            if (coachingPoints.has(featureName)) {
                coachingPercents.add(AchievementScale.percentFromDeviation(z))
                feedback.add(
                    CoachingFeedback(
                        label = coachingPoints.getString(featureName),
                        needsWork = z > FLAG_THRESHOLD,
                        deviationScore = z,
                        achievement = AchievementScale.fromDeviation(z)
                    )
                )
            }
        }

        val overallDeviation = if (zScores.isNotEmpty()) zScores.average().toFloat() else Float.NaN
        val overallAchievement = AchievementScale.aggregate(coachingPercents, overallDeviation)

        return ScoreResult(
            skillId = skillConfig.skillId,
            overallScore = overallDeviation,
            coachingFeedback = feedback,
            overall = overallAchievement
        )
    }
}