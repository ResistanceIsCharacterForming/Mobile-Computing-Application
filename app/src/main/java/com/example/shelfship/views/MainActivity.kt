package com.example.shelfship.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R
import com.example.shelfship.views.showChatMessages.ChatFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_main)
        //setContentView(R.layout.activity_chat)

        setContentView(R.layout.activity_chat)
        supportFragmentManager.beginTransaction()
            .replace(R.id.chatListFragment , ChatFragment.newInstance())
            .commit()

        /*if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.ChatScreen, ChatFragment.newInstance())
                .commit()
        }*/


        /*
        val showButton = findViewById<Button>(R.id.button2)

        val editText = findViewById<EditText>(R.id.inputField)

        Log.d("MainActivity", "Running saveMessageToFirebase...")


        lifecycleScope.launch {
            FirebaseUtils.saveMessageToFirebase(
                uid = "oO1fv6QmzVSVDf3yZpGQ",
                userMessage = "A new user has joined the chat.",
                systemOwner = true)
            Log.d("MainActivity", "Backoutside saveMessageToFirebase")
        }

        showButton.setOnClickListener {

            val text = editText.text

            //Log.d("myTag", text.toString())

            lifecycleScope.launch {
                FirebaseUtils.saveMessageToFirebase(
                    uid = "oO1fv6QmzVSVDf3yZpGQ",
                    userMessage = text.toString()
                )
            }

            //FirebaseUtils.listener()

            editText.getText().clear()

        }*/


    }

}
