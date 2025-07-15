package com.example.shelfship.views

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shelfship.R
import com.example.shelfship.utils.FavoritesAdapter
import com.example.shelfship.utils.FirebaseUtils
import com.example.shelfship.viewmodels.OtherUserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OtherUserProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var aboutMeTextView: TextView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var removeFriendButton: Button

    private lateinit var viewModel: OtherUserViewModel
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)

        viewModel = ViewModelProvider(this)[OtherUserViewModel::class.java]

        findViewById<ComposeView>(R.id.compose_scaffold).setContent {
            LargeDropdownMenuScaffold(context = this, screenTitle = "")
        }

        profileImageView = findViewById(R.id.otherUserProfileImage)
        aboutMeTextView = findViewById(R.id.otherAboutMeText)
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        removeFriendButton = findViewById(R.id.removeFriendButton)

        favoritesAdapter = FavoritesAdapter()
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = favoritesAdapter

        userId = intent.getStringExtra("userId")
        if (userId != null) {
            loadUserProfile(userId!!)
            observeFavorites(userId!!)
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
        }

        removeFriendButton.setOnClickListener {
            userId?.let { friendUID ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = FirebaseUtils.removeFriend(friendUID)
                    runOnUiThread {
                        if (result) {
                            Toast.makeText(
                                this@OtherUserProfileActivity,
                                "Friend removed successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@OtherUserProfileActivity,
                                "Failed to remove friend",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeFavorites(userId: String) {
        viewModel.getFavorites(userId)
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.favorites.collect { favorites ->
                    favoritesAdapter.submitList(favorites)
                }
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profilePictureUrl = document.getString("profilePictureUrl")
                    val aboutMe = document.getString("aboutMe")
                    aboutMeTextView.text = if (!aboutMe.isNullOrBlank()) aboutMe else "No description provided."



                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profilePictureUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }


                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }


}