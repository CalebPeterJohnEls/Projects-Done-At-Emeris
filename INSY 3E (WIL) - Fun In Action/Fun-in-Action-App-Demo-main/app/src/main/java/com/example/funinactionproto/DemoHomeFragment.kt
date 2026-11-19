package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

/** Landing page after "Sign in as Demo": start the challenge or look at the leaderboard. */
class DemoHomeFragment : Fragment(R.layout.fragment_demo_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.startChallengeBtn).setOnClickListener {
            ChallengeSession.reset()
            ChallengeFlow.toNickname(requireActivity())
        }

        view.findViewById<Button>(R.id.viewLeaderboardBtn).setOnClickListener {
            ChallengeFlow.toLeaderboard(requireActivity())
        }
    }
}