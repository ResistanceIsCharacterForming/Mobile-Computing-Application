package com.example.shelfship.views.homeScreen

import com.example.shelfship.views.chatScreen.ChatFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            val fragment = HomeFragment.newInstance()

            supportFragmentManager.beginTransaction()
                .replace(R.id.homeFragment, fragment)
                .commit()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }
    }
}