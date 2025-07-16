package com.example.shelfship.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.utils.FirebaseUtils
import com.example.shelfship.utils.FirebaseUtils.getAllBooks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.StateFlow

// ======= LISTS, IMAGES =======
// RecyclerView usage
// https://developer.android.com/develop/ui/views/layout/recyclerview

// Context access in adapter (for Glide, etc.)
// https://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter

// Image loading/caching lib options
// https://www.geeksforgeeks.org/android/image-loading-caching-library-android-set-2/

// ======= List Adapter =======
// https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
// https://medium.com/androiddevelopers/adapting-to-listadapter-341da4218f5b

// ======= FIREBASE + REAL-TIME UPDATES =======
// Listen to live Firestore updates
// https://firebase.google.com/docs/firestore/query-data/listen

class BookshelfViewModel : ViewModel() {
    private val _allBooks = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())

    private val _favorites = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val favorites: StateFlow<List<FirestoreBookDetails>> = _favorites

    private val _simplyRead = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val simplyRead: StateFlow<List<FirestoreBookDetails>> = _simplyRead

    private val _currentlyReading = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val currentlyReading: StateFlow<List<FirestoreBookDetails>> = _currentlyReading

    private val _wishlist = MutableStateFlow<List<FirestoreBookDetails>>(emptyList())
    val wishlist: StateFlow<List<FirestoreBookDetails>> = _wishlist

    private var bookshelfListener: ListenerRegistration? = null

    init {
        listenBookshelfUpdates()
    }

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
        bookshelfListener = FirebaseUtils.listenBookshelfUpdates(
            onChange = { changeMap ->
                val currentList = _allBooks.value.toMutableList()

                changeMap["remove"]?.forEach { removed ->
                    currentList.removeAll { it.id == removed.id }
                }
                changeMap["modify"]?.forEach { modified ->
                    val index = currentList.indexOfFirst { it.id == modified.id }
                    if (index != -1) {
                        currentList[index] = modified
                    }
                }
                changeMap["add"]?.forEach { added ->
                    // Prevent duplicates
                    if (currentList.none { it.id == added.id }) {
                        currentList.add(added)
                    }
                }

                _allBooks.value = currentList
                _favorites.value = _allBooks.value.filter { it.ownerBookShelves[0] }
                _simplyRead.value = _allBooks.value.filter { it.ownerBookShelves[1] }
                _currentlyReading.value = _allBooks.value.filter { it.ownerBookShelves[2] }
                _wishlist.value = _allBooks.value.filter { it.ownerBookShelves[3] }
                },
            onError = { exception ->
                Log.e("FirebaseUtils", "Listen failedListen failed: $exception")
            })

    }

    override fun onCleared() {
        bookshelfListener?.remove()
        super.onCleared()
    }

}