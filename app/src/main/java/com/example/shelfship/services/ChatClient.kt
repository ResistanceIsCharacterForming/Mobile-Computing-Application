package com.example.shelfship.services

import android.util.Log
import com.example.shelfship.models.SessionData
import com.example.shelfship.utils.FirebaseUtils

//import com.example.shelfship.utils.FirebaseUtils.currentSessionDetails
import com.example.shelfship.utils.FirebaseUtils.currentUserDetails
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

import com.example.shelfship.models.UserData
import com.example.shelfship.utils.FirebaseUtils.firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

object ChatClient {



    suspend fun saveMessageToFirebase(firestore: FirebaseFirestore,
                                      uid: String, userMessage: String, timestamp: String, systemOwner: Boolean = false): Boolean {

        var owner = ""
        if (systemOwner.equals(true)) {
            owner = "System"
        } else {
            val user = currentUserDetails()?.get()?.await()
            owner = user?.getString("username").toString()
        }

        val messageData = mapOf(
            "uid" to UUID.randomUUID().toString(),
            "content" to userMessage,
            "sender" to owner,
            "timestamp" to timestamp
        )

        firestore.collection("sessions").document(uid)
            .update("messages", FieldValue.arrayUnion(messageData))
            .await()

        return true

    }

}