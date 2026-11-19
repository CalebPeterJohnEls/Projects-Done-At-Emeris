package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class MenuFragment : Fragment(R.layout.fragment_menu) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity =
            requireActivity() as MainActivity

        // Back to Learn
        val backButton =
            view.findViewById<View>(R.id.backButton)

        backButton.setOnClickListener {
            requireActivity()
                .supportFragmentManager
                .popBackStack()
        }

        // Grip
        val cardGrip =
            view.findViewById<View>(R.id.cardGrip)

        cardGrip.setOnClickListener {
            mainActivity.replaceFragment(
                GripFragment(),
                true
            )
        }

        // Running with the ball
        val cardRunning =
            view.findViewById<View>(R.id.cardRunning)

        cardRunning.setOnClickListener {
            mainActivity.replaceFragment(
                RunningFragment(),
                true
            )
        }

        // Passing
        val cardPassing =
            view.findViewById<View>(R.id.cardPassing)

        cardPassing.setOnClickListener {
            mainActivity.replaceFragment(
                PassingFragment(),
                true
            )
        }

        // Receiving
        val cardReceiving =
            view.findViewById<View>(R.id.cardReceiving)

        cardReceiving.setOnClickListener {
            mainActivity.replaceFragment(
                RecevingFragment(),
                true
            )
        }
    }
}