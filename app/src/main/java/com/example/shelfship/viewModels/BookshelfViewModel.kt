package com.example.shelfship.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.utils.FirebaseUtils
import com.example.shelfship.utils.FirebaseUtils.getAllBooks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class BookshelfViewModel : ViewModel() {
    var _allBooks = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())

    var _favorites = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val favorites: MutableStateFlow<List<FirestoreBookDetails>> = _favorites

    var _simplyRead = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val simplyRead: MutableStateFlow<List<FirestoreBookDetails>> = _simplyRead

    var _currentlyReading = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val currentlyReading: MutableStateFlow<List<FirestoreBookDetails>> = _currentlyReading

    var _wishlist = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val wishlist: MutableStateFlow<List<FirestoreBookDetails>> = _wishlist

    fun populateAllShelves() {
        viewModelScope.launch {
            _allBooks.value = getAllBooks()
            _favorites.value = _allBooks.value.filter { it.ownerBookShelves[0] }
            _simplyRead.value = _allBooks.value.filter { it.ownerBookShelves[1] }
            _currentlyReading.value = _allBooks.value.filter { it.ownerBookShelves[2] }
            _wishlist.value = _allBooks.value.filter { it.ownerBookShelves[3] }
        }
    }

    fun listenBookshelfUpdates() {
        FirebaseUtils.listenBookshelfUpdates(
            onChange = { changeMap ->
                val add = changeMap["add"] ?: emptyList()
                val modify = changeMap["modify"] ?: emptyList()

                _allBooks.value = add + modify
                _favorites.value = _allBooks.value.filter { it.ownerBookShelves[0] }
                _simplyRead.value = _allBooks.value.filter { it.ownerBookShelves[1] }
                _currentlyReading.value = _allBooks.value.filter { it.ownerBookShelves[2] }
                _wishlist.value = _allBooks.value.filter { it.ownerBookShelves[3] }
                },
            onError = { exception ->
                Log.e("FirebaseUtils", "Listen failedListen failed: $exception")
            })

    }


}