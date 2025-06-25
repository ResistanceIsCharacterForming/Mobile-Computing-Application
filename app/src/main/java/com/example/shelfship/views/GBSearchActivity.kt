package com.example.shelfship.views

import android.content.Intent
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
import kotlinx.coroutines.launch

class GBSearchActivity : AppCompatActivity() {

    private lateinit var viewModel: GBSearchViewModel

    private lateinit var searchBar: SearchBar
    private lateinit var genreDropdown: com.google.android.material.textfield.TextInputLayout
    private lateinit var genreDropdownMenu: AutoCompleteTextView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gbsearch)

        searchBar = findViewById<SearchBar>(R.id.search_bar)
        genreDropdown =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.genre_dropdown)
        genreDropdownMenu = genreDropdown.findViewById<AutoCompleteTextView>(R.id.genre_menu)
        searchView = findViewById<SearchView>(R.id.search_view)
        progressBar = findViewById<ProgressBar>(R.id.searching_progress_bar)
        recyclerView = findViewById<RecyclerView>(R.id.search_results)

        var adapter = RowRecyclerViewAdapter(arrayListOf<GBSearchBook>())
        adapter.setOnItemClickListener(listener = object : RowRecyclerViewAdapter.onItemClickListener {
            override fun onItemClick(item: GBSearchBook) {
                Log.d("SearchActivity", "Viewing the details of: ${item.id}")
                val intent = Intent(this@GBSearchActivity, BookDetailsActivity::class.java)
                intent.putExtra("book_id", item.id)
                intent.putExtra("assigned_genre", genreDropdownMenu.text.toString())
                startActivity(intent)
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[GBSearchViewModel::class.java]

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
