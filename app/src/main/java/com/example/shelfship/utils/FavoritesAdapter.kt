package com.example.shelfship.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.R
import com.example.shelfship.models.FirestoreBookDetails

import com.bumptech.glide.Glide

class FavoritesAdapter : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private var favoritesList: List<FirestoreBookDetails> = emptyList()

    fun submitList(list: List<FirestoreBookDetails>) {
        favoritesList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.bookTitleText)
        val coverImageView: ImageView = itemView.findViewById(R.id.bookCoverImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_title, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = favoritesList[position]
        holder.titleTextView.text = book.title ?: "Untitled"

        if (book.thumbnail != null && book.thumbnail.isNotEmpty()) {
            Glide.with(holder.coverImageView.context)
                .load(book.thumbnail)
                .placeholder(R.drawable.placeholder_book)
                .error(R.drawable.error_book)
                .into(holder.coverImageView)
        } else {
            holder.coverImageView.setImageResource(R.drawable.placeholder_book)
        }
    }

    override fun getItemCount(): Int = favoritesList.size
}