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

class FriendsRecyclerViewAdapter(private var itemListener: onItemClickListener? = null,
                                 private var chatListener: onItemClickListener? = null):
    ListAdapter<FriendUserData, FriendsRecyclerViewAdapter.ViewHolder>(FriendsDiffCallback){
    object FriendsDiffCallback:DiffUtil.ItemCallback<FriendUserData>() {
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
        val friendName: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.friendName)
        val messageButton: ImageButton = itemView.findViewById<ImageButton>(R.id.btnMessage)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(parent.context, R.layout.item_friend, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.friendName.text = this.getItem(position).displayName
        holder.messageButton.setOnClickListener { chatListener?.onItemClick(this.getItem(position).uid) }
        holder.itemView.setOnClickListener { itemListener?.onItemClick(this.getItem(position).uid) }
    }

    fun updateSearchResults(newFriends: List<FriendUserData>) {
        this.submitList(newFriends)
    }

    fun clearItems() {
        this.submitList(emptyList())
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        this.itemListener = listener
    }

    fun setOnChatClickListener(listener: onItemClickListener) {
        this.chatListener = listener
    }

    interface onItemClickListener {
        fun onItemClick(uid: String)
    }
}
