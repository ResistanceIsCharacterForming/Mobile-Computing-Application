package com.example.shelfship.models

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
