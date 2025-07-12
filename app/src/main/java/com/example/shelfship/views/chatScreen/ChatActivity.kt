package com.example.shelfship.views.chatScreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        if (savedInstanceState == null) {
            val chatUUI = intent.getStringExtra("chatUUI") ?: error("Need a chatUUI.")
            val fragment = ChatFragment.newInstance(chatUUI)

            supportFragmentManager.beginTransaction()
                .replace(R.id.chatListFragment, fragment)
                .commit()
        }
    }

    companion object {
        fun start(context: Context, chatUUI: String) {
            Log.d("TESTING", "I Work")
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("chatUUI", chatUUI)
            }
            context.startActivity(intent)
        }
    }
}