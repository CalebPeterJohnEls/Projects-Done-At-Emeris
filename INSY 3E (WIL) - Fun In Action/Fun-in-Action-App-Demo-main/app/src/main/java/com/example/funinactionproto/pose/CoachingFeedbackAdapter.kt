package com.example.funinactionproto.pose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.funinactionproto.R

class CoachingFeedbackAdapter(
    private val items: List<CoachingFeedback>
) : RecyclerView.Adapter<CoachingFeedbackAdapter.VH>() {

    class VH(root: View) : RecyclerView.ViewHolder(root) {
        val label: TextView = root.findViewById(R.id.feedbackLabel)
        val status: TextView = root.findViewById(R.id.feedbackStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coaching_feedback, parent, false)
        return VH(root)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.label.text = item.label
        holder.status.text = if (item.needsWork) {
            "Needs work (z=%.2f)".format(item.deviationScore)
        } else {
            "OK (z=%.2f)".format(item.deviationScore)
        }
        holder.status.setTextColor(
            holder.status.context.getColor(
                if (item.needsWork) android.R.color.holo_red_dark
                else android.R.color.holo_green_dark
            )
        )
    }

    override fun getItemCount(): Int = items.size
}