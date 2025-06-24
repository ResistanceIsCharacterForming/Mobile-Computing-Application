package com.example.shelfship.viewmodels
<<<<<<< Updated upstream
/*
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.example.shelfship.R

class ChatViewModel : ViewModel() {

    val textField = MutableLiveData<String>()
    val button = MutableLiveData<String>()
    val editText = MutableLiveData<String>()

    fun run() {
        viewModelScope.launch {

            monitorText

        }
    }*/

    /*fun monitorTextField(view: View) {


            val showButton = findViewById<Button>(R.id.button2)

            val editText = findViewById<EditText>(R.id.inputField)

            showButton.setOnClickListener {

                val text = editText.text

                Log.d("myTag", text.toString())

                editText.getText().clear()
            }


    }*/
/*
    val monitorText: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            TODO("Not yet implemented")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textField.value = "$s"
            Log.d("myTag", "$s")
        }

        override fun afterTextChanged(s: Editable?) {
            TODO("Not yet implemented")
        }

    }

}*/
=======

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.shelfship.utils.FirebaseUtils

class ChatViewModel: ViewModel() {

    fun chatSession(uid: String, context: Context) {
        viewModelScope.launch {

            // saveMessageToFirebase

            // sessionsDocumentListener

        }
    }

}
>>>>>>> Stashed changes
