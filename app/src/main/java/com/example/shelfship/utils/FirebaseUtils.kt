package com.example.shelfship.utils

import android.util.Log
import com.example.shelfship.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

import com.google.firebase.firestore.FieldValue

import com.example.shelfship.services.ChatClient

object FirebaseUtils {

    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
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

    /*
    fun currentSessionDetails(uid: String): DocumentReference {
        return firestore.collection("sessions").document(uid)
    }*/

    suspend fun saveMessageToFirebase(uid: String, userMessage: String, systemOwner: Boolean = false): Boolean {
        if (ChatClient.saveMessageToFirebase(firestore, uid, userMessage, systemOwner)) {
            return true
        } else {
            return false
        }
<<<<<<< Updated upstream

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
=======
>>>>>>> Stashed changes
    }

    fun sessionsDocumentListener() {
        ChatClient.sessionsDocumentListener(firestore)
    }

    suspend fun saveGoogleBooksTokens(token: String, authCode: String): Boolean {
        val uid = currentUserId()
        if (uid == null) return false

        return try {
            firestore.collection("users")
                .document(uid)
                .update("googleBooksAccessToken", token, "googleBooksAuthCode", authCode)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    val isLoggedIn: Boolean
        get() = currentUserId() != null
}
