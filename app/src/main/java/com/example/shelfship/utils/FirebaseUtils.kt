package com.example.shelfship.utils

import android.util.Log
import com.example.shelfship.models.UserData
import com.google.android.gms.analytics.ecommerce.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

import com.example.shelfship.models.Message

object FirebaseUtils {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    suspend fun saveUserToFirestore(userData: UserData): Boolean {
        return try {
            firestore.collection("users")
                .document(userData.uid)
                .set(userData)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun currentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun currentUserDetails(): DocumentReference? {
        val uid = currentUserId()
        return if (uid != null) {
            firestore.collection("users").document(uid)
        } else {
            null
        }
    }

    fun currentSessionDetails(uid: String): DocumentReference {
        return firestore.collection("sessions").document(uid)
    }

    suspend fun saveMessageToFirebase(uid: String, userMessage: String, systemOwner: Boolean = false): Boolean {

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

    fun listener() {
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


    val isLoggedIn: Boolean
        get() = currentUserId() != null
}
