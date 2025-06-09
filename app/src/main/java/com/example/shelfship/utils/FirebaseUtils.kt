package com.example.shelfship.utils

import com.example.shelfship.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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

    val isLoggedIn: Boolean
        get() = currentUserId() != null
}
