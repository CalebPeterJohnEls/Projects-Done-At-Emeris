package com.example.funinactionproto

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Short delay before moving on to the welcome screen
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (isAdded) {
                (activity as MainActivity).replaceFragment(WelcomeFragment())
            }
        }, 1600)
    }
}