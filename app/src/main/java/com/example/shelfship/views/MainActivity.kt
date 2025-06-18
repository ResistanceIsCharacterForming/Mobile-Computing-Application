package com.example.shelfship.views

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        setContentView(R.layout.chat_screen)

        val showButton = findViewById<Button>(R.id.button2)

        val editText = findViewById<EditText>(R.id.inputField)

        showButton.setOnClickListener {

            val text = editText.text

            Log.d("myTag", text.toString())

            editText.getText().clear()

        }

        }

    }
