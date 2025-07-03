package com.example.shelfship.views.chatScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.shelfship.models.Message
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.databinding.ChatListItemBinding
import com.example.shelfship.views.chatScreen.ChatAdapter.ChatViewHolder

class ChatAdapter(private val chatContentListener: ChatContentListener) :
    RecyclerView.Adapter<ChatViewHolder>() {

    interface ChatContentListener {
    }

    private var messages: List<Message> = emptyList()

    fun setMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding =
            ChatListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bindItem(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class ChatViewHolder(private val binding: ChatListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindItem(message: Message) {
            binding.senderTextView.text = message.sender
            binding.timestampTextView.text = message.timestamp
            binding.messageTextView.text = message.content
//            itemView.setOnClickListener {
////                Snackbar.make(itemView, place.name, Snackbar.LENGTH_SHORT).show()
//                placeSelectionListener.onPlaceSelected(place)
//            }
            //itemView.setOnClickListener { placeSelectionListener.onPlaceSelected(place) }
        }
    }

}