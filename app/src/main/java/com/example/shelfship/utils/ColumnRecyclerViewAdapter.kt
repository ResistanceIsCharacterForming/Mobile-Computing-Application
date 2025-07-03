package com.example.shelfship.utils

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shelfship.R
import com.example.shelfship.models.FirestoreBookDetails
import com.google.android.material.textview.MaterialTextView

class ColumnRecyclerViewAdapter(private var listener: onItemClickListener? = null)
    : ListAdapter<FirestoreBookDetails, ColumnRecyclerViewAdapter.ViewHolder>(BooksDiffCallback) {

        object BooksDiffCallback: DiffUtil.ItemCallback<FirestoreBookDetails>() {
            override fun areItemsTheSame(
                oldItem: FirestoreBookDetails,
                newItem: FirestoreBookDetails
            ): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: FirestoreBookDetails,
                newItem: FirestoreBookDetails
            ): Boolean {
                return oldItem.id == newItem.id && oldItem.thumbnail == newItem.thumbnail && oldItem.title == newItem.title && oldItem.ownerBookShelves == newItem.ownerBookShelves && oldItem.assignedGenre == newItem.assignedGenre
            }

        }

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

    fun updateSearchResults(newBooks: List<FirestoreBookDetails>) {
        this.submitList(newBooks)
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
        Log.d("SearchActivity", "Binding view holder at position $position")
        val book = this.getItem(position)
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
}