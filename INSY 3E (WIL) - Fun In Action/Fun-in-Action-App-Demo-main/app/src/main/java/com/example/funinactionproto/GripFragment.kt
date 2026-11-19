package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class GripFragment : Fragment(R.layout.fragment_grip) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        val backButton =
            view.findViewById<Button>(
                R.id.backButton
            )

        backButton.setOnClickListener {
            requireActivity()
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
                "grip"
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

