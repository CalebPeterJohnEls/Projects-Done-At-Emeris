package com.example.funinactionproto

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ListenerRegistration

/**
 * Live top-20 leaderboard, shared by every device running the demo.
 */
class LeaderboardFragment : Fragment(R.layout.fragment_leaderboard) {

    private val adapter = LeaderboardAdapter()
    private var registration: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusText =
            view.findViewById<TextView>(R.id.leaderboardStatus)

        val list =
            view.findViewById<RecyclerView>(R.id.leaderboardList)

        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        view.findViewById<Button>(R.id.leaderboardBackBtn)
            .setOnClickListener {
                parentFragmentManager.popBackStack()
            }

        statusText.text = "Loading..."

        registration = LeaderboardRepository.listenTop(
            limit = 20,

            onUpdate = { entries ->
                adapter.submit(entries)

                statusText.text =
                    if (entries.isEmpty()) {
                        "No scores yet - be the first!"
                    } else {
                        "Live - top ${entries.size}"
                    }
            },

            onError = { error ->
                statusText.text =
                    "Couldn't load the leaderboard: ${error.message}"
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        registration?.remove()
        registration = null
    }
}

/**
 * Displays the leaderboard rows.
 */
private class LeaderboardAdapter :
    RecyclerView.Adapter<LeaderboardAdapter.Holder>() {

    private var entries: List<LeaderboardEntry> = emptyList()

    fun submit(newEntries: List<LeaderboardEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {

        val rank: TextView =
            view.findViewById(R.id.rankText)

        val name: TextView =
            view.findViewById(R.id.nameText)

        val score: TextView =
            view.findViewById(R.id.scoreText)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int = entries.size

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val entry = entries[position]

        holder.rank.text = "${position + 1}"
        holder.name.text = entry.nickname
        holder.score.text = "${entry.averageLevel}/7"

        // Highlight the player who submitted on this device.
        val isMine = entry.id == ChallengeSession.submittedDocId

        holder.itemView.setBackgroundColor(
            if (isMine) {
                Color.parseColor("#442EC4B6")
            } else {
                Color.TRANSPARENT
            }
        )
    }
}