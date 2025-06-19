package com.example.shelfship.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shelfship.R
import com.example.shelfship.viewmodels.LoginViewModel
import com.google.android.gms.common.SignInButton
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    lateinit var signInButton: SignInButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signInButton = findViewById(R.id.sign_in_button)
        val loginViewModel = LoginViewModel()

        lifecycleScope.launch {
            loginViewModel.signInState.collect { state ->
                if (state.isSignInSuccessful) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
                state.signInError?.let { error ->
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }

        signInButton.setOnClickListener {
            loginViewModel.signInWithGoogle(context = this)
        }

    }
}