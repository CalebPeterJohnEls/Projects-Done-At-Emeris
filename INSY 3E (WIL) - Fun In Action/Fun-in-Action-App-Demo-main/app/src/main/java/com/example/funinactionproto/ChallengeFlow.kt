package com.example.funinactionproto

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.funinactionproto.pose.Achievement
import com.example.funinactionproto.pose.AchievementScale
import com.example.funinactionproto.pose.CaptureMode
import com.example.funinactionproto.pose.Skills
import com.google.firebase.auth.FirebaseAuth

/**
 * Holds the current player's progress through the Hockey Report Card challenge.
 * In-memory only - it is cleared whenever a new challenge starts.
 */
object ChallengeSession {
    var nickname: String = ""

    /** skillId -> that skill's achievement (e.g. "grip" -> 6/7, 74%) */
    val results = mutableMapOf<String, Achievement>()

    /** Firestore document id once the score has been posted (stops double-posting). */
    var submittedDocId: String? = null

    fun reset() {
        nickname = ""
        results.clear()
        submittedDocId = null
    }

    /** Average of the challenge skills' percentages, or null if any score is missing. */
    fun average(): Achievement? {
        val all = ChallengeFlow.CHALLENGE_SKILLS.map { results[it] ?: return null }
        return AchievementScale.achievementFromPercent(all.map { it.percent }.average().toFloat())
    }
}

/**
 * All navigation for the challenge lives here so the individual fragments stay simple.
 * Order of the flow: demo home -> nickname -> rules(grip) -> grip capture ->
 * rules(push_pass) -> push pass capture -> results -> leaderboard.
 */
object ChallengeFlow {

    const val ARG_SKILL_ID = "skillId"
    const val ARG_CHALLENGE = "challengeMode"

    /** The skills in the challenge, in order. Add a skill id here to extend it. */
    val CHALLENGE_SKILLS = listOf("grip", "push_pass")

    private fun show(activity: FragmentActivity, fragment: Fragment, addToBackStack: Boolean = false) {
        (activity as MainActivity).replaceFragment(fragment, addToBackStack = addToBackStack)
    }

    /** Called by the "Sign in as Demo" button on the get-started page. */
    fun signInAsDemo(activity: FragmentActivity) {
        ChallengeSession.reset()
        // Fire-and-forget: the demo works offline, and LeaderboardRepository.submit()
        // signs in again if it still needs to.
        FirebaseAuth.getInstance().signInAnonymously()
        show(activity, DemoHomeFragment(), addToBackStack = true)
    }

    fun toNickname(activity: FragmentActivity) = show(activity, ChallengeNicknameFragment())

    fun toRules(activity: FragmentActivity, skillId: String) {
        val fragment = ChallengeRulesFragment().apply {
            arguments = Bundle().apply { putString(ARG_SKILL_ID, skillId) }
        }
        show(activity, fragment)
    }

    fun toCapture(activity: FragmentActivity, skillId: String) {
        val fragment: Fragment =
            if (Skills.byId(skillId).captureMode == CaptureMode.PHOTO) GripCaptureFragment()
            else VideoCaptureFragment()
        fragment.arguments = Bundle().apply {
            putString(ARG_SKILL_ID, skillId)
            putBoolean(ARG_CHALLENGE, true)
        }
        show(activity, fragment)
    }

    /** Capture screens call this when their skill is scored: go to the next rules page, or the results. */
    fun onSkillFinished(activity: FragmentActivity, skillId: String) {
        val next = CHALLENGE_SKILLS.getOrNull(CHALLENGE_SKILLS.indexOf(skillId) + 1)
        if (next != null) toRules(activity, next) else toResults(activity)
    }

    fun toResults(activity: FragmentActivity) = show(activity, ChallengeResultsFragment())

    fun toLeaderboard(activity: FragmentActivity) =
        show(activity, LeaderboardFragment(), addToBackStack = true)

    fun backToDemoHome(activity: FragmentActivity) {
        ChallengeSession.reset()
        show(activity, DemoHomeFragment())
    }
}