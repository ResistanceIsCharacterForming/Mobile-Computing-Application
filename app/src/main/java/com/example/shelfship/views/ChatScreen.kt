package com.example.shelfship.views

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R

class ChatScreen : AppCompatActivity() {

    private val messages = mutableListOf("Hello", "Wyd")
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val friendName = intent.getStringExtra("friendName") ?: "Unknown"

        title = "Chat with $friendName"

        val listView = findViewById<ListView>(R.id.messageList)
        val editText = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
        listView.adapter = adapter

        sendButton.setOnClickListener {
            val message = editText.text.toString().trim()
            if (message.isNotEmpty()) {
                messages.add("Me: $message")
                adapter.notifyDataSetChanged()
                editText.text.clear()
            }
        }
    }
}
