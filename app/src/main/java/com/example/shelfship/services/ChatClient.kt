package com.example.shelfship.services

import android.util.Log
import com.example.shelfship.utils.FirebaseUtils

import com.example.shelfship.utils.FirebaseUtils.currentSessionDetails
import com.example.shelfship.utils.FirebaseUtils.currentUserDetails
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

import com.example.shelfship.models.UserData
import com.example.shelfship.utils.FirebaseUtils.firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.tasks.await

object ChatClient {



    suspend fun saveMessageToFirebase(firestore: FirebaseFirestore,
                                      uid: String, userMessage: String, systemOwner: Boolean = false): Boolean {

        var owner = ""
        if (systemOwner.equals(true)) {
            owner = "System"
        } else {
            val user = currentUserDetails()?.get()?.await()
            owner = user?.getString("username").toString()
        }

        val messageData = mapOf(
            "attachment" to "",
            "edited" to "",
            "uid" to UUID.randomUUID().toString(),
            "message" to userMessage,
            "reactions" to emptyList<String>(),
            "readby" to emptyList<String>(),
            "sender" to owner,
            "timestamp" to ""
        )

        firestore.collection("sessions").document(uid)
            .update("messages", FieldValue.arrayUnion(messageData))
            .await()

        return true

    }

    fun sessionsDocumentListener(firestore: FirebaseFirestore) {
        // https://firebase.google.com/docs/firestore/query-data/listen#kotlin

        val docRef = firestore.collection("sessions").document("oO1fv6QmzVSVDf3yZpGQ")

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("MESSAGE", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val messages = snapshot.get("messages") as? List<Map<String, Any>>

                val message = messages?.last()
                val owner = message?.get("sender")
                val text = message?.get("message")

                Log.d("MESSAGE", "${owner}: ${text}")
            } else {
                Log.d("MESSAGE", "Current data: null")
            }
        }
    }

}