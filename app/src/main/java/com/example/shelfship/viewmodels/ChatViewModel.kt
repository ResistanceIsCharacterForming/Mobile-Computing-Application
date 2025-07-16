package com.example.shelfship.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shelfship.models.SessionData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

import com.example.shelfship.models.Message
import com.example.shelfship.utils.FirebaseUtils

class ChatViewModel(private val chatUUI: String) : ViewModel() {

    // I'm using a factory, same as the one from the lecture code, since we need to pass an ID to the ViewModel.
    class Factory(private val chatUUI: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Log.d("ChatViewModel::Factory", "Creating view model.")
            return ChatViewModel(chatUUI) as T
        }
    }

    // Our StateFlow.
    private val _chatMessages = MutableStateFlow(
        SessionData(messages = emptyList(), participants = emptyList())
    )
    val chatMessages: StateFlow<SessionData> = _chatMessages

    init {
        startSessionListener()
    }

    // This function is ongoing while the ViewModel is alive. Listen to a given document in sessions and look for changes in the message array.
    private fun startSessionListener() {

        // Note: Most of the code here is based on this article: https://firebase.google.com/docs/firestore/query-data/listen
        // It's just changed to work for our system.

        // This is my fault, poor planning, firestore instance shouldn't be needed to be used inside here. Instead this ViewModel should rather listen to a function inside FirebaseUtils and then respond.
        val firestore = FirebaseUtils.firestore
        val docRef = firestore.collection("sessions").document(chatUUI)

        Log.d("startSessionListener", docRef.toString())

        // Add a listener to the document itself. We get a snapshot of it right away, and then each time there is a change.
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("startSessionListener", "Listen failed.", e)
                return@addSnapshotListener
            }

            // If the snapshot isnt null and has data go ahead.
            if (snapshot != null && snapshot.exists()) {
                // Get the users in the chat -- participants -- and all their messages -- messageMaps.
                val participants = snapshot.get("participants") as? List<String> ?: emptyList()
                val messageMaps = snapshot.get("messages") as? List<Map<String, Any>> ?: emptyList()

                // We are only interested in the messages themselves.
                val messages = messageMaps.mapNotNull { msg ->
                    try {
                        Message(
                            sender = msg["sender"] as? String ?: "",
                            content = msg["content"] as? String ?: "",
                            timestamp = msg["timestamp"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.d("startSessionListener", "Failed to parse message", e)
                        null
                    }
                }

                // Add the new message to the StateFlow.
                _chatMessages.value = SessionData(messages, participants)

            } else {
                Log.d("MESSAGE", "Current data: null")
            }
        }
    }
}