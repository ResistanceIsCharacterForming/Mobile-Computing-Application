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

    class Factory(private val chatUUI: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Log.d("ChatViewModel::Factory", "Creating view model.")
            return ChatViewModel(chatUUI) as T
        }
    }

    private val _chatMessages = MutableStateFlow(
        SessionData(messages = emptyList(), participants = emptyList())
    )
    val chatMessages: StateFlow<SessionData> = _chatMessages

    //private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        startSessionListener()
    }

    private fun startSessionListener() {
        val firestore = FirebaseUtils.getInstance()

        val docRef = firestore.collection("sessions").document(chatUUI)

        Log.d("startSessionListener", docRef.toString())

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("MESSAGE", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val participants = snapshot.get("participants") as? List<String> ?: emptyList()
                val messageMaps = snapshot.get("messages") as? List<Map<String, Any>> ?: emptyList()

                val messages = messageMaps.mapNotNull { msg ->
                    try {
                        Message(
                            sender = msg["sender"] as? String ?: "",
                            content = msg["content"] as? String ?: "",
                            timestamp = msg["timestamp"] as? String ?: "",
                            id = msg["uid"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.d("MESSAGE_PARSE", "Failed to parse message", e)
                        null
                    }
                }

                _chatMessages.value = SessionData(messages, participants)

            } else {
                Log.d("MESSAGE", "Current data: null")
            }
        }
    }
}