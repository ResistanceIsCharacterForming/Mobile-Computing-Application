package com.example.shelfship.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.shelfship.models.FriendUserData
import com.example.shelfship.models.UserData
import com.example.shelfship.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.shelfship.utils.FirebaseUtils.getAllSocialState
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.StateFlow

// ======= List Adapter =======
// https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
// https://medium.com/androiddevelopers/adapting-to-listadapter-341da4218f5b

// ======= FIREBASE + REAL-TIME UPDATES =======
// Listen to live Firestore updates
// https://firebase.google.com/docs/firestore/query-data/listen
// Text search with Firestore: substring queries, OR, case-insensitivity
// https://stackoverflow.com/questions/46568142/google-firestore-query-on-substring-of-a-property-value-text-search
// https://firebase.google.com/docs/firestore/query-data/queries#or_queries
// https://stackoverflow.com/questions/52137652/firebase-search-query-doesnt-work-when-user-entered-value-in-lower-case-then-it
// https://stackoverflow.com/questions/50812056/making-a-firebase-query-search-not-case-sensitive

// ======= FRIENDS & SOCIAL STRUCTURES IN FIRESTORE =======
// How to model friends & friend requests
// https://stackoverflow.com/questions/73706094/firestore-social-media-friend-requests-data-model
// Transactions for safe friend request handling
// https://firebase.google.com/docs/firestore/manage-data/transactions
// Deleting fields in Firestore
// https://firebase.google.com/docs/firestore/manage-data/delete-data#fields

class FriendScreenViewModel: ViewModel() {
    private val _socialState = MutableStateFlow<Map<String, List<FriendUserData>>?>(emptyMap())
    val socialState: StateFlow<Map<String, List<FriendUserData>>?> = _socialState

    private val _friends = MutableStateFlow<List<FriendUserData>>(emptyList())
    val friends: StateFlow<List<FriendUserData>> = _friends

    private val _friendRequests = MutableStateFlow<List<FriendUserData>>(emptyList())
    val friendRequests: StateFlow<List<FriendUserData>> = _friendRequests

    private val _sentFriendRequests = MutableStateFlow<List<FriendUserData>>(emptyList())
    val sentFriendRequests: StateFlow<List<FriendUserData>> = _sentFriendRequests

    private val _uiToastMessage = MutableStateFlow<String?>(null)
    val uiToastMessage: StateFlow<String?> = _uiToastMessage

    private val _userSearchResults = MutableStateFlow<List<UserData>>(emptyList())
    val userSearchResults: StateFlow<List<UserData>> = _userSearchResults

    private val _loading = MutableStateFlow<Boolean>(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private var socialListener: ListenerRegistration? = null

    init {
        listenSocialStateUpdates()
    }

    fun populateFragments() {
        viewModelScope.launch {
            _loading.value = true
            _socialState.value = getAllSocialState()
            _loading.value = false
        }
    }

    fun socialStateUpdated() {
        if (_socialState.value == null) {
            _error.value = "Error fetching social connections!"
        }
        else {
            _friends.value = _socialState.value!!["allFriends"] ?: emptyList()
            _friendRequests.value = _socialState.value!!["friendRequests"] ?: emptyList()
            _sentFriendRequests.value = _socialState.value!!["ownRequests"] ?: emptyList()
        }
    }

    fun listenSocialStateUpdates() {
        socialListener = FirebaseUtils.listenToSocialStateChanges(
            onChange = { changeMap ->
                val allFriends = changeMap?.get("allFriends") ?: emptyList()
                val friendRequests = changeMap?.get("friendRequests") ?: emptyList()
                val ownRequests = changeMap?.get("ownRequests") ?: emptyList()
                _socialState.value = mapOf(
                    "allFriends" to allFriends,
                    "friendRequests" to friendRequests,
                    "ownRequests" to ownRequests
                )
                },
            onError = { exception ->
                _error.value = "Listen failed: $exception"
            }
        )

    }

    fun sendFriendRequest(friendUID: String, displayName: String) {
        viewModelScope.launch {
            if (friends.value.any { it.uid == friendUID }) {
                _uiToastMessage.value = "User is already your friend!"
            }
            else {
                if (friendRequests.value.any { it.uid == friendUID }) {
                    if (FirebaseUtils.acceptFriendRequest(FriendUserData(displayName, friendUID, null))) {
                        _uiToastMessage.value = "Friend request accepted!"
                    }
                    else {
                        _uiToastMessage.value = "Error accepting friend request!"
                    }
                }
                else if (sentFriendRequests.value.any { it.uid == friendUID }) {
                    _uiToastMessage.value = "Friend request already sent!"
                }
                else {
                    if (FirebaseUtils.sendFriendRequest(FriendUserData(displayName, friendUID, null))) {
                        _uiToastMessage.value = "Friend request sent!"
                    }
                    else {
                        _uiToastMessage.value = "Error sending friend request!"
                    }
                }
            }
        }
    }

    fun acceptFriendRequest(friendUID: String, displayName: String) {
        viewModelScope.launch {
            if (FirebaseUtils.acceptFriendRequest(FriendUserData(displayName, friendUID, null))) {
                _uiToastMessage.value = "Friend request accepted!"
            }
            else {
                _uiToastMessage.value = "Error accepting friend request!"
            }
            _uiToastMessage.value = null
        }
    }

    fun rejectFriendRequest(friendUID: String) {
        viewModelScope.launch {
            if (FirebaseUtils.rejectFriendRequest(friendUID)) {
                _uiToastMessage.value = "Friend request rejected!"
            }
            else {
                _uiToastMessage.value = "Error rejecting friend request!"
            }
        }
    }

    fun removePendingRequest(pendingRequestUid: String) {
        viewModelScope.launch {
            if (FirebaseUtils.removePendingRequest(pendingRequestUid)) {
                _uiToastMessage.value = "Pending request removed!"
            }
            else {
                _uiToastMessage.value = "Error removing pending request!"
            }
        }
    }

    fun searchPotentialFriends(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _userSearchResults.value = FirebaseUtils.searchUsers(query)
            _isSearching.value = false
        }
    }

    fun clearSearchResults() {
        _userSearchResults.value = emptyList()
    }

    fun resetUiMessage() {
        _uiToastMessage.value = null
    }

    override fun onCleared() {
        socialListener?.remove()
        super.onCleared()
    }
}