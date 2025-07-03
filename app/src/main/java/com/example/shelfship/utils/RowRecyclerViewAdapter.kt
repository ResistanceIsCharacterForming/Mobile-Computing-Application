package com.example.shelfship.utils

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shelfship.R
import com.example.shelfship.models.GBSearchBook
import com.google.android.material.textview.MaterialTextView

class RowRecyclerViewAdapter(private var books: ArrayList<GBSearchBook>, private var listener: onItemClickListener? = null):
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
        if (book.volumeInfo.imageLinks != null) {
            Glide.with(holder.bookCover.context)
                .load(book.volumeInfo.imageLinks.thumbnail?:"")
                .placeholder(R.drawable.placeholder_book)
                .error(R.drawable.error_book)
                .into(holder.bookCover)
        }
        else {
            holder.bookCover.setImageResource(R.drawable.placeholder_book)
        }
        holder.bookTitle.text = book.volumeInfo.title
        holder.authors.text = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown authors"
        holder.starRating.rating = book.volumeInfo.averageRating?.toFloat() ?: 0f
        holder.itemView.setOnClickListener { listener?.onItemClick(book) }
    }

    override fun getItemCount(): Int {
        return books.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchResults(newBooks: ArrayList<GBSearchBook>) {
        books.clear()
        books.addAll(newBooks)
        notifyDataSetChanged()
    }

    fun clearItems() {
        val oldSize = books.size
        books.clear()
        notifyItemRangeRemoved(0, oldSize)
    }

    interface onItemClickListener {
        fun onItemClick(item: GBSearchBook)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }
}