package com.example.betterbudget_poe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class SplashScreen : Fragment(R.layout.fragment_splash_screen) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)


        //handler creates a delay of 2 seconds before moving to the next screen
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            (activity as MainActivity).replaceFragment(AuthFragment())},
            2000)
    }
}