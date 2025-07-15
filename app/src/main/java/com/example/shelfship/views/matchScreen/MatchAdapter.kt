package com.example.shelfship.views.matchScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.databinding.MatchListItemBinding
import com.example.shelfship.models.Match
import coil.load

class MatchAdapter(private val matchResultListener: MatchResultListener) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    private var matches: List<Match> = emptyList()

    interface MatchResultListener {
    }

    // Update the matches list and refresh the RecyclerView
    fun updateMatches(newMatches: List<Match>) {
        matches = newMatches
        notifyDataSetChanged()  // You can also consider using notifyItemRangeChanged if the list size doesn't change drastically
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
            binding.matchNameView.text = match.username
            binding.matchImageView.load(match.profilePictureUrl)
        }
    }
}