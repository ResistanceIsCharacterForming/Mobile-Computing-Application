package com.example.shelfship.utils

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
        val bookCover = itemView.findViewById<ImageView>(R.id.book_cover)
        val bookTitle = itemView.findViewById<MaterialTextView>(R.id.book_title)
        val authors = itemView.findViewById<MaterialTextView>(R.id.authors)
        val starRating = itemView.findViewById<RatingBar>(R.id.average_rating)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RowRecyclerViewAdapter.ViewHolder {
        val view = View.inflate(parent.context, R.layout.item_book_row, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RowRecyclerViewAdapter.ViewHolder, position: Int) {
        val book = books[position]

        Glide.with(holder.bookCover.context)
            .load(book.volumeInfo.imageLinks.thumbnail)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.bookCover)

        holder.bookTitle.text = book.volumeInfo.title
        holder.authors.text = book.volumeInfo.authors.joinToString(", ")
        holder.starRating.rating = book.volumeInfo.averageRating?.toFloat() ?: 0f
    }

    override fun getItemCount(): Int {
        return books.size
    }

    fun updateSearchResults(newBooks: ArrayList<GBSearchBook>) {
        books = newBooks
        notifyDataSetChanged()
    }
}