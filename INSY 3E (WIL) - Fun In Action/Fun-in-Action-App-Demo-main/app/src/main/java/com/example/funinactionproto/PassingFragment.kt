package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class PassingFragment : Fragment(R.layout.fragment_passing) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity =
            requireActivity() as MainActivity

        val backButton =
            view.findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            mainActivity
                .supportFragmentManager
                .popBackStack()
        }

        val skillContent =
            view.findViewById<LinearLayout>(
                R.id.skillContent
            )

        val skill =
            SkillRepository.getSkill(
                requireContext(),
                "passing"
            )

        if (skill != null) {
            SkillRenderer.render(
                requireContext(),
                skillContent,
                skill
            )
        }
    }
}