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

class GoogleBooksAuthorizationClient {
    private val REQUEST_AUTHORIZE = 1001

    fun requestGoogleBooksAuthorization(context: Context) {
        val booksScope = Scope("https://www.googleapis.com/auth/books")

        val authorizationRequest = AuthorizationRequest.Builder()
            .setRequestedScopes(listOf(booksScope))
            .build()

        val authorizationClient = Identity.getAuthorizationClient(context)

        authorizationClient
            .authorize(authorizationRequest)
            .addOnSuccessListener { result: AuthorizationResult ->
                if (result.hasResolution()) {
                    val pendingIntent: PendingIntent? = result.pendingIntent

                    try {
                        startIntentSenderForResult(
                            context as Activity,
                            pendingIntent!!.intentSender,
                            REQUEST_AUTHORIZE,
                            null,
                            0,
                            0,
                            0,
                            null
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("GoogleBooksAuthorizationClient", "Couldn't start Authorization UI: ${e.localizedMessage}")
                    }

                } else {
                    // Access already granted — use the access token / proceed. proceedWithBooksAccess(result)
                }
            }
            .addOnFailureListener { e ->
                Log.e("GoogleBooksAuthorizationClient", "Failed to authorize", e)
            }
    }
}