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

class BookDetailsViewModel: ViewModel() {
    private var _bookDetailsState = MutableStateFlow<BookDetailsState>(BookDetailsState(null, false, null))
    val bookDetailsState: StateFlow<BookDetailsState> = _bookDetailsState

    private var _assignedGenre = MutableStateFlow<String>("")
    val assignedGenre: StateFlow<String> = _assignedGenre

    fun getBookDetails(bookId: String) {
        viewModelScope.launch {
            try {
                _bookDetailsState.value = BookDetailsState(null, true, null) // emit new state
                Log.d("BookDetailsViewModel", "Fetching book details for book ID: $bookId")
                val response = GBSeachClient.gbSearchService.getBookDetails(bookId)
                if (response.isSuccessful) {
                    _bookDetailsState.value.bookDetails = response.body()
                    if (_bookDetailsState.value.bookDetails?.volumeInfo?.description != null)
                        _bookDetailsState.value.bookDetails?.volumeInfo?.description =
                            HtmlCompat.fromHtml(_bookDetailsState.value.bookDetails?.volumeInfo?.description!!,
                                HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    _bookDetailsState.value.bookDetails?.assignedGenre = _assignedGenre.value
                    Log.d("BookDetailsViewModel", "Book details fetched successfully!")
                    _bookDetailsState.value = BookDetailsState(_bookDetailsState.value.bookDetails, false, null)
                } else {
                    Log.d("BookDetailsViewModel", "Error fetching book details: ${response.message()}")
                    _bookDetailsState.value = BookDetailsState(null, false, response.message())
                }
                Log.d("BookDetailsViewModel", "Book details fetching completed!")
            } catch (e: Exception) {
                Log.d("BookDetailsViewModel", "Error fetching book details from server: ${e.localizedMessage}")
                _bookDetailsState.value = BookDetailsState(null, false, e.localizedMessage)
            }
        }
    }

    fun setAssignedGenre(genre: String) {
        _assignedGenre.value = genre
        if (_bookDetailsState.value.bookDetails != null) {
            _bookDetailsState.value.bookDetails?.assignedGenre = genre
        }
    }

}