package com.example.shelfship.models

data class GBSearchState(
    var searchResults: GBSearchResult,
    var loading: Boolean = false,
    var error: String? = null
)
