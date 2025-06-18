import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.GoogleBooksAuthResult
import com.example.shelfship.models.GoogleBooksAuthState
import com.example.shelfship.services.GoogleBooksAuthorizationClient
import com.example.shelfship.utils.FirebaseUtils.saveGoogleBooksAccessToken
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookshelfViewModel : ViewModel() {
    private val authClient = GoogleBooksAuthorizationClient()
    private val _autharizationState = MutableStateFlow(GoogleBooksAuthState(success = false))
    val authorizationState: StateFlow<GoogleBooksAuthState> = _autharizationState

    fun handleUserConsentResult(activity: Activity, requestCode: Int, data: Intent?) {
        if (requestCode == authClient.REQUEST_AUTHORIZE) {
            val authorizationResult: AuthorizationResult =
                Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(data)
            val result: GoogleBooksAuthResult = GoogleBooksAuthResult (
                success = true,
                needsUserInteraction = false,
                errorMessage = null,
                accessToken = authorizationResult.accessToken
            )
            handleAuthorizationResult(result)
        }
        else {
            Log.e("BookshelfViewModel", "Unknown request code: $requestCode")
            val result: GoogleBooksAuthResult = GoogleBooksAuthResult (
                success = false,
                needsUserInteraction = false,
                errorMessage = "Unknown request code",
            )
            handleAuthorizationResult(result)
        }
    }

    fun requestGoogleBooksAuthorization(activity: Activity) {
        authClient.requestGoogleBooksAuthorization(activity, object : GoogleBooksAuthorizationClient.AuthorizationCallback {
            override fun onAuthorizationResult(result: GoogleBooksAuthResult) {
                handleAuthorizationResult(result)
            }
        })
    }

    private fun handleAuthorizationResult(result: GoogleBooksAuthResult) {
        viewModelScope.launch {
            when {
                result.success && result.accessToken != null -> {
                    saveGoogleBooksAccessToken(result.accessToken)
                    _autharizationState.value = GoogleBooksAuthState(success = true, errorMessage = null)
                }
                result.needsUserInteraction -> {
                    /*
                    UI will be shown, wait for onActivityResult in the activity. The reason for this is
                    because the startIntentSenderForResult function is an asynchronous function ant it
                    will send its result to the launcher activity.
                     */
                    Log.d("BookshelfViewModel", "User interaction required for authorization")
                }
                else -> {
                    Log.e("BookshelfViewModel", "Authorization failed: ${result.errorMessage}")
                    _autharizationState.value = GoogleBooksAuthState(success = false, errorMessage = result.errorMessage)
                }
            }
        }
    }
}