package com.example.shelfship.utils

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.R
import com.example.shelfship.models.FriendUserData
import com.google.android.material.textview.MaterialTextView

// ======= List Adapter =======
// https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
// https://medium.com/androiddevelopers/adapting-to-listadapter-341da4218f5b

class PendingRequestsRecyclerViewAdapter(private var itemListener: onItemClickListener? = null,
                                         private var dismissListener: onItemClickListener? = null):
ListAdapter<FriendUserData, PendingRequestsRecyclerViewAdapter.ViewHolder>(UserDiffCallback){
    object UserDiffCallback:DiffUtil.ItemCallback<FriendUserData>() {
        override fun areItemsTheSame(
            oldItem: FriendUserData,
            newItem: FriendUserData
        ): Boolean {
            return oldItem.uid == newItem.uid
        }
        override fun areContentsTheSame(
            oldItem: FriendUserData,
            newItem: FriendUserData
        ): Boolean {
            return oldItem.uid == newItem.uid && oldItem.displayName == newItem.displayName && oldItem.sessionID == newItem.sessionID
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userName: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.user_name)
        val dismissButton: ImageButton = itemView.findViewById<ImageButton>(R.id.btnDismissPendingRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(parent.context, R.layout.item_pending_request, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.userName.text = this.getItem(position).displayName
        holder.dismissButton.setOnClickListener { dismissListener?.onItemClick(this.getItem(position).uid) }
        holder.itemView.setOnClickListener { itemListener?.onItemClick(this.getItem(position).uid) }
    }

    fun updateSearchResults(newFriends: List<FriendUserData>) {
        this.submitList(newFriends)
    }

    fun clearItems() {
        this.submitList(emptyList())
    }

    fun setOnItemClickListener(listener: PendingRequestsRecyclerViewAdapter.onItemClickListener) {
        this.itemListener = listener
    }

    fun setOnDismissClickListener(listener: onItemClickListener){
        this.dismissListener = listener
    }


    interface onItemClickListener {
        fun onItemClick(uid: String)
    }
}