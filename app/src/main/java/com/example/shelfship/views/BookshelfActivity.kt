package com.example.shelfship.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R
import com.example.shelfship.BuildConfig


class BookshelfActivity : AppCompatActivity() {

    val api_key = BuildConfig.BOOKS_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookshelf)

    }

}