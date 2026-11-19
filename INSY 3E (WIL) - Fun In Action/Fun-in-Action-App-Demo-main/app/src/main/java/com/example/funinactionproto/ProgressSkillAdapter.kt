package com.example.funinactionproto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.funinactionproto.pose.SkillConfig

class ProgressSkillAdapter(
    private val skills: List<SkillConfig>,
    private val progress: Map<String, SkillProgress>
) : RecyclerView.Adapter<ProgressSkillAdapter.ViewHolder>() {

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val name: TextView =
            view.findViewById(
                R.id.progressSkillName
            )

        val percentage: TextView =
            view.findViewById(
                R.id.progressSkillPercentage
            )

        val bar: ProgressBar =
            view.findViewById(
                R.id.progressSkillBar
            )

        val details: TextView =
            view.findViewById(
                R.id.progressSkillDetails
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_progress_skill,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val skill =
            skills[position]

        val data =
            progress[skill.skillId]
                ?: SkillProgress(
                    skillId = skill.skillId,
                    attempts = 0,
                    averageScore = 0f,
                    bestScore = 0f
                )

        val average =
            data.averageScore.toInt()

        val best =
            data.bestScore.toInt()

        holder.name.text =
            skill.displayName

        holder.percentage.text =
            "$average%"

        holder.bar.max = 100

        holder.bar.progress =
            average

        holder.details.text =
            if (data.attempts == 0) {

                "Not attempted yet"

            } else {

                "${data.attempts} attempt" +
                        if (data.attempts == 1) "" else "s" +
                                " • Best: $best%"
            }
    }

    override fun getItemCount(): Int =
        skills.size
}