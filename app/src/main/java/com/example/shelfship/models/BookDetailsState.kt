package com.example.shelfship.models

data class BookDetailsState(
    var bookDetails: BookDetails?,
    var isLoading: Boolean = false,
    var error: String? = null
)
