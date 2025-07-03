package com.example.shelfship.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.SessionData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

import com.example.shelfship.models.Message
import com.example.shelfship.utils.FirebaseUtils

class ChatViewModel(private val chatUUI: String) : ViewModel() {

    class Factory(private val chatUUI: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Log.i("ChatViewModel::Factory", "Creating view model.")
            return ChatViewModel(chatUUI) as T
        }
    }

    private val _chatMessages = MutableStateFlow(
        SessionData(messages = emptyList(), owner = "", participants = emptyList())
    )
    val chatMessages: StateFlow<SessionData> = _chatMessages

    //private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        startSessionListener()
    }

    private fun startSessionListener() {
        val firestore = FirebaseUtils.getInstance()

        val docRef = firestore.collection("sessions").document(chatUUI)

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("MESSAGE", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                //val owner = snapshot.getString("owner")
                val userReference = snapshot.getDocumentReference("owner")
                val participants = snapshot.get("participants") as? List<String> ?: emptyList()
                val messageMaps = snapshot.get("messages") as? List<Map<String, Any>> ?: emptyList()

                val messages = messageMaps.mapNotNull { msg ->
                    try {
                        Message(
                            sender = msg["sender"] as? String ?: "",
                            content = msg["content"] as? String ?: "",
                            timestamp = msg["timestamp"] as? String ?: "",
                            id = msg["id"] as? String ?: "",
                            edited = msg["edited"] as? String ?: "",
                            readby = msg["readby"] as? List<String> ?: emptyList(),
                            reactions = emptyList()
                        )
                    } catch (e: Exception) {
                        Log.w("MESSAGE_PARSE", "Failed to parse message", e)
                        null
                    }
                }

                if (userReference != null) {
                    userReference.get().addOnSuccessListener { usernameSnapshot ->
                        val ownerUsername = usernameSnapshot.getString("username") ?: "Unknown"
                        _chatMessages.value = SessionData(messages, ownerUsername, participants)
                    }.addOnFailureListener {
                        _chatMessages.value = SessionData(messages, "Unknown", participants)
                    }
                }

            } else {
                Log.d("MESSAGE", "Current data: null")
            }
        }
    }
}