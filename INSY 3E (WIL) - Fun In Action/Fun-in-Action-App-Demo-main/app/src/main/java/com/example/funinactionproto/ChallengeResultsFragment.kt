package com.example.funinactionproto

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.funinactionproto.pose.Achievement
import com.example.funinactionproto.pose.Skills

/** Report card: each challenge's mark, the overall average, and posts it to the leaderboard. */
class ChallengeResultsFragment : Fragment(R.layout.fragment_challenge_results) {

    private lateinit var statusText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var offlineNotice: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusText = view.findViewById(R.id.statusText)
        view.findViewById<TextView>(R.id.nicknameText).text = ChallengeSession.nickname

        view.findViewById<Button>(R.id.leaderboardBtn).setOnClickListener {
            ChallengeFlow.toLeaderboard(requireActivity())
        }
        view.findViewById<Button>(R.id.finishBtn).setOnClickListener {
            ChallengeFlow.backToDemoHome(requireActivity())
        }

        val grip = ChallengeSession.results["grip"]
        val pushPass = ChallengeSession.results["push_pass"]
        val average = ChallengeSession.average()

        if (grip == null || pushPass == null || average == null) {
            statusText.text = "Some scores are missing - please run the challenge again."
            return
        }

        bindRow(view.findViewById(R.id.gripRow), Skills.byId("grip").displayName, grip)
        bindRow(view.findViewById(R.id.pushPassRow), Skills.byId("push_pass").displayName, pushPass)
        bindRow(view.findViewById(R.id.overallRow), "Overall average", average)

        postScore(grip, pushPass, average)
    }

    private fun bindRow(row: View, title: String, achievement: Achievement) {
        row.findViewById<TextView>(R.id.rowTitle).text = title
        row.findViewById<TextView>(R.id.rowDesc).text = achievement.level.descriptor
        row.findViewById<TextView>(R.id.rowLevel).text = "${achievement.level.level}/7"
        row.findViewById<TextView>(R.id.rowPercent).text = "${achievement.percent}%"
    }

    private fun postScore(grip: Achievement, pushPass: Achievement, average: Achievement) {
        // Coming back from the leaderboard re-creates this screen - don't post twice
        if (ChallengeSession.submittedDocId != null) {
            statusText.text = "You're on the leaderboard!"
            return
        }

        statusText.text = "Posting to the leaderboard..."
        statusText.setOnClickListener(null)

        // Firestore queues writes while offline and sends them later, so a slow
        // connection looks like "still posting". Tell the player what's happening.
        val notice = Runnable {
            if (isAdded && ChallengeSession.submittedDocId == null) {
                statusText.text = "No connection right now - your score will post as soon as you're back online."
            }
        }
        offlineNotice = notice
        handler.postDelayed(notice, 6000L)

        LeaderboardRepository.submit(
            ChallengeSession.nickname,
            average
        ) { result ->
            handler.removeCallbacks(notice)
            result
                .onSuccess { id ->
                    ChallengeSession.submittedDocId = id
                    if (isAdded) statusText.text = "You're on the leaderboard!"
                }
                .onFailure { e ->
                    if (isAdded) {
                        statusText.text = "Couldn't post your score (${e.message}). Tap here to retry."
                        statusText.setOnClickListener { postScore(grip, pushPass, average) }
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        offlineNotice?.let { handler.removeCallbacks(it) }
        offlineNotice = null
    }
}