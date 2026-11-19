package com.example.funinactionproto.pose

import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * The 7-point achievement scale used on South African school report cards (CAPS).
 * Level boundaries are percentage bands; the descriptors are the standard wording
 * printed on report cards.
 *
 * Declared highest-first so fromPercent() can just take the first band the
 * percentage clears.
 */
enum class AchievementLevel(val level: Int, val descriptor: String, val minPercent: Int) {
    OUTSTANDING(7, "Outstanding achievement", 80),
    MERITORIOUS(6, "Meritorious achievement", 70),
    SUBSTANTIAL(5, "Substantial achievement", 60),
    ADEQUATE(4, "Adequate achievement", 50),
    MODERATE(3, "Moderate achievement", 40),
    ELEMENTARY(2, "Elementary achievement", 30),
    NOT_ACHIEVED(1, "Not achieved", 0);

    companion object {
        fun fromPercent(percent: Int): AchievementLevel =
            values().first { percent >= it.minPercent }
    }
}

/** A percentage plus the 1-7 level that percentage falls into. */
data class Achievement(val percent: Int, val level: AchievementLevel)

object AchievementScale {

    /**
     * Controls how quickly the percentage falls as deviation from the reference grows.
     * This is THE tuning knob for the whole scale:
     *   - raise it  -> marking gets stricter (a given deviation scores lower)
     *   - lower it  -> marking gets more forgiving
     *
     * At 0.3:  deviation 0.5 -> 86%, 1.0 -> 74%, 1.5 -> 64%, 2.0 -> 55%,
     *          3.0 -> 41%, 4.0 -> 30%, 5.0 -> 22%
     * Calibrate it once you have more reference reps, by checking what deviation a
     * genuinely good attempt produces and making sure that lands in the 70-80%+ range.
     */
    private const val DECAY = 0.3f

    /** 0 deviation (matches the reference exactly) = 100%, falling smoothly from there. */
    fun percentFromDeviation(deviation: Float): Float =
        (100f * exp(-DECAY * deviation)).coerceIn(0f, 100f)

    fun achievementFromPercent(percent: Float): Achievement {
        val rounded = percent.roundToInt().coerceIn(0, 100)
        return Achievement(rounded, AchievementLevel.fromPercent(rounded))
    }

    /** For a single coaching point. Returns null if there was nothing to score. */
    fun fromDeviation(deviation: Float): Achievement? =
        if (deviation.isNaN()) null else achievementFromPercent(percentFromDeviation(deviation))

    /**
     * The overall result for an attempt. Averages the per-coaching-point PERCENTAGES
     * (not the raw deviations) so one very bad point drags the total down without
     * flattening it to zero, the way a single weak subject affects a report card.
     * Falls back to the all-feature deviation if the skill has no coaching points.
     */
    fun aggregate(coachingPercents: List<Float>, fallbackDeviation: Float): Achievement? {
        if (coachingPercents.isNotEmpty()) {
            return achievementFromPercent(coachingPercents.average().toFloat())
        }
        return fromDeviation(fallbackDeviation)
    }
}