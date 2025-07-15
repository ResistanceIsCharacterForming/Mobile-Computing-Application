package com.example.shelfship.utils

import android.util.Log
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.models.FriendUserData
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CancellationException
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
            withContext(Dispatchers.IO) {
                val usersRef = firestore.collection("users").document(userData.uid)
                val socialRef = firestore.collection("social").document(userData.uid)

                firestore.runTransaction { transaction ->

                    // save user data
                    transaction.set(usersRef, userData)

                    // initialize social connections
                    val initialSocialData = mapOf(
                        "allFriends" to emptyMap<String, Any>(),
                        "friendRequests" to emptyMap<String, Any>(),
                        "ownRequests" to emptyMap<String, Any>()
                    )
                    transaction.set(socialRef, initialSocialData)

                }.await()

                Log.d("FirebaseUtils", "User and social data saved in transaction successfully!")
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FirebaseUtils", "Error saving user/social data in transaction: ${e.message}")
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
    ): ListenerRegistration? {
        val uid = currentUserId() ?: return null
        val libraryReference = firestore.collection("users").document(uid).collection("library")

        return libraryReference.addSnapshotListener { snapshot, e ->
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
                val book = dc.document.toObject(FirestoreBookDetails::class.java)
                when (dc.type) {
                    DocumentChange.Type.ADDED -> changeMap["add"]?.add(book)
                    DocumentChange.Type.MODIFIED -> changeMap["modify"]?.add(book)
                    DocumentChange.Type.REMOVED -> changeMap["remove"]?.add(book)
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

    suspend fun getAllSocialState(): Map<String, List<FriendUserData>>? {
        val uid = currentUserId()
        if (uid == null) return null

        return try {
            withContext(Dispatchers.IO) {
                val snapshot = firestore
                    .collection("social")
                    .document(uid)
                    .get()
                    .await()

                val allFriendsMap = snapshot.get("allFriends") as? Map<String, Map<String, String>> ?: emptyMap()
                val friendRequestsMap = snapshot.get("friendRequests") as? Map<String, Map<String, String>> ?: emptyMap()
                val ownRequestsMap = snapshot.get("ownRequests") as? Map<String, Map<String, String>> ?: emptyMap()

                Log.d("FriendScreenViewModel", "Own requests: $ownRequestsMap")

                val allFriendsList = allFriendsMap.map {
                    FriendUserData(it.value["displayName"].toString(), it.value["uid"].toString(),
                        it.value["sessionID"]
                    )
                }
                val friendRequestsList = friendRequestsMap.map {
                    FriendUserData(it.value["displayName"].toString(), it.value["uid"].toString(),
                        it.value["sessionID"]
                    )
                }
                val ownRequestsList = ownRequestsMap.map {
                    FriendUserData(
                        it.value["displayName"].toString(), it.value["uid"].toString(),
                        it.value["sessionID"]
                    )
                }
                val socialState = mapOf(
                    "allFriends" to allFriendsList,
                    "friendRequests" to friendRequestsList,
                    "ownRequests" to ownRequestsList
                )
                return@withContext socialState
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun listenToSocialStateChanges(
        onChange: (Map<String, List<FriendUserData>>?) -> Unit,
        onError: (Exception) -> Unit = { it.printStackTrace() }
    ): ListenerRegistration? {
        val uid = currentUserId()
        if (uid == null) return null

        return firestore
            .collection("social")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val allFriendsMap = snapshot.get("allFriends") as? Map<String, Map<String, Any>> ?: emptyMap()
                        val friendRequestsMap = snapshot.get("friendRequests") as? Map<String, Map<String, Any>> ?: emptyMap()
                        val ownRequestsMap = snapshot.get("ownRequests") as? Map<String, Map<String, Any>> ?: emptyMap()

                        val allFriendsList = allFriendsMap.map {
                            FriendUserData(
                                it.value["displayName"] as String,
                                it.value["uid"] as String,
                                it.value["sessionID"] as? String
                            )
                        }
                        val friendRequestsList = friendRequestsMap.map {
                            FriendUserData(
                                it.value["displayName"] as String,
                                it.value["uid"] as String,
                                it.value["sessionID"] as? String
                            )
                        }
                        val ownRequestsList = ownRequestsMap.map {
                            FriendUserData(
                                it.value["displayName"] as String,
                                it.value["uid"] as String,
                                it.value["sessionID"] as? String
                            )
                        }
                        val socialState = mapOf(
                        "allFriends" to allFriendsList,
                        "friendRequests" to friendRequestsList,
                        "ownRequests" to ownRequestsList
                        )
                        onChange(socialState)

                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            }
    }

    suspend fun searchUsers(query: String): List<UserData> {
        val uid = currentUserId() ?: return emptyList()
        return try {
            withContext(Dispatchers.IO) {
                val normalizedQuery = query.lowercase()
                val querySnapshot = firestore.collection("users")
                    .whereGreaterThanOrEqualTo("queryName", normalizedQuery)
                    .whereLessThanOrEqualTo("queryName", normalizedQuery + "\uf8ff")
                    .get()
                    .await()

                querySnapshot.documents.mapNotNull { document ->
                    document.toObject(UserData::class.java)
                }.filter { it.uid != uid}
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun sendFriendRequest(friendUserData: FriendUserData): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Get current user's data
                val thisClientUser = currentUserDetails()?.get()?.await()?.toObject(UserData::class.java) ?: return@withContext false

                // References to both users' social documents
                val clientSocialReference = firestore.collection("social").document(thisClientUser.uid)
                val friendSocialReference = firestore.collection("social").document(friendUserData.uid)

                firestore.runTransaction { transaction ->

                    // Check if a request already exists from the target user
                    val clientFriendRequests = transaction.get(clientSocialReference).get("friendRequests") as? Map<String, Map<String, String>> ?: emptyMap()
                    if (clientFriendRequests.containsKey(friendUserData.uid)) {
                        // If already received a request, cancel and accept instead
                        throw CancellationException("Already got a friend request from this user!")
                    }

                    // Update ownRequests for the sender
                    val clientOwnRequestsUpdate = mapOf(
                        "displayName" to friendUserData.displayName,
                        "uid" to friendUserData.uid,
                        "sessionID" to friendUserData.sessionID
                    )

                    // Update friendRequests for the receiver
                    val friendFriendRequestsUpdate = mapOf(
                        "displayName" to thisClientUser.username,
                        "uid" to thisClientUser.uid,
                        "sessionID" to null
                    )

                    transaction.update(clientSocialReference, "ownRequests.${friendUserData.uid}", clientOwnRequestsUpdate)
                    transaction.update(friendSocialReference, "friendRequests.${thisClientUser.uid}", friendFriendRequestsUpdate)
                }.await()
                return@withContext true
            }
        }
        catch (e: CancellationException) {
            // If already received a request, accept it instead
            Log.d("FirebaseUtils", "${e.message}")
            acceptFriendRequest(friendUserData)
        }
        catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun acceptFriendRequest(friendUserData: FriendUserData): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Get current user's data
                val thisClientUser = currentUserDetails()?.get()?.await()?.toObject(UserData::class.java) ?: return@withContext false

                // References to both users' social documents
                val clientSocialReference = firestore.collection("social").document(thisClientUser.uid)
                val friendSocialReference = firestore.collection("social").document(friendUserData.uid)

                // Generate a unique session ID (sorted to ensure consistency)
                val sessionID = listOf(thisClientUser.uid, friendUserData.uid).sorted().joinToString("_")

                firestore.runTransaction { transaction ->
                    // Add each user to the other's allFriends and initialize the session
                    transaction.update(clientSocialReference, "allFriends.${friendUserData.uid}", mapOf(
                        "displayName" to friendUserData.displayName,
                        "uid" to friendUserData.uid,
                        "sessionID" to sessionID
                    ))
                    transaction.update(friendSocialReference, "allFriends.${thisClientUser.uid}", mapOf(
                        "displayName" to thisClientUser.username,
                        "uid" to thisClientUser.uid,
                        "sessionID" to sessionID
                    ))
                    transaction.set(firestore.collection("sessions").document(sessionID), mapOf(
                        "participants" to listOf(thisClientUser.uid, friendUserData.uid),
                        "messages" to emptyList()
                    ))

                    // Remove old request data from both users
                    transaction.update(clientSocialReference, "friendRequests.${friendUserData.uid}", FieldValue.delete())
                    transaction.update(friendSocialReference, "ownRequests.${thisClientUser.uid}", FieldValue.delete())
                }.await()
                return@withContext true
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun rejectFriendRequest(friendUID: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val clientUid = currentUserId() ?: return@withContext false
                val clientSocialReference = firestore.collection("social").document(clientUid)
                val friendSocialReference = firestore.collection("social").document(friendUID)

                firestore.runTransaction { transaction ->
                    transaction.update(clientSocialReference, "friendRequests.$friendUID", FieldValue.delete())
                    transaction.update(friendSocialReference, "ownRequests.$clientUid", FieldValue.delete())
                }.await()
                return@withContext true
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeFriend(friendUID: String): Boolean {
        return try {
            withContext (Dispatchers.IO) {
                val clientUid = currentUserId() ?: return@withContext false

                val sessionID = listOf(clientUid, friendUID).sorted().joinToString("_")
                val clientSocialReference = firestore.collection("social").document(clientUid)
                val friendSocialReference = firestore.collection("social").document(friendUID)
                val sessionReference = firestore.collection("sessions").document(sessionID)

                firestore.runTransaction { transaction ->
                    transaction.update(clientSocialReference, "allFriends.$friendUID", FieldValue.delete())
                    transaction.update(friendSocialReference, "allFriends.$clientUid", FieldValue.delete())
                    transaction.delete(sessionReference)
                }.await()
                true
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removePendingRequest(pendingRequestUid: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val clientUid = currentUserId() ?: return@withContext false
                val clientSocialReference = firestore.collection("social").document(clientUid)
                val friendSocialReference = firestore.collection("social").document(pendingRequestUid)

                firestore.runTransaction { transaction ->
                    transaction.update(clientSocialReference, "ownRequests.$pendingRequestUid", FieldValue.delete())
                    transaction.update(friendSocialReference, "friendRequests.$clientUid", FieldValue.delete())
                }.await()
                return@withContext true
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getFavoritesForUser(uid: String): List<FirestoreBookDetails> {
        return try {
            withContext(Dispatchers.IO) {
                if (uid != null) {
                    val libraryReference = firestore.collection("users").document(uid)
                        .collection("library")
                    val querySnapshot = libraryReference.get().await()
                    Log.d("FirebaseUtils", "Book details being turned to object!")


                    querySnapshot.documents.mapNotNull { document ->
                        document.toObject(FirestoreBookDetails::class.java)
                    }.filter { book ->
                        book.ownerBookShelves.getOrNull(0) == true
                    }
                } else {
                    Log.d("FirebaseUtils", "No user logged in!")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FirebaseUtils", "Exception: ${e.localizedMessage}")
            emptyList()
        }
    }

}