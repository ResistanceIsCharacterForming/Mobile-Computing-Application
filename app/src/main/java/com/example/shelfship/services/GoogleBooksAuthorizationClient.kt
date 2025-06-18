package com.example.shelfship.services

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.common.api.Scope
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import android.app.PendingIntent
import android.content.IntentSender
import android.util.Log
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.example.shelfship.models.GoogleBooksAuthResult

class GoogleBooksAuthorizationClient {
    val REQUEST_AUTHORIZE = 1001
    private val TAG = "GoogleBooksAuthClient"

    // Callback interface for returning results
    interface AuthorizationCallback {
        fun onAuthorizationResult(result: GoogleBooksAuthResult)
    }

    fun requestGoogleBooksAuthorization(context: Context, callback: AuthorizationCallback) {
        val booksScope = Scope("https://www.googleapis.com/auth/books")

        val authorizationRequest = AuthorizationRequest.Builder()
            .setRequestedScopes(listOf(booksScope))
            .build()

        val authorizationClient = Identity.getAuthorizationClient(context)

        authorizationClient
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                handleAuthorizationSuccess(result, context, callback)
            }
            .addOnFailureListener { exception ->
                handleAuthorizationFailure(exception, callback)
            }
    }

    private fun handleAuthorizationSuccess(
        result: AuthorizationResult,
        context: Context,
        callback: AuthorizationCallback
    ) {
        if (result.hasResolution()) {
            handleUserInteractionRequired(result, context, callback)
        } else {
            handleAuthorizationGranted(result, callback)
        }
    }

    private fun handleUserInteractionRequired(
        result: AuthorizationResult,
        context: Context,
        callback: AuthorizationCallback
    ) {
        val pendingIntent: PendingIntent? = result.pendingIntent

        if (pendingIntent != null) {
            try {
                startIntentSenderForResult(
                    context as Activity,
                    pendingIntent.intentSender,
                    REQUEST_AUTHORIZE,
                    null,
                    0,
                    0,
                    0,
                    null
                )

                // Return that user interaction is needed
                val authResult = GoogleBooksAuthResult(
                    success = false,
                    needsUserInteraction = true
                )
                callback.onAuthorizationResult(authResult)

            } catch (e: IntentSender.SendIntentException) {
                Log.e(TAG, "Couldn't start Authorization UI: ${e.localizedMessage}")
                val authResult = GoogleBooksAuthResult(
                    success = false,
                    errorMessage = "Failed to start authorization UI: ${e.localizedMessage}"
                )
                callback.onAuthorizationResult(authResult)
            }
        } else {
            val authResult = GoogleBooksAuthResult(
                success = false,
                errorMessage = "No pending intent available for authorization"
            )
            callback.onAuthorizationResult(authResult)
        }
    }

    private fun handleAuthorizationGranted(result: AuthorizationResult, callback: AuthorizationCallback) {
        // Extract the access token from the result
        val accessToken = result.accessToken

        if (accessToken != null) {
            val authResult = GoogleBooksAuthResult(
                success = true,
                accessToken = accessToken
            )
            callback.onAuthorizationResult(authResult)
        } else {
            val authResult = GoogleBooksAuthResult(
                success = false,
                errorMessage = "Authorization granted but no access token received"
            )
            callback.onAuthorizationResult(authResult)
        }
    }

    private fun handleAuthorizationFailure(exception: Exception, callback: AuthorizationCallback) {
        Log.e(TAG, "Failed to authorize", exception)
        val authResult = GoogleBooksAuthResult(
            success = false,
            errorMessage = "Authorization failed: ${exception.localizedMessage}"
        )
        callback.onAuthorizationResult(authResult)
    }
}