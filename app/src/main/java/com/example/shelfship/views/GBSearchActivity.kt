package com.example.shelfship.views

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.R
import com.example.shelfship.models.GBSearchBook
import com.example.shelfship.utils.RowRecyclerViewAdapter
import com.example.shelfship.viewmodels.GBSearchViewModel
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class GBSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gbsearch)

        val searchBar = findViewById<SearchBar>(R.id.search_bar)
        val genreDropdown = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.genre_dropdown)
        val searchView = findViewById<SearchView>(R.id.search_view)
        val recyclerView = findViewById<RecyclerView>(R.id.search_results)

        var adapter = RowRecyclerViewAdapter(arrayListOf<GBSearchBook>())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        val viewModel = ViewModelProvider(this).get(GBSearchViewModel::class.java)
        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResults.collect { books ->
                    adapter.updateSearchResults(books.items)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { err ->
                    err?.let {
                        Toast.makeText(this@GBSearchActivity, it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        searchView.setupWithSearchBar(searchBar)
        searchView.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString()
                val subject = genreDropdown.editText?.text.toString()
                Log.d("SearchBooks", "Query: $query, Subject: $subject")
                viewModel.searchBooks(query, subject)
                return@setOnEditorActionListener true
            }
            false
        }
    }


}
