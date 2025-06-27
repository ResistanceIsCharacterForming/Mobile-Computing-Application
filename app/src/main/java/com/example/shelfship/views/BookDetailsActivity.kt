package com.example.shelfship.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.shelfship.R
import com.example.shelfship.viewmodels.BookDetailsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: BookDetailsViewModel

    private lateinit var clearImageCover: ImageView
    private lateinit var bookTitleView: MaterialTextView
    private lateinit var averageRatingView: RatingBar
    private lateinit var authorNamesView: MaterialTextView
    private lateinit var descriptionView: MaterialTextView
    private lateinit var categoriesView: MaterialTextView
    private lateinit var publishedDateView: MaterialTextView
    private lateinit var publisherView: MaterialTextView
    private lateinit var pageCountView: MaterialTextView
    private lateinit var languageView: MaterialTextView
    private lateinit var genreDropdown: com.google.android.material.textfield.TextInputLayout
    private lateinit var genreDropdownMenu: AutoCompleteTextView
    private lateinit var seeInLibraryButton: MaterialButton

    private lateinit var loadingLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout
    private lateinit var loadingText: MaterialTextView
    private lateinit var loadingBar: ProgressBar

    private lateinit var ownerBookShelves: BooleanArray

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)

        clearImageCover = findViewById(R.id.book_cover_frame)
        bookTitleView = findViewById(R.id.book_title_frame)
        averageRatingView = findViewById(R.id.average_rating_frame)
        authorNamesView = findViewById(R.id.authors_value)
        descriptionView = findViewById(R.id.description_value)
        categoriesView = findViewById(R.id.categories_value)
        publishedDateView = findViewById(R.id.published_date_value)
        publisherView = findViewById(R.id.publisher_value)
        pageCountView = findViewById(R.id.page_count_value)
        languageView = findViewById(R.id.language_value)
        genreDropdown = findViewById(R.id.genre_dropdown)
        genreDropdownMenu = genreDropdown.findViewById<AutoCompleteTextView>(R.id.assigned_genre_menu)
        seeInLibraryButton = findViewById<MaterialButton>(R.id.see_in_library_button)

        loadingLayout = findViewById(R.id.loading_layout)
        contentLayout = findViewById(R.id.content_layout)
        loadingText = loadingLayout.findViewById(R.id.loading_text)
        loadingBar = loadingLayout.findViewById(R.id.progress_bar)

        viewModel = ViewModelProvider(this)[BookDetailsViewModel::class.java]

        val launcherIntent = intent
        if (savedInstanceState == null && launcherIntent != null) {
            // the activity is started for the first time and view model data is being initialized
            val bookId = launcherIntent.getStringExtra("book_id")
            val assignedGenre = launcherIntent.getStringExtra("assigned_genre")
            ownerBookShelves = launcherIntent.getBooleanArrayExtra("owner_book_shelves")?: booleanArrayOf(false, false, false, false)
            if (assignedGenre != null && viewModel.assignedGenre.value == "") {
                viewModel.setAssignedGenre(assignedGenre)
            }
            if (bookId != null) {
                if (ownerBookShelves.contentEquals(booleanArrayOf(false, false, false, false))) {
                    // activity is being started from the search results. thus the book should be checked if it is in the library
                    lifecycleScope.launch {
                        viewModel.fixShelvesAndGenreIfInLibrary(bookId)
                    }
                }
                viewModel.setOwnerBookShelves(ownerBookShelves)
                viewModel.getBookDetails(bookId)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookDetailsState.collect {
                    if (it.isLoading) {
                        Log.d("BookDetailsActivity", "Loading...")
                        loadingText.text = "Loading..."
                        loadingLayout.visibility = LinearLayout.VISIBLE
                        contentLayout.visibility = LinearLayout.GONE
                    }
                    else {
                        val currentBookDetails = it.bookDetails // smartcast was complaining that i am using it.bookDetails
                        if (currentBookDetails != null) {
                            if (currentBookDetails.volumeInfo.imageLinks != null) {
                                Glide.with(clearImageCover.context)
                                    .load(currentBookDetails.volumeInfo.imageLinks.thumbnail?:"")
                                    .placeholder(R.drawable.placeholder_book)
                                    .error(R.drawable.error_book)
                                    .into(clearImageCover)
                            }
                            else {
                                clearImageCover.setImageResource(R.drawable.placeholder_book)
                            }
                            bookTitleView.text = currentBookDetails.volumeInfo.title
                            averageRatingView.rating = currentBookDetails.volumeInfo.averageRating?.toFloat()?: 0f
                            authorNamesView.text = currentBookDetails.volumeInfo.authors?.joinToString(", ")?: "Unknown Author"
                            descriptionView.text = currentBookDetails.volumeInfo.description?: "Description not found"
                            categoriesView.text = currentBookDetails.volumeInfo.categories?.joinToString(", ")?: "Categories not found"
                            publishedDateView.text = currentBookDetails.volumeInfo.publishedDate?: "Date not found"
                            publisherView.text = currentBookDetails.volumeInfo.publisher?: "Publisher not found"
                            pageCountView.text = currentBookDetails.volumeInfo.pageCount?.toString()?: "Unknown page count"
                            languageView.text = currentBookDetails.volumeInfo.language?: "Language not found"
                            genreDropdownMenu.setText(currentBookDetails.assignedGenre, false)
                            loadingLayout.visibility = LinearLayout.GONE
                            contentLayout.visibility = LinearLayout.VISIBLE
                            Log.d("BookDetailsActivity", "Book details loaded successfully!")
                        }
                        else if (it.error != null) {
                            loadingText.text = "Error loading book details!"
                            loadingBar.visibility = ProgressBar.GONE
                            Log.d("BookDetailsActivity", "Error loading book details! ${it.error}")
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.assignedGenre.collect {
                    if (it != "") genreDropdownMenu.setText(it.toString(), false)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ownerBookShelves.collect {
                    ownerBookShelves = it.toBooleanArray()
                }
            }
        }

        genreDropdownMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedGenre = parent.getItemAtPosition(position).toString()
            viewModel.setAssignedGenre(selectedGenre)
        }

        seeInLibraryButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(seeInLibraryButton.context)
            val ownerBookShelvesSnapshot = ownerBookShelves.clone()
            val fallBackOwnerBookShelves = ownerBookShelvesSnapshot.clone()
            builder
                .setTitle("Add to / Remove from Library")
                .setPositiveButton("Update") { dialog, which ->
                    viewModel.setOwnerBookShelves(ownerBookShelvesSnapshot)
                    lifecycleScope.launch {
                        if (viewModel.updateLibrary()) {
                            Toast.makeText(this@BookDetailsActivity, "Library updated!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this@BookDetailsActivity, "Error updating library!", Toast.LENGTH_SHORT).show()
                            viewModel.setOwnerBookShelves(fallBackOwnerBookShelves)
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .setMultiChoiceItems(
                    arrayOf("Favorites", "Simply Read", "Currently Reading", "Wishlist"), ownerBookShelvesSnapshot) { dialog, which, isChecked -> }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
}