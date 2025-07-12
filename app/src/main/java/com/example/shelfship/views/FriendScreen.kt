package com.example.shelfship.views

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shelfship.R
import com.example.shelfship.viewmodels.FriendScreenViewModel
import kotlinx.coroutines.launch

class FriendScreen : AppCompatActivity() {

    private lateinit var btnMyFriends: Button
    private lateinit var btnRequests: Button
    private lateinit var btnSearch: Button
    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var progressBar: ProgressBar

    private val viewModel: FriendScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        val composeView =
            findViewById<androidx.compose.ui.platform.ComposeView>(R.id.compose_scaffold)
        composeView.setContent {
            LargeDropdownMenuScaffold(context = this, screenTitle = "")
        }

        btnMyFriends = findViewById(R.id.btnMyFriends)
        btnRequests = findViewById(R.id.btnRequests)
        btnSearch = findViewById(R.id.btnSearch)
        fragmentContainerView = findViewById(R.id.fragmentContainer)
        progressBar = findViewById(R.id.loading_progress_bar)

        btnMyFriends.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FriendsFragment())
                .commit()
        }
        btnRequests.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FriendRequestsFragment())
                .commit()
        }
        btnSearch.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, UserSearchFragment())
                .commit()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FriendsFragment())
                .commit()
            lifecycleScope.launch {
                viewModel.populateFragments()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { isLoading ->
                    if (isLoading) {
                        progressBar.visibility = ProgressBar.VISIBLE
                        btnMyFriends.isEnabled = false
                        btnRequests.isEnabled = false
                        btnSearch.isEnabled = false
                    } else {
                        progressBar.visibility = ProgressBar.GONE
                        btnMyFriends.isEnabled = true
                        btnRequests.isEnabled = true
                        btnSearch.isEnabled = true
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.socialState.collect { socialState ->
                    viewModel.socialStateUpdated()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    if (error != null) {
                        Toast.makeText(this@FriendScreen, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle (Lifecycle.State.STARTED) {
                viewModel.uiToastMessage.collect { message ->
                    if (message != null) {
                        Toast.makeText(this@FriendScreen, message, Toast.LENGTH_SHORT).show()
                        viewModel.resetUiMessage()
                    }
                }
            }
        }
    }
}
