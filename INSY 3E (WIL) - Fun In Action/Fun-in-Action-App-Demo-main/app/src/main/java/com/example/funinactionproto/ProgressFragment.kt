package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.funinactionproto.pose.Skills

class ProgressFragment :
    Fragment(R.layout.fragment_progress) {

    private lateinit var levelText: TextView
    private lateinit var xpText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        levelText =
            view.findViewById(
                R.id.progressLevelText
            )

        xpText =
            view.findViewById(
                R.id.progressXpText
            )

        attemptsText =
            view.findViewById(
                R.id.totalAttemptsText
            )

        recyclerView =
            view.findViewById(
                R.id.progressRecyclerView
            )

        recyclerView.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        loadProgress()
    }

    override fun onResume() {
        super.onResume()

        if (::recyclerView.isInitialized) {
            loadProgress()
        }
    }

    private fun loadProgress() {

        val context =
            requireContext()

        val level =
            ProgressStore.getPlayerLevel(
                context
            )

        levelText.text =
            "★ Level ${level.level}"

        xpText.text =
            "${level.xp} total XP"

        attemptsText.text =
            "${ProgressStore.getTotalAttempts(context)} " +
                    "completed practice attempts"

        val progressMap =
            Skills.ALL.associate {
                it.skillId to
                        ProgressStore.getSkillProgress(
                            context,
                            it.skillId
                        )
            }

        recyclerView.adapter =
            ProgressSkillAdapter(
                skills = Skills.ALL,
                progress = progressMap
            )
    }
}