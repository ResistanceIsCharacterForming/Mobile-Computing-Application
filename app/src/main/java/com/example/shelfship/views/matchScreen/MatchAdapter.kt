package com.example.shelfship.views.matchScreen

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.databinding.MatchListItemBinding
import com.example.shelfship.models.Match
// Coil is used to dynamically load the images of matches.
import coil.load

// Adapter for the result of matchmaking. We need scope to launch a coroutine inside the parent fragment's activity.
class MatchAdapter(
    private val matchResultListener: MatchResultListener) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    // The data we want to display.
    private var matches: List<Match> = emptyList()

    interface MatchResultListener {
        fun sendRequest(match: Match)
    }

    fun updateMatches(newMatches: List<Match>) {
        matches = newMatches
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding =
            MatchListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.bindItem(match)
    }

    override fun getItemCount(): Int = matches.size

    inner class MatchViewHolder(private val binding: MatchListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItem(match: Match) {
            // The two elements our match has. Name (text) of the person and their image (profilePictureUrl)
            binding.matchNameView.text = match.username
            binding.matchImageView.load(match.profilePictureUrl)

            binding.matchImageView.setOnClickListener {
                Log.d("bindItem -- match", match.toString())

                matchResultListener.sendRequest(match)
            }
        }
    }
}