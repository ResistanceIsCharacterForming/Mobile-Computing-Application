package com.example.shelfship.views

import BookshelfViewModel
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.shelfship.R
import com.example.shelfship.views.LoginActivity

class BookshelfActivity : AppCompatActivity() {
    lateinit var bookshelfViewModel: BookshelfViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookshelf)

        val autharizeBtn: Button = findViewById(R.id.autharize_books_button)
        val successTextView: TextView = findViewById(R.id.placeholder_success)

        bookshelfViewModel = BookshelfViewModel()
        lifecycleScope.launchWhenStarted {
            bookshelfViewModel.authorizationState.collect { state ->
                if (state.success) {
                    successTextView.visibility = View.VISIBLE
                }
                else {
                    state.errorMessage?.let { error ->
                        Toast.makeText(this@BookshelfActivity, error, Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
        autharizeBtn.setOnClickListener { bookshelfViewModel.requestGoogleBooksAuthorization(this) }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bookshelfViewModel.handleUserConsentResult(this, requestCode, data)
    }
}