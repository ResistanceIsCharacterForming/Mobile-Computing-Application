package com.example.shelfship.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.shelfship.models.BookDetailsState
import com.example.shelfship.services.GBSeachClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.core.text.HtmlCompat
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.utils.FirebaseUtils

class BookDetailsViewModel : ViewModel() {

    private var _bookDetailsState = MutableStateFlow<BookDetailsState>(BookDetailsState(null, false, null))
    val bookDetailsState: StateFlow<BookDetailsState> = _bookDetailsState

    private var _assignedGenre = MutableStateFlow<String>("")
    val assignedGenre: StateFlow<String> = _assignedGenre

    private var _ownerBookShelves = MutableStateFlow<List<Boolean>>(listOf(false, false, false, false))
    val ownerBookShelves: StateFlow<List<Boolean>> = _ownerBookShelves

    /**
     * Fetches detailed book information from Google Books API and updates the UI state.
     */
    fun getBookDetails(bookId: String) {
        viewModelScope.launch {
            try {
                Log.d("BookDetailsViewModel", "Fetching book details for book ID: $bookId")
                _bookDetailsState.value = BookDetailsState(null, true, null) // Loading state

                val response = GBSeachClient.gbSearchService.getBookDetails(bookId)
                if (response.isSuccessful) {
                    _bookDetailsState.value.bookDetails = response.body()

                    _bookDetailsState.value.bookDetails?.volumeInfo?.description?.let {
                        _bookDetailsState.value.bookDetails?.volumeInfo?.description =
                            HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    }
                    _bookDetailsState.value.bookDetails?.assignedGenre = _assignedGenre.value

                    Log.d("BookDetailsViewModel", "Book details fetched successfully.")
                    _bookDetailsState.value = BookDetailsState(_bookDetailsState.value.bookDetails, false, null)
                } else {
                    Log.w("BookDetailsViewModel", "API response unsuccessful: ${response.message()}")
                    _bookDetailsState.value = BookDetailsState(null, false, response.message())
                }

                Log.d("BookDetailsViewModel", "Finished handling book details response.")
            } catch (e: Exception) {
                Log.e("BookDetailsViewModel", "Exception during book fetch: ${e.localizedMessage}")
                _bookDetailsState.value = BookDetailsState(null, false, e.localizedMessage)
            }
        }
    }

    /**
     * If the book is already in the library, pre-fill genre and shelf info from Firestore.
     */
    suspend fun fixShelvesAndGenreIfInLibrary(bookId: String) {
        val lightweightBookDetails = FirebaseUtils.getBookFromLibrary(bookId)
        if (lightweightBookDetails != null) {
            Log.d("BookDetailsViewModel", "Book found in library, updating genre and shelves.")
            setOwnerBookShelves(lightweightBookDetails.ownerBookShelves.toBooleanArray())
            setAssignedGenre(lightweightBookDetails.assignedGenre)
        } else {
            Log.d("BookDetailsViewModel", "Book not found in library.")
        }
    }

    /**
     * Updates the assigned genre and propagates it to bookDetails if present.
     */
    fun setAssignedGenre(genre: String) {
        _assignedGenre.value = genre
        _bookDetailsState.value.bookDetails?.assignedGenre = genre
        Log.d("BookDetailsViewModel", "Genre set to $genre")
    }

    /**
     * Updates the user shelves and syncs it with bookDetails if present.
     */
    fun setOwnerBookShelves(ownerBookShelves: BooleanArray) {
        _ownerBookShelves.value = ownerBookShelves.toList()
        _bookDetailsState.value.bookDetails?.ownerBookShelves = ownerBookShelves.toList()
        Log.d("BookDetailsViewModel", "Updated owner shelves: ${ownerBookShelves.toList()}")
    }

    /**
     * Pushes the current book details to Firestore. Returns success status.
     */
    suspend fun updateLibrary(): Boolean {
        _bookDetailsState.value.bookDetails?.let { book ->
            Log.d("BookDetailsViewModel", "Attempting to update library with current book details.")
            val lightWeightBookDetails = FirestoreBookDetails(
                book.id,
                book.volumeInfo.title,
                book.volumeInfo.imageLinks?.thumbnail ?: "",
                book.assignedGenre.toString(),
                book.ownerBookShelves
            )
            val success = FirebaseUtils.updateLibrary(lightWeightBookDetails)
            Log.d("BookDetailsViewModel", "Library update result: $success")
            return success
        }
        Log.e("BookDetailsViewModel", "Cannot update library: bookDetails is null.")
        return false
    }
}
