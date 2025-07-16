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

// ======= GOOGLE BOOKS API USAGE =======
// General overview
// https://developers.google.com/books/docs/v1/getting_started

// Usage overview
// https://developers.google.com/books/docs/v1/using#:~:text=starting%20to%20code.-,Authorizing%20requests%20and%20identifying%20your%20application,using%20the%20application%27s%20API%20key.

// Book search query
// https://developers.google.com/books/docs/v1/using#PerformingSearch

// Retrofit code tutorial for api calls
// https://medium.com/@imkuldeepsinghrai/api-calls-with-retrofit-in-android-kotlin-a-comprehensive-guide-e049e19deba9
// https://medium.com/@pritam.karmahapatra/retrofit-in-android-with-kotlin-9af9f66a54a8
// https://www.youtube.com/watch?v=sRLunCZX2Uc

// ======= SEARCH UI + DROPDOWN FILTER =======
// Fix for dropdown menu filtering bug
// https://github.com/material-components/material-components-android/issues/1464

// Search bar UI specs
// https://m3.material.io/components/search/specs

// Material search bar implementation
// https://www.geeksforgeeks.org/android/material-search-bar-in-android/
// https://stackoverflow.com/questions/75996550/material-3-search-bar

// Material exposed dropdown menus
// https://github.com/material-components/material-components-android/blob/master/docs/components/Menu.md#exposed-dropdown-menus


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