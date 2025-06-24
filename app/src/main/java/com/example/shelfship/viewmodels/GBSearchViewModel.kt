package com.example.shelfship.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.GBSearchBook
import com.example.shelfship.models.GBSearchResult
import com.example.shelfship.models.GBSearchState
import com.example.shelfship.services.GBSeachClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GBSearchViewModel: ViewModel() {

    private val _searchState = MutableStateFlow(GBSearchState(GBSearchResult(0, arrayListOf<GBSearchBook>()), false, null))
    val searchState: StateFlow<GBSearchState> = _searchState

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _subject = MutableStateFlow("Fantasy")
    val subject: StateFlow<String> = _subject

    fun searchBooks(query: String, subject: String) {
        viewModelScope.launch {
            try {
                _searchState.value.loading = true
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

    fun setQuery(query: String) {
        _query.value = query
    }

    fun setSubject(subject: String) {
        _subject.value = subject
    }

    fun resetQuery() {
        _query.value = ""
    }


}