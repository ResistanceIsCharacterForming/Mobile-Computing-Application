package com.example.shelfship.models

data class UserData(
    val uid: String,
    val username: String,
    val profilePictureUrl: String,
    val googleBooksAccessToken: String? = null,
    val googleBooksRefreshToken: String? = null,
    val googleBooksTokenExpiry: Long? = null,
    val hasGoogleBooksAccess: Boolean = false,
    val googleBooksLastSyncTime: Long? = null
)
