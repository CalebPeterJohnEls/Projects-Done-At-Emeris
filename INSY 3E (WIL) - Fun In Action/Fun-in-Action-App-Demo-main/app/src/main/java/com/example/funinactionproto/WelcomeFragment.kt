package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGetStarted = view.findViewById<Button>(R.id.btnGetStarted)
        val btnHaveAccount = view.findViewById<Button>(R.id.btnHaveAccount)
        val btnDemo = view.findViewById<Button>(R.id.btnDemo)

        // New users go through the tutorial first
        btnGetStarted.setOnClickListener {
            (activity as MainActivity).replaceFragment(TutorialFragment())
        }

        // Returning users skip straight to login
        btnHaveAccount.setOnClickListener {
            (activity as MainActivity).replaceFragment(LoginFragment())
        }

        // Demo users go straight to the demo challenge
        btnDemo.setOnClickListener {
            ChallengeFlow.signInAsDemo(requireActivity())
        }
    }
}