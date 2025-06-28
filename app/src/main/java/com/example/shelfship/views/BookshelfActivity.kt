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
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.models.GBSearchBook
import com.example.shelfship.utils.ColumnRecyclerViewAdapter
import com.example.shelfship.utils.RowRecyclerViewAdapter
import com.example.shelfship.viewmodels.BookshelfViewModel
import com.example.shelfship.viewmodels.GBSearchViewModel
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class BookshelfActivity : AppCompatActivity() {

    private lateinit var gbSearchViewModel: GBSearchViewModel
    private lateinit var bookshelfViewModel: BookshelfViewModel

    private lateinit var searchBar: SearchBar
    private lateinit var genreDropdown: com.google.android.material.textfield.TextInputLayout
    private lateinit var genreDropdownMenu: AutoCompleteTextView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchResultsRecyclerView: RecyclerView

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var simplyReadRecyclerView: RecyclerView
    private lateinit var currentlyReadingRecyclerView: RecyclerView
    private lateinit var wishlistRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookshelf)

        searchBar = findViewById<SearchBar>(R.id.search_bar)
        genreDropdown =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.genre_dropdown)
        genreDropdownMenu = genreDropdown.findViewById<AutoCompleteTextView>(R.id.genre_menu)
        searchView = findViewById<SearchView>(R.id.search_view)
        progressBar = findViewById<ProgressBar>(R.id.searching_progress_bar)
        searchResultsRecyclerView = findViewById<RecyclerView>(R.id.search_results)
        favoritesRecyclerView = findViewById<RecyclerView>(R.id.favorites_library_recycler_view)
        simplyReadRecyclerView = findViewById<RecyclerView>(R.id.simply_read_library_recycler_view)
        currentlyReadingRecyclerView = findViewById<RecyclerView>(R.id.currently_reading_library_recycler_view)
        wishlistRecyclerView = findViewById<RecyclerView>(R.id.wishlist_library_recycler_view)

        var searchResultsAdapter = RowRecyclerViewAdapter(arrayListOf<GBSearchBook>())
        var favoritesAdapter = ColumnRecyclerViewAdapter(arrayListOf<FirestoreBookDetails>())
        var simplyReadAdapter = ColumnRecyclerViewAdapter(arrayListOf<FirestoreBookDetails>())
        var currentlyReadingAdapter = ColumnRecyclerViewAdapter(arrayListOf<FirestoreBookDetails>())
        var wishlistAdapter = ColumnRecyclerViewAdapter(arrayListOf<FirestoreBookDetails>())

        searchResultsAdapter.setOnItemClickListener(listener = object : RowRecyclerViewAdapter.onItemClickListener {
            override fun onItemClick(item: GBSearchBook) {
                Log.d("SearchActivity", "Viewing the details of: ${item.id}")
                val intent = Intent(this@BookshelfActivity, BookDetailsActivity::class.java)
                intent.putExtra("book_id", item.id)
                intent.putExtra("assigned_genre", genreDropdownMenu.text.toString())
                intent.putExtra("owner_book_shelves", booleanArrayOf(false, false, false, false))
                startActivity(intent)
            }
        })
        favoritesAdapter.setOnItemClickListener(listener = object : ColumnRecyclerViewAdapter.onItemClickListener {
            override fun onItemClick(item: FirestoreBookDetails) {
                onLibraryItemClick(item)
            }
        })
        simplyReadAdapter.setOnItemClickListener(listener = object : ColumnRecyclerViewAdapter.onItemClickListener {
            override fun onItemClick(item: FirestoreBookDetails) {
                onLibraryItemClick(item)
            }
        })
        currentlyReadingAdapter.setOnItemClickListener(listener = object : ColumnRecyclerViewAdapter.onItemClickListener {
            override fun onItemClick(item: FirestoreBookDetails) {
                onLibraryItemClick(item)
            }
        })
        wishlistAdapter.setOnItemClickListener(listener = object : ColumnRecyclerViewAdapter.onItemClickListener {
            override fun onItemClick(item: FirestoreBookDetails) {
                onLibraryItemClick(item)
            }
        })

        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultsRecyclerView.setHasFixedSize(true)
        searchResultsRecyclerView.setItemViewCacheSize(20)
        searchResultsRecyclerView.adapter = searchResultsAdapter

        favoritesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        favoritesRecyclerView.setHasFixedSize(true)
        favoritesRecyclerView.setItemViewCacheSize(20)
        favoritesRecyclerView.adapter = favoritesAdapter

        simplyReadRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        simplyReadRecyclerView.setHasFixedSize(true)
        simplyReadRecyclerView.setItemViewCacheSize(20)
        simplyReadRecyclerView.adapter = simplyReadAdapter

        currentlyReadingRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        currentlyReadingRecyclerView.setHasFixedSize(true)
        currentlyReadingRecyclerView.setItemViewCacheSize(20)
        currentlyReadingRecyclerView.adapter = currentlyReadingAdapter

        wishlistRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        wishlistRecyclerView.setHasFixedSize(true)
        wishlistRecyclerView.setItemViewCacheSize(20)
        wishlistRecyclerView.adapter = wishlistAdapter

        gbSearchViewModel = ViewModelProvider(this)[GBSearchViewModel::class.java]
        bookshelfViewModel = ViewModelProvider(this)[BookshelfViewModel::class.java]

        searchView.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString()
                val subject = genreDropdown.editText?.text.toString()
                gbSearchViewModel.searchBooks(query, subject)
                Log.d("SearchActivity", "Searched for: $query and $subject")
                return@setOnEditorActionListener true
            }
            false
        }
        searchView.addTransitionListener { searchView, previousState, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                // SearchView is dismissed
                searchResultsAdapter.clearItems()
                gbSearchViewModel.resetQuery()
            }
        }
        searchView.setupWithSearchBar(searchBar)

        genreDropdownMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedGenre = parent.getItemAtPosition(position).toString()
            gbSearchViewModel.setSubject(selectedGenre)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gbSearchViewModel.query.collect { query ->
                    if (query.isNotEmpty()) {
                        searchView.setText(query.toString())
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gbSearchViewModel.subject.collect { subject ->
                    genreDropdownMenu.setText(subject.toString(), false)
                }
            }
        }

        lifecycleScope.launch {
            Log.d("SearchActivity", "Collecting search results")
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gbSearchViewModel.searchState.collect { searchState ->
                    if (searchState.loading) {
                        progressBar.visibility = ProgressBar.VISIBLE
                    } else {
                        progressBar.visibility = ProgressBar.GONE
                        if (searchState.error != null) {
                            Toast.makeText(
                                this@BookshelfActivity,
                                "Error: ${searchState.error}",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                        else if (searchState.searchResults.totalItems == 0) searchResultsAdapter.clearItems()
                        else {
                            searchResultsAdapter.updateSearchResults(searchState.searchResults.items)
                        }
                    }
                }
            }
        }
    }

    fun onLibraryItemClick(item: FirestoreBookDetails) {
        Log.d("SearchActivity", "Viewing the details of: ${item.id}")
        val intent = Intent(this@BookshelfActivity, BookDetailsActivity::class.java)
        intent.putExtra("book_id", item.id)
        intent.putExtra("assigned_genre", item.assignedGenre)
        intent.putExtra("owner_book_shelves", item.ownerBookShelves.toBooleanArray())
        startActivity(intent)
    }
}
