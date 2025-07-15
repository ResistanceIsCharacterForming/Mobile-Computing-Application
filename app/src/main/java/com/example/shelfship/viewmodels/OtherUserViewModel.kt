package com.example.shelfship.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OtherUserViewModel : ViewModel() {

    private val _favorites = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val favorites: StateFlow<List<FirestoreBookDetails>> get() = _favorites

    fun getFavorites(userId: String) {
        viewModelScope.launch {
            val list = FirebaseUtils.getFavoritesForUser(userId)
            _favorites.value = list
        }
    }
}