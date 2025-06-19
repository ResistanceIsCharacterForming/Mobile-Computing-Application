package com.example.shelfship.models

data class GoogleBooksAuthResult(
    val success: Boolean,
    val accessToken: String? = null,
    val authorizationCode: String? = null,
    val errorMessage: String? = null,
    val needsUserInteraction: Boolean = false
)