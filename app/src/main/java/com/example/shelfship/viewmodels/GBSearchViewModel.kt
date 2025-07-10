package com.example.shelfship.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.GBSearchBook
import com.example.shelfship.models.GBSearchResult
import com.example.shelfship.models.GBSearchState
import com.example.shelfship.services.GBSeachClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GBSearchViewModel: ViewModel() {

    private val _searchState = MutableStateFlow(GBSearchState(GBSearchResult(0, arrayListOf<GBSearchBook>()), false, null))
    val searchState: StateFlow<GBSearchState> = _searchState

    private val _subject = MutableStateFlow("Fantasy")
    val subject: StateFlow<String> = _subject

    fun searchBooks(query: String, subject: String) {
        viewModelScope.launch {
            try {
                /*
                if i simply changed the value of the loading parameter by using value.loading, the flow did not emit
                a new output which meant that the loading state was not updated.
                 */
                _searchState.value = GBSearchState(GBSearchResult(0, arrayListOf<GBSearchBook>()), true, null)
                val response = GBSeachClient.gbSearchService.searchBooks(query, subject)
                if (response.isSuccessful) {
                    val body = response.body()
                    _searchState.value = GBSearchState(body?: GBSearchResult(0, arrayListOf<GBSearchBook>()), false, null)
                    Log.d("SearchBooks", "${_searchState.value.searchResults}")
                } else {
                    _searchState.value = GBSearchState(GBSearchResult(0, arrayListOf<GBSearchBook>()), false, response.message())
                    Log.e("SearchBooks", "API Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _searchState.value = GBSearchState(GBSearchResult(0, arrayListOf<GBSearchBook>()), false, e.localizedMessage)
                Log.e("SearchBooks", "Exception: ${e.localizedMessage}", e)
            }
        }
    }

    fun setSubject(subject: String) {
        _subject.value = subject
    }


}