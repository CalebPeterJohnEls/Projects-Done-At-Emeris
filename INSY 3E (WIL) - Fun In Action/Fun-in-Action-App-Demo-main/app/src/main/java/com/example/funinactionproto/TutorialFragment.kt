package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class TutorialFragment : Fragment(R.layout.fragment_tutorial) {

    private val pages = buildTutorialPages()
    private lateinit var pager: ViewPager2
    private lateinit var dots: List<View>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager = view.findViewById(R.id.tutorialPager)
        val btnNext = view.findViewById<Button>(R.id.btnTutNext)
        val btnSkip = view.findViewById<TextView>(R.id.btnTutSkip)

        dots = listOf(
            view.findViewById(R.id.dot0), view.findViewById(R.id.dot1),
            view.findViewById(R.id.dot2), view.findViewById(R.id.dot3),
            view.findViewById(R.id.dot4)
        )

        pager.adapter = TutorialPagerAdapter(requireActivity(), pages)

        //Keeps the dots and the Next/Get Started label in sync, whether the
        //user swipes or taps the button
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                btnNext.text = if (position == pages.lastIndex) "Get Started" else "Next"
            }
        })

        btnSkip.setOnClickListener {
            (activity as MainActivity).replaceFragment(LoginFragment())
        }

        btnNext.setOnClickListener {
            if (pager.currentItem < pages.lastIndex) {
                pager.currentItem += 1
            } else {
                (activity as MainActivity).replaceFragment(LoginFragment())
            }
        }

        updateDots(0)
    }


    private fun updateDots(activeIndex: Int) {
        val activeWidth = (20 * resources.displayMetrics.density).toInt()
        val inactiveWidth = (6 * resources.displayMetrics.density).toInt()

        dots.forEachIndexed { index, dot ->
            val isActive = index == activeIndex
            dot.setBackgroundResource(
                if (isActive) R.drawable.bg_button_pill_gradient else R.drawable.bg_chip_unselected
            )
            dot.layoutParams = dot.layoutParams.apply {
                width = if (isActive) activeWidth else inactiveWidth
            }
            dot.requestLayout()
        }
    }
}

// Reference:
// Android Developers (2026) Slide between fragments using ViewPager2. Available at:
// https://developer.android.com/training/animation/screen-slide-2 (Accessed: 16 August 2026).
