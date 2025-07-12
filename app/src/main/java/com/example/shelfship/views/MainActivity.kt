package com.example.shelfship.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shelfship.utils.FirebaseUtils
import com.example.shelfship.views.chatScreen.ChatActivity
import com.example.shelfship.views.matchScreen.MatchActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        lifecycleScope.launch {
            FirebaseUtils.addTestData("5bcKzc8ueEQFiHXb72UjgmIqO3d2")
            FirebaseUtils.addTestData("GWzn3UIyreMt0R0GyoTLIkaEhFk1")
            FirebaseUtils.addTestData("fcu4Xiq87PQnM8X8XRsWgEIMITk2")
            FirebaseUtils.addTestData("fvKkX7Y8QJMF2szezaUV6rGkx7N2")
            FirebaseUtils.addTestData("tIXkrCS2EfVYZP3V6327KJjnUWd2")
        }*/

        MatchActivity.start(this)

        //ChatActivity.start(this, chatUUI="hello")

        //setContentView(R.layout.activity_main)
        //setContentView(R.layout.activity_chat)

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
