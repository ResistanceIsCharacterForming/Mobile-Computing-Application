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

class FriendRequestsRecyclerViewAdapter(private var itemListener: onItemClickListener? = null,
                                        private var acceptListener: onItemClickListener? = null,
                                        private var rejectListener: onItemClickListener? = null):
    ListAdapter<FriendUserData, FriendRequestsRecyclerViewAdapter.ViewHolder>(UserDiffCallback){

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
        val acceptButton: ImageButton = itemView.findViewById<ImageButton>(R.id.btnAccept)
        val rejectButton: ImageButton = itemView.findViewById<ImageButton>(R.id.btnReject)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = View.inflate(parent.context, R.layout.item_friend_request, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = this.getItem(position)
        holder.userName.text = item.displayName
        holder.acceptButton.setOnClickListener { acceptListener?.onItemClick(item.uid, item.displayName) }
        holder.rejectButton.setOnClickListener { rejectListener?.onItemClick(item.uid) }
        holder.itemView.setOnClickListener { itemListener?.onItemClick(item.uid) }
    }

    fun updateSearchResults(newFriends: List<FriendUserData>) {
        this.submitList(newFriends)
    }

    fun clearItems() {
        this.submitList(emptyList())
    }

    fun setOnItemClickListener(listener: FriendRequestsRecyclerViewAdapter.onItemClickListener) {
        this.itemListener = listener
    }

    fun setOnAcceptClickListener(listener: FriendRequestsRecyclerViewAdapter.onItemClickListener) {
        this.acceptListener = listener
    }

    fun setOnRejectClickListener(listener: onItemClickListener){
        this.rejectListener = listener
    }

    interface onItemClickListener {
        fun onItemClick(uid: String, displayName: String? = null)
    }
}