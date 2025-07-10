package com.example.shelfship.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shelfship.R
import com.example.shelfship.viewmodels.LoginViewModel
import com.google.android.gms.common.SignInButton
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.example.shelfship.views.homeScreen.HomeActivity

class LoginActivity : AppCompatActivity() {
    lateinit var signInButton: SignInButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signInButton = findViewById(R.id.sign_in_button)
        val loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

       lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.signInState.collect { state ->
                    if (state.isSignInSuccessful) {
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }
                    state.signInError?.let { error ->
                        Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        signInButton.setOnClickListener {
            signInButton.isEnabled = false
            loginViewModel.signInWithGoogle(context = this)
            signInButton.isEnabled = true
        }

    }
}