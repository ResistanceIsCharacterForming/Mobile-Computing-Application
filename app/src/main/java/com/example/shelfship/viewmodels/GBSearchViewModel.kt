package com.example.shelfship.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.GBSearchBook
import com.example.shelfship.models.GBSearchResult
import com.example.shelfship.services.GBSeachClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GBSearchViewModel: ViewModel() {

    private val _searchResults = MutableStateFlow<GBSearchResult>(GBSearchResult(0, arrayListOf<GBSearchBook>()))
    val searchResults: StateFlow<GBSearchResult> = _searchResults

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun searchBooks(query: String, subject: String) {
        viewModelScope.launch {
            try {
                val response = GBSeachClient.gbSearchService.searchBooks(query, subject)
                if (response.isSuccessful) {
                    val body = response.body()
                    _searchResults.value = body?: GBSearchResult(0, arrayListOf<GBSearchBook>())
                    Log.d("SearchBooks", "Found ${body?.items?.size ?: 0} books")
                } else {
                    _error.value = "Error ${response.code()}: ${response.message()}"
                    Log.e("SearchBooks", "API Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
                Log.e("SearchBooks", "Exception: ${e.localizedMessage}", e)
            }
        }
    }
}