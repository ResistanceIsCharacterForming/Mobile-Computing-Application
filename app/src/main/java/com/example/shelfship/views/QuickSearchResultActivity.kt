package com.example.shelfship.views

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.shelfship.R
import com.example.shelfship.views.compose.ChatScreen

class QuickSearchResultActivity : AppCompatActivity() {

    private lateinit var foundMatchButton: Button
    private lateinit var chatNowButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_search_result)

        foundMatchButton = findViewById(R.id.btnFoundMatch)
        chatNowButton = findViewById(R.id.btnChatNow)

        chatNowButton.setOnClickListener {
            val intent = Intent(this, ChatScreen::class.java)
            startActivity(intent)
        }
    }
}
