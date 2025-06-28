package com.example.shelfship.utils

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shelfship.R
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.models.GBSearchBook
import com.google.android.material.textview.MaterialTextView

class ColumnRecyclerViewAdapter(private var books: ArrayList<FirestoreBookDetails>, private var listener: onItemClickListener? = null)
    :RecyclerView.Adapter<ColumnRecyclerViewAdapter.ViewHolder>(){
        class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val bookCover: ImageView = itemView.findViewById<ImageView>(R.id.book_cover)
            val bookTitle: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.book_title)
        }

    interface onItemClickListener {
        fun onItemClick(item: FirestoreBookDetails)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = View.inflate(parent.context, R.layout.item_book_column, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val book = books[position]
        if (book.thumbnail != null) {
            Glide.with(holder.bookCover.context)
                .load(book.thumbnail)
                .placeholder(R.drawable.placeholder_book)
                .error(R.drawable.error_book)
                .into(holder.bookCover)
        }
        else {
            holder.bookCover.setImageResource(R.drawable.placeholder_book)
        }
        holder.bookTitle.text = book.title
        holder.itemView.setOnClickListener { listener?.onItemClick(book) }
    }

    override fun getItemCount(): Int {
        return books.size
    }
}