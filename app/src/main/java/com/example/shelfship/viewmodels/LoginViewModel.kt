package com.example.shelfship.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.SignInState
import com.example.shelfship.services.GoogleAuthClient
import com.example.shelfship.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ======= LOGIN =======
// Firebase + Google Sign-In integration
// https://firebase.google.com/docs/auth/android/google-signin?utm_source=studio#kotlin
// Credential Manager with Sign-In with Google (modern approach)
// https://developer.android.com/identity/sign-in/credential-manager-siwg
// Helpful YouTube tutorial
// https://www.youtube.com/watch?v=P-3GmvmIdRo

class LoginViewModel : ViewModel() {
    private val googleAuthClient = GoogleAuthClient()
    private val _signInState = MutableStateFlow(SignInState())
    val signInState: StateFlow<SignInState> = _signInState

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            val result = googleAuthClient.signInWithGoogle(context)

            result.data?.let { userData ->
                if (!FirebaseUtils.userExists(userData.uid)) {
                    FirebaseUtils.saveUserToFirestore(userData)
                }
            }

            _signInState.value = SignInState(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }

}