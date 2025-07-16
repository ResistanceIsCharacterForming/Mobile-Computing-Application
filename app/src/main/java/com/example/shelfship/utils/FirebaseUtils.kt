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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CancellationException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

// NOTE: This pair (||) has been added to quickly move between sections when using the search.

// This is an object housing all the functions that need to interact directly with Firestore.
object FirebaseUtils {

    // firestore should be private ideally.
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val isLoggedIn: Boolean get() = auth.currentUser != null


    // REMOVE THIS LATER
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

        for (i in 1..20) {

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



    // || USERS AND MISC

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



    suspend fun userExists(uid: String): Boolean {
        return firestore.collection("users").document(uid).get().await().exists()
    }

    // || BOOKSHELVES

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

    // || FRIENDS

    // Function to add a empty document in social for a user.
    suspend fun existsSocial(uid: String) {

        val document = firestore.collection("social").document(uid)
        val snapshot = document.get().await()

        if (!snapshot.exists()) {
            val initialSocialData = mapOf(
                "allFriends" to emptyMap<String, Map<String, String>>(),
                "friendRequests" to emptyMap<String, Map<String, String>>(),
                "ownRequests" to emptyMap<String, Map<String, String>>()
            )

            document.set(initialSocialData).await()
        }
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

    // || MATCHMAKING

    // This is where we get an object of the 10 user with the oldest added fields from matchmaking.
    suspend fun fetchFromMatchmaking(): QuerySnapshot? {
        // I wrap this entire function into a try-catch. I would need two otherwise, one for firestore.collection() and one for collection.whereNotEqualTo()
        try {
            // Get all documents in matchmaking collection.
            val collection = firestore.collection("matchmaking")

            val userUID = currentUserId().toString()

            // We sort by the added field, and get ten from our collection.
            val snapshot = collection.whereNotEqualTo("uid", userUID)
                .orderBy("added", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .await()

            // Send the QuerySnapshot back to the ViewModel.
            return snapshot
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("fetchFromMatchmaking", "Exception: ${e.localizedMessage}")
            return null
        }
    }

    // This function is called when the user clicks the button to start matchmaking. We add them to the matchmaking collection in Firestore.
    suspend fun addToQueue(): Boolean {
        // I wrap this entire function into a try-catch. I would need two otherwise, one for the currentUserDetails() call, and one for the firestore.collection() call.
        try {
            // Call currentUserDetails() and await a response. Then convert the response into an object.
            val user = currentUserDetails()?.get()?.await()?.toObject(UserData::class.java)

            // Fetch the values we need from the user.
            val userUID = (user?.uid).toString()
            val userName = (user?.username).toString()
            val userprofilePicture = (user?.profilePictureUrl).toString()

            // Important. 'yyyy-MM-dd HH:mm:ss', this format lets us sort the documents in matchmaking later.
            val timestamp = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())

            // Set up a map with the values we fetched.
            val thisUser = mapOf(
                "username" to userName,
                "uid" to userUID,
                "profilePictureUrl" to userprofilePicture,
                "added" to timestamp
            )

            // Either make a new document in matchmaking or update the existing document.
            firestore.collection("matchmaking")
                .document(userUID)
                .set(thisUser)
                .await()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("addToQueue", "Exception: ${e.localizedMessage}")

            return false
        }
    }

    // Send a friend request and remove the user from the matchmaking.
    suspend fun sendRequest(target: Match) {

        // Build the FriendUserData and send it to sendFriendRequest.
        val ourUid = currentUserId()
        val data = FriendUserData(
            displayName = target.username,
            uid = target.uid,
            sessionID = (ourUid + target.uid).toString()
        )
        Log.d("sendRequest -- data", data.toString())
        sendFriendRequest(data)

        // Remove the logged in user from matchmaking.
        try {
            if (ourUid != null) {
                firestore.collection("matchmaking").document(ourUid).delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("sendRequest", "Exception: ${e.localizedMessage}")
        }
    }

    // || CHATTING

    // In the sessions collection add a new entry to the messages array of a given document.
    suspend fun saveMessageToFirebase(
        uid: String,
        userMessage: String,
        timestamp: String,
        systemOwner: Boolean = false
    ): Boolean {
        // Is this message sent by a user (person) or the system? Programmatically.
        var owner = ""
        if (systemOwner.equals(true)) {
            owner = "System"
        } else {
            val user = currentUserDetails()?.get()?.await()
            owner = user?.getString("username").toString()
        }

        // Set up a map of the data.
        val messageData = mapOf(
            "content" to userMessage,
            "sender" to owner,
            "timestamp" to timestamp
        )

        try {
            // Add the new data to the array.
            firestore.collection("sessions").document(uid)
                .update("messages", FieldValue.arrayUnion(messageData))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("saveMessageToFirebase", "Exception: ${e.localizedMessage}")

            return false
        }

        return true
    }


}