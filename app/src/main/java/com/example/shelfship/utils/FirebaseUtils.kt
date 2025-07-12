package com.example.shelfship.utils

import android.util.Log
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.models.Match
import com.example.shelfship.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.shelfship.services.ChatClient
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object FirebaseUtils {

    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun saveUserToFirestore(userData: UserData): Boolean {
        return try {
            firestore.collection("users")
                .document(userData.uid)
                .set(userData)
                .await()
            Log.d("FirebaseUtils", "User saved to Firestore successfully!")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FirebaseUtils", "Error saving user to Firestore: ${e.message}")
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

    fun currentSessionDetails(uid: String): DocumentReference {
        return firestore.collection("sessions").document(uid)
    }

    suspend fun fetchFromMatchmaking(): QuerySnapshot {

        val collection = firestore.collection("matchmaking")

        val userUID = currentUserId().toString()

        val snapshot = collection.whereNotEqualTo("uid", userUID)
                .orderBy("added", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .await()

        return snapshot
    }

    suspend fun userExists(uid: String): Boolean {
        return firestore.collection("users").document(uid).get().await().exists()
    }

    suspend fun saveMessageToFirebase(
        uid: String,
        userMessage: String,
        timestamp: String,
        systemOwner: Boolean = false
    ): Boolean {
        return ChatClient.saveMessageToFirebase(
                firestore,
                uid,
                userMessage,
                timestamp,
                systemOwner
            )
    }

    suspend fun addTestData(uid: String) {

        val genres = listOf(
            "Fantasy",
            "SciFie",
            "Mystery",
            "Romance",
            "Thriller",
            "Horror",
            "Non-fiction"
        )

        for (i in 1..10) {

            val book = mapOf(
                "assignedGenre" to genres.random(),
                "ownerBookShelves" to listOf(true, false, false, false),
                "thumbnail" to "",
                "title" to "",
                "userRating" to Random.nextInt(1, 6)
            )

            firestore.collection("users")
                .document(uid)
                .collection("library")
                .add(book)
                .await()
        }
    }

    suspend fun updateLibrary(lightBookDetails: FirestoreBookDetails): Boolean {
        val uid = currentUserId()
        if (uid == null) return false
        return try {
            withContext(Dispatchers.IO) {
                val bookReference = firestore.collection("users").document(uid)
                    .collection("library").document(lightBookDetails.id)
                if (lightBookDetails.ownerBookShelves == listOf<Boolean>(
                        false,
                        false,
                        false,
                        false
                    )
                ) {
                    bookReference.delete().await()
                } else {
                    bookReference.set(lightBookDetails).await()
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getBookFromLibrary(bookId: String): FirestoreBookDetails? {
        val uid = currentUserId()
        return try {
            withContext(Dispatchers.IO) {
                if (uid != null) {
                    val bookReference = firestore.collection("users").document(uid)
                        .collection("library").document(bookId)
                    val documentSnapshot = bookReference.get().await()
                    if (documentSnapshot.exists()) {
                        Log.d("BookDetailsViewModel", "Book details being turned to object!")
                        documentSnapshot.toObject(FirestoreBookDetails::class.java)
                    } else {
                        // the document doesn't exist
                        null
                    }
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllBooks(exUid: String = ""): List<FirestoreBookDetails> {
        val uid = if (exUid != "") exUid else currentUserId().toString()

        Log.d("FirebaseUtils [getAllBooks]: ", uid)

        return try {
            withContext(Dispatchers.IO) {
                if (uid != null) {
                    val libraryReference = firestore.collection("users").document(uid)
                        .collection("library")
                    val querySnapshot = libraryReference.get().await()
                    Log.d("BookDetailsViewModel", "Book details being turned to object!")
                    querySnapshot.documents.mapNotNull { document ->
                        document.toObject(FirestoreBookDetails::class.java)
                    }
                } else {
                    Log.d("BookDetailsViewModel", "No user logged in!")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("BookDetailsViewModel", "Exception: ${e.localizedMessage}")
            emptyList()
        }
    }

    fun listenBookshelfUpdates(
        onChange: (Map<String, List<FirestoreBookDetails>>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val uid = currentUserId() ?: return
        val libraryReference = firestore.collection("users").document(uid).collection("library")

        libraryReference.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirebaseUtils", "Listen failed.", e)
                onError(e)
                return@addSnapshotListener
            }

            val changeMap = mutableMapOf(
                "add" to mutableListOf<FirestoreBookDetails>(),
                "modify" to mutableListOf(),
                "remove" to mutableListOf()
            )

            snapshot?.documentChanges?.forEach { dc ->
                when (dc.type) {
                    DocumentChange.Type.ADDED -> changeMap["add"]?.add(
                        dc.document.toObject(
                            FirestoreBookDetails::class.java
                        )
                    )

                    DocumentChange.Type.MODIFIED -> changeMap["modify"]?.add(
                        dc.document.toObject(
                            FirestoreBookDetails::class.java
                        )
                    )

                    DocumentChange.Type.REMOVED -> changeMap["remove"]?.add(
                        dc.document.toObject(
                            FirestoreBookDetails::class.java
                        )
                    )
                }
            }
            onChange(changeMap)
        }
    }

    suspend fun addToQueue(): Boolean {

        Log.d("AUTH", "Current user: ${currentUserId()}")

        Log.d("FIRESTORE INFO", "add new to matchmaking")

        //val user = currentUserDetails()?.get()?.await()

        //val userUID = user?.getString("uid").toString()
        //val username = user?.getString("username").toString()



        val userUID = currentUserId().toString()

        val timestamp = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        val thisUser = mapOf(
            "username" to "Lamina",
            "uid" to userUID,
            "profilePictureUrl" to "https://picsum.photos/600/400",
            "added" to timestamp
        )


        if (userUID != null) {
            firestore.collection("matchmaking")
                .document(userUID)
                .set(thisUser)
                .await()
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

        return users
    }
}