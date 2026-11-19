package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class LearnFragment : Fragment(R.layout.fragment_learn) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity =
            requireActivity() as MainActivity

        // Back to Home
        val backButton =
            view.findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            mainActivity.replaceFragment(
                HomeFragment()
            )
        }

        // Grip
        view.findViewById<View>(R.id.cardGrip)
            .setOnClickListener {
                mainActivity.replaceFragment(
                    GripFragment(),
                    true
                )
            }

        // Running
        view.findViewById<View>(R.id.cardRunning)
            .setOnClickListener {
                mainActivity.replaceFragment(
                    RunningFragment(),
                    true
                )
            }

        // Passing
        view.findViewById<View>(R.id.cardPassing)
            .setOnClickListener {
                mainActivity.replaceFragment(
                    PassingFragment(),
                    true
                )
            }

        // Receiving
        view.findViewById<View>(R.id.cardReceiving)
            .setOnClickListener {
                mainActivity.replaceFragment(
                    RecevingFragment(),
                    true
                )
            }
    }
}