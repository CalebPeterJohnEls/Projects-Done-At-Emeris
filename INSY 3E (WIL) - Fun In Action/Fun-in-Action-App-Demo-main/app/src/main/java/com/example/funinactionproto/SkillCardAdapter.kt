package com.example.funinactionproto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.funinactionproto.pose.SkillConfig

class SkillCardAdapter(
    private val context: Context,
    private val skills: List<SkillConfig>,
    private val onSkillClicked: (SkillConfig) -> Unit
) : RecyclerView.Adapter<SkillCardAdapter.SkillViewHolder>() {

    class SkillViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val name: TextView =
            view.findViewById(R.id.skillName)

        val description: TextView =
            view.findViewById(R.id.skillDescription)

        val progressText: TextView =
            view.findViewById(R.id.skillProgressText)

        val progress: ProgressBar =
            view.findViewById(R.id.skillProgress)

        val button: Button =
            view.findViewById(R.id.startSkillButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SkillViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_skill_card,
                parent,
                false
            )

        return SkillViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SkillViewHolder,
        position: Int
    ) {

        val skill = skills[position]

        val progress =
            ProgressStore.getSkillProgress(
                context,
                skill.skillId
            )

        holder.name.text =
            skill.displayName

        holder.description.text =
            when (skill.skillId) {

                "grip" ->
                    "Improve your hand position and stick control."

                "push_pass" ->
                    "Practise your passing movement and release."

                else ->
                    "Improve your hockey technique."
            }

        holder.progress.progress =
            progress.averageScore.toInt()

        holder.progressText.text =
            if (progress.attempts == 0) {
                "Not attempted yet"
            } else {
                "${progress.averageScore.toInt()}% average • " +
                        "${progress.attempts} attempt" +
                        if (progress.attempts == 1) "" else "s"
            }

        holder.button.setOnClickListener {
            onSkillClicked(skill)
        }

        holder.itemView.setOnClickListener {
            onSkillClicked(skill)
        }
    }

    override fun getItemCount(): Int =
        skills.size
}