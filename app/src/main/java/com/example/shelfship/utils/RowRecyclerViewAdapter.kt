package com.example.shelfship.utils

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shelfship.R
import com.example.shelfship.models.GBSearchBook
import com.google.android.material.textview.MaterialTextView

class RowRecyclerViewAdapter(private var books: ArrayList<GBSearchBook>):
    RecyclerView.Adapter<RowRecyclerViewAdapter.ViewHolder>(){
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val bookCover: ImageView = itemView.findViewById<ImageView>(R.id.book_cover)
        val bookTitle: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.book_title)
        val authors: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.authors)
        val starRating: RatingBar = itemView.findViewById<RatingBar>(R.id.average_rating)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = View.inflate(parent.context, R.layout.item_book_row, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        Log.d("RecyclerViewAdapter", "Binding book: $book")
        if (book.volumeInfo.imageLinks != null) {
            Glide.with(holder.bookCover.context)
                .load(book.volumeInfo.imageLinks.thumbnail?:"")
                .placeholder(R.drawable.message_icon)
                .error(R.drawable.ic_notification)
                .into(holder.bookCover)
        }
        holder.bookTitle.text = book.volumeInfo.title
        holder.authors.text = book.volumeInfo.authors?.joinToString(", ")
        holder.starRating.rating = book.volumeInfo.averageRating?.toFloat() ?: 0f
    }

    override fun getItemCount(): Int {
        return books.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchResults(newBooks: ArrayList<GBSearchBook>) {
        Log.d("RecyclerViewAdapter", "Updating search results: $newBooks")
        books = newBooks
        notifyDataSetChanged()
    }
}