package com.example.shelfship.services

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.shelfship.R
import com.example.shelfship.models.SignInResult
import com.example.shelfship.models.UserData
import com.example.shelfship.utils.FirebaseUtils
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthClient {

    private val auth = FirebaseAuth.getInstance()

    suspend fun signInWithGoogle(context: Context): SignInResult {
        return try {
            val credentialResponse = getCredentialResponse(context)
            val googleIdTokenCredential = getGoogleIdTokenCredential(credentialResponse)

            if (googleIdTokenCredential != null) {
                val firebaseUser = firebaseAuthWithGoogle(googleIdTokenCredential)
                SignInResult(
                    data = UserData(
                        uid = firebaseUser.uid,
                        username = firebaseUser.displayName ?: "",
                        profilePictureUrl = firebaseUser.photoUrl?.toString() ?: "",
                        queryName = firebaseUser.displayName.toString().lowercase(),
                        aboutMe = "",
                        age = 0,
                        interests = "",
                        location = "",
                    ),
                    errorMessage = null
                )
            } else {
                SignInResult(data = null, errorMessage = "Google ID Token credential is null.")
            }
        } catch (e: Exception) {
            Log.e("GoogleAuthClient", "signInWithGoogle failed", e)
            SignInResult(data = null, errorMessage = e.localizedMessage ?: "Unknown error")
        }
    }

    private suspend fun getCredentialResponse(context: Context): GetCredentialResponse {
        val signInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return CredentialManager.create(context).getCredential(context, request)
    }

    private fun getGoogleIdTokenCredential(response: GetCredentialResponse): GoogleIdTokenCredential? {
        return try {
            val credential = response.credential
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleIdTokenCredential.createFrom(credential.data)
            } else {
                Log.e("GoogleAuthClient", "Unexpected credential type")
                null
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("GoogleAuthClient", "Invalid Google ID token", e)
            null
        }
    }

    private suspend fun firebaseAuthWithGoogle(credential: GoogleIdTokenCredential) =
        auth.signInWithCredential(
            GoogleAuthProvider.getCredential(credential.idToken, null)
        ).await().user!!
}
