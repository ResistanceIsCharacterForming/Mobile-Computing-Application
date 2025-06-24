package com.example.shelfship.views

import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GBSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gbsearch)

        val searchBar = findViewById<SearchBar>(R.id.search_bar)
        val genreDropdown =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.genre_dropdown)
        val genreDropdownMenu = genreDropdown.findViewById<AutoCompleteTextView>(R.id.genre_menu)
        val searchView = findViewById<SearchView>(R.id.search_view)
        val progressBar = findViewById<ProgressBar>(R.id.searching_progress_bar)
        val recyclerView = findViewById<RecyclerView>(R.id.search_results)

        var adapter = RowRecyclerViewAdapter(arrayListOf<GBSearchBook>())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.adapter = adapter

        val viewModel = ViewModelProvider(this).get(GBSearchViewModel::class.java)

        searchView.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString()
                val subject = genreDropdown.editText?.text.toString()
                viewModel.searchBooks(query, subject)
                Log.d("SearchActivity", "Searched for: $query and $subject")
                return@setOnEditorActionListener true
            }
            false
        }

        searchView.addTransitionListener { searchView, previousState, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                // SearchView is dismissed
                adapter.clearItems()
                viewModel.resetQuery()
            }
        }

        searchView.setupWithSearchBar(searchBar)


        genreDropdownMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedGenre = parent.getItemAtPosition(position).toString()
            viewModel.setSubject(selectedGenre)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.query.collect { query ->
                    if (query.isNotEmpty()) {
                        searchView.setText(query.toString())
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subject.collect { subject ->
                    genreDropdownMenu.setText(subject.toString(), false)
                }
            }
        }

        lifecycleScope.launch {
            Log.d("SearchActivity", "Collecting search results")
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { searchState ->
                    if (searchState.loading) {
                        //TODO (Fix) CURRENTLY NOT WORKING
                        progressBar.visibility = ProgressBar.VISIBLE
                    } else {
                        progressBar.visibility = ProgressBar.GONE
                        if (searchState.error != null) {
                            Toast.makeText(
                                this@GBSearchActivity,
                                "Error: ${searchState.error}",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                        else if (searchState.searchResults.totalItems == 0) adapter.clearItems()
                        else {
                            adapter.updateSearchResults(searchState.searchResults.items)
                        }
                    }
                }
            }
        }
    }
}
