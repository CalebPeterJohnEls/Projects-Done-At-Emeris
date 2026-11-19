package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

private data class ChallengeInfo(
    val title: String,
    val subtitle: String,
    val steps: List<String>
)

/** The rules shown before each challenge. Edit the wording here. */
private val CHALLENGE_INFO = mapOf(
    "grip" to ChallengeInfo(
        title = "Challenge 1: Grip",
        subtitle = "Show us how you hold the stick. You'll be marked from 1 to 7, just like a school report card.",
        steps = listOf(
            "Stand back from the phone so your head, arms and hips are all in view.",
            "Hold your hockey stick in front of you with both hands.",
            "Left hand at the very top of the stick.",
            "Right hand about one third of the way down the stick.",
            "Wrap your fingers firmly around the stick - a closed grip, not open fingers.",
            "Keep both hands visible to the camera.",
            "Tap \"Take my grip photo\". After the countdown, hold still until it says your grip is scored."
        )
    ),
    "push_pass" to ChallengeInfo(
        title = "Challenge 2: Push Pass",
        subtitle = "Three passes, one mark out of 7. Your three marks are averaged.",
        steps = listOf(
            "Stand far enough back that the camera sees your whole body, head to feet.",
            "Stand side-on to your target, with the ball (or an imaginary ball) close to your right foot and your stick resting on it.",
            "Lunge forward, stepping your left foot out toward the target.",
            "Push the ball forward, keeping your stick on it, and flex your right wrist at the release like a throw.",
            "Follow through low and around your body.",
        )
    )
)

/** Splash screen with scrollable rules; one fragment reused for every challenge. */
class ChallengeRulesFragment : Fragment(R.layout.fragment_challenge_rules) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val skillId = arguments?.getString(ChallengeFlow.ARG_SKILL_ID) ?: "grip"
        val info = CHALLENGE_INFO.getValue(skillId)

        view.findViewById<TextView>(R.id.rulesTitle).text = info.title
        view.findViewById<TextView>(R.id.rulesSubtitle).text = info.subtitle

        val container = view.findViewById<LinearLayout>(R.id.stepsContainer)
        info.steps.forEachIndexed { index, step ->
            container.addView(buildStep(index + 1, step))
        }

        view.findViewById<Button>(R.id.rulesReadyBtn).setOnClickListener {
            ChallengeFlow.toCapture(requireActivity(), skillId)
        }
    }

    private fun buildStep(number: Int, text: String): TextView {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        return TextView(requireContext()).apply {
            this.text = "$number.  $text"
            textSize = 17f
            setTextColor(resources.getColor(R.color.text_dark, null))
            setBackgroundColor(resources.getColor(R.color.card_white, null))
            setPadding(dp(16), dp(14), dp(16), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
        }
    }
}