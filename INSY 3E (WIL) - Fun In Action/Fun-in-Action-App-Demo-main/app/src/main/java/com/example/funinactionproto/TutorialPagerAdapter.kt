package com.example.funinactionproto

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TutorialPagerAdapter(
    activity: FragmentActivity,
    private val pages: List<TutorialPage>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment =
        TutorialSlideFragment.newInstance(pages[position])
}

// Reference:
// Android Developers (2026) Slide between fragments using ViewPager2. Available at:
// https://developer.android.com/training/animation/screen-slide-2 (Accessed: 16 August 2026).
