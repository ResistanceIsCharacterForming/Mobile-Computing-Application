package com.example.shelfship.utils

import android.util.Log
import com.example.shelfship.models.Match
import com.example.shelfship.models.SessionData
import com.example.shelfship.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlinx.coroutines.flow.StateFlow

import com.google.firebase.firestore.FieldValue

import com.example.shelfship.services.ChatClient
import com.example.shelfship.viewModels.HomeViewModel
import com.google.firebase.firestore.ListenerRegistration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object FirebaseUtils {

    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

    fun getInstance(): FirebaseFirestore {
        return firestore
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

    suspend fun saveMessageToFirebase(
        uid: String,
        userMessage: String,
        timestamp: String,
        systemOwner: Boolean = false
    ): Boolean {
        if (ChatClient.saveMessageToFirebase(firestore, uid, userMessage, timestamp, systemOwner)) {
            return true
        } else {
            return false
        }
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

    suspend fun addToQueue(): Boolean {

        var userUID = ""
        val user = currentUserDetails()?.get()?.await()
        userUID = user?.getString("uid").toString()
        val username = user?.getString("username").toString()

        val timestamp = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        val thisUser = mapOf(
            "username" to username,
            "uid" to userUID,
            "profilePictureUrl" to "",
            "added" to timestamp,
            "matching" to false,
        )

        firestore.collection("matchmaking").document(userUID).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    firestore.collection("matchmaking")
                        .document(userUID)
                        .set(thisUser)
                }
            }

        return true
    }

    suspend fun getTestData(): List<Match> {
        var userUID = ""
        val user = currentUserDetails()?.get()?.await()
        userUID = user?.getString("uid").toString()
        val collection = firestore.collection("matchmaking")

        var users = emptyList<Match>()

        val snapshot = collection.whereNotEqualTo("uid", userUID)
            .limit(3)
            .get()
            .await()

             users = snapshot.documents.mapNotNull { thisUser ->
                try {
                    Match(
                        username = thisUser["username"] as? String ?: "",
                        uid = thisUser["uid"] as? String ?: "",
                        profilePictureUrl = thisUser["profilePictureUrl"] as? String ?: "",
                        added = thisUser["added"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    Log.w("MATCH_PARSE", "Failed to parse match document", e)
                    null
                }

            }

        return users;
    }

}