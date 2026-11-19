package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.funinactionproto.pose.CaptureMode
import com.example.funinactionproto.pose.Skills

class PracticeFragment :
    Fragment(R.layout.fragment_practice) {

    private lateinit var skillsRecyclerView: RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        skillsRecyclerView =
            view.findViewById(
                R.id.skillsRecyclerView
            )


        skillsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        setupSkillCards()
    }

    private fun setupSkillCards() {

        skillsRecyclerView.adapter =
            SkillCardAdapter(
                context = requireContext(),
                skills = Skills.ALL
            ) { skill ->

                onSkillClicked(skill)
            }
    }

    private fun onSkillClicked(
        skill: com.example.funinactionproto.pose.SkillConfig
    ) {

        val args = Bundle().apply {
            putString(
                "skillId",
                skill.skillId
            )
        }

        when (skill.captureMode) {

            CaptureMode.PHOTO -> {

                (requireActivity() as MainActivity)
                    .replaceFragment(
                        GripCaptureFragment().apply {
                            arguments = args
                        },
                        true
                    )
            }

            CaptureMode.VIDEO_AUTO_DETECT -> {

                (requireActivity() as MainActivity)
                    .replaceFragment(
                        VideoCaptureFragment().apply {
                            arguments = args
                        },
                        true
                    )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        /*
         * Rebind the adapter so that when the user returns
         * from a practice session, the new score appears.
         */
        if (::skillsRecyclerView.isInitialized) {
            setupSkillCards()
        }
    }
}