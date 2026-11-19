package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.funinactionproto.api.SessionManager
import com.example.funinactionproto.pose.Skills

class HomeFragment :
    Fragment(R.layout.fragment_home) {

    private lateinit var greeting: TextView
    private lateinit var avatar: TextView

    private lateinit var levelText: TextView
    private lateinit var levelXpText: TextView
    private lateinit var levelProgress: ProgressBar

    private lateinit var gripText: TextView
    private lateinit var gripProgress: ProgressBar

    private lateinit var passText: TextView
    private lateinit var passProgress: ProgressBar

    private lateinit var recentPracticeText: TextView
    private lateinit var certificateText: TextView
    private lateinit var certificateProgress: ProgressBar

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        greeting =
            view.findViewById(R.id.homeGreeting)

        avatar =
            view.findViewById(R.id.homeAvatar)

        levelText =
            view.findViewById(R.id.levelText)

        levelXpText =
            view.findViewById(R.id.levelXpText)

        levelProgress =
            view.findViewById(R.id.levelProgress)

        gripText =
            view.findViewById(R.id.gripProgressText)

        gripProgress =
            view.findViewById(R.id.gripProgress)

        passText =
            view.findViewById(R.id.passProgressText)

        passProgress =
            view.findViewById(R.id.passProgress)

        recentPracticeText =
            view.findViewById(R.id.recentPracticeText)

        certificateText =
            view.findViewById(R.id.certificateText)

        certificateProgress =
            view.findViewById(R.id.certificateProgress)

        view.findViewById<Button>(
            R.id.btnPracticeStance
        ).setOnClickListener {

            (activity as MainActivity)
                .replaceFragment(
                    PracticeFragment()
                )
        }

        loadHomeData()
    }

    override fun onResume() {
        super.onResume()

        if (::levelText.isInitialized) {
            loadHomeData()
        }
    }

    private fun loadHomeData() {

        val context = requireContext()

        /*
         * Use the actual logged-in user's name
         * instead of hard-coded "Caleb".
         */
        val session =
            SessionManager(context)

        val fullName =
            session.getFullName()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "Player"

        greeting.text =
            "Hi, ${fullName.split(" ").first()}!"

        avatar.text =
            fullName
                .firstOrNull()
                ?.uppercase()
                ?: "P"

        /*
         * LEVEL
         */
        val level =
            ProgressStore.getPlayerLevel(context)

        levelText.text =
            "★ Level ${level.level}"

        levelXpText.text =
            "${level.xpIntoLevel} / " +
                    "${level.xpRequiredForNextLevel} XP"

        levelProgress.max =
            level.xpRequiredForNextLevel

        levelProgress.progress =
            level.xpIntoLevel

        /*
         * GRIP
         */
        val grip =
            ProgressStore.getSkillProgress(
                context,
                "grip"
            )

        gripProgress.progress =
            grip.averageScore.toInt()

        gripText.text =
            if (grip.attempts == 0) {
                "Not attempted yet"
            } else {
                "${grip.averageScore.toInt()}% • " +
                        "${grip.attempts} attempts"
            }

        /*
         * PUSH PASS
         */
        val pushPass =
            ProgressStore.getSkillProgress(
                context,
                "push_pass"
            )

        passProgress.progress =
            pushPass.averageScore.toInt()

        passText.text =
            if (pushPass.attempts == 0) {
                "Not attempted yet"
            } else {
                "${pushPass.averageScore.toInt()}% • " +
                        "${pushPass.attempts} attempts"
            }

        /*
         * RECENT PRACTICE
         */
        val attempts =
            ProgressStore.getAttempts(context)

        val latest =
            attempts.maxByOrNull {
                it.timestamp
            }

        if (latest == null) {

            recentPracticeText.text =
                "No practice completed yet.\n" +
                        "Start your first skill above!"

        } else {

            val skill =
                Skills.byId(
                    latest.skillId
                )

            recentPracticeText.text =
                "${skill.displayName}\n" +
                        "${latest.score.toInt()}% score"
        }

        /*
         * CERTIFICATE
         *
         * Four completed attempts are used
         * as the unlock target.
         */
        val totalAttempts =
            ProgressStore.getTotalAttempts(context)

        val completed =
            totalAttempts.coerceAtMost(4)

        certificateProgress.max = 4
        certificateProgress.progress = completed

        val remaining =
            (4 - completed).coerceAtLeast(0)

        certificateText.text =
            if (remaining == 0) {
                "Certificate unlocked! 🎉"
            } else {
                "$remaining more practice " +
                        "attempt" +
                        if (remaining == 1) "" else "s" +
                                " to unlock your certificate."
            }
    }
}
