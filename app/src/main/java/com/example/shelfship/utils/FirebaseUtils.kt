package com.example.shelfship.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.shelfship.models.UserData

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

    suspend fun saveGoogleBooksAccessToken(token: String): Boolean {
        val uid = currentUserId()
        if (uid == null) return false

        return try {
            firestore.collection("users")
                .document(uid)
                .update("googleBooksAccessToken", token)
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

    val isLoggedIn: Boolean
        get() = currentUserId() != null
}
