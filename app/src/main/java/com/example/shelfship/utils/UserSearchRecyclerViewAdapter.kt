package com.example.shelfship.utils

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.R
import com.example.shelfship.models.UserData
import com.google.android.material.textview.MaterialTextView

// ======= List Adapter =======
// https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
// https://medium.com/androiddevelopers/adapting-to-listadapter-341da4218f5b

class UserSearchRecyclerViewAdapter (private var itemListener: onItemClickListener? = null,
                                     private var sendReqListener: onItemClickListener? = null):
    ListAdapter<UserData, UserSearchRecyclerViewAdapter.ViewHolder>(UserDiffCallback) {

    object UserDiffCallback: DiffUtil.ItemCallback<UserData>() {
            override fun areItemsTheSame(
                oldItem: UserData,
                newItem: UserData
            ): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(
                oldItem: UserData,
                newItem: UserData
            ): Boolean {
                return oldItem.uid == newItem.uid && oldItem.username == newItem.username && oldItem.profilePictureUrl == newItem.profilePictureUrl
            }
        }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userName: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.user_name)
        val sendReqButton: ImageButton = itemView.findViewById<ImageButton>(R.id.btnSendReq)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = View.inflate(parent.context, R.layout.item_user_search_result, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = this.getItem(position)
        holder.userName.text = item.username
        holder.sendReqButton.setOnClickListener { sendReqListener?.onItemClick(item.uid, item.username) }
        holder.itemView.setOnClickListener { itemListener?.onItemClick(item.uid) }
    }

    fun updateSearchResults(newFriends: List<UserData>) {
        this.submitList(newFriends)
    }

    fun clearItems() {
        this.submitList(emptyList())
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        this.itemListener = listener
    }

    fun setOnSendReqClickListener(listener: onItemClickListener) {
        this.sendReqListener = listener
    }

    interface onItemClickListener {
        fun onItemClick(uid: String, displayName: String? = null)
    }
}