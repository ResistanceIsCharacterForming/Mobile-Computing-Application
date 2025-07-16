package com.example.shelfship.views.chatScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.shelfship.models.Message
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.databinding.ChatListItemBinding
import com.example.shelfship.views.chatScreen.ChatAdapter.ChatViewHolder

// Adapter for the data we want to present from a session (chat).
class ChatAdapter(
    private val chatContentListener: ChatContentListener) :
    RecyclerView.Adapter<ChatViewHolder>() {

    interface ChatContentListener {}

    // The data we want to display.
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
            // The three elements we want to bridge in the chat's fragment. Name (text) of a message owner, the message itself, and the message's timestamp.
            binding.senderTextView.text = message.sender
            binding.timestampTextView.text = message.timestamp
            binding.messageTextView.text = message.content
        }
    }

}