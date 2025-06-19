package com.example.shelfship.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R

class MenuScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<Button>(R.id.btnFriends).setOnClickListener {
            startActivity(Intent(this, FriendScreen::class.java))
        }
        // The other menu buttons can be added here
    }
}
