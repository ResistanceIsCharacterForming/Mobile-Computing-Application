package com.example.shelfship.views

import com.example.shelfship.viewmodels.BookshelfViewModel
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shelfship.R
import com.example.shelfship.BuildConfig
import kotlinx.coroutines.launch


class BookshelfActivity : AppCompatActivity() {
    lateinit var bookshelfViewModel: BookshelfViewModel
    val api = BuildConfig.BOOKS_API_KEY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookshelf)

        val autharizeBtn: Button = findViewById(R.id.autharize_books_button)
        val successTextView: TextView = findViewById(R.id.placeholder_success)

        bookshelfViewModel = BookshelfViewModel()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                bookshelfViewModel.authorizationState.collect { state ->
                    if (state.success) {
                        successTextView.visibility = View.VISIBLE
                    } else {
                        state.errorMessage?.let { error ->
                            Toast.makeText(this@BookshelfActivity, error, Toast.LENGTH_LONG).show()
                        }
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