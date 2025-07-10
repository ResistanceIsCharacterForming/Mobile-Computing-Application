package com.example.shelfship.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.R
import com.example.shelfship.utils.PendingRequestsRecyclerViewAdapter
import com.example.shelfship.utils.UserSearchRecyclerViewAdapter
import com.example.shelfship.viewmodels.FriendScreenViewModel
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch
import kotlin.getValue


class UserSearchFragment : Fragment(R.layout.fragment_user_search) {

    private lateinit var searchBar: SearchBar
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchingProgressBar: ProgressBar
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var pendingRequestsRecyclerView: RecyclerView
    private val viewModel: FriendScreenViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBar = view.findViewById(R.id.search_bar)
        searchView = view.findViewById(R.id.search_view)
        searchingProgressBar = view.findViewById(R.id.searching_progress_bar)
        searchResultsRecyclerView = view.findViewById(R.id.user_search_results)
        pendingRequestsRecyclerView = view.findViewById(R.id.pending_requests_recycler_view)

        val userSearchRecyclerViewAdapter = UserSearchRecyclerViewAdapter()
        userSearchRecyclerViewAdapter.setOnSendReqClickListener(
            object : UserSearchRecyclerViewAdapter.onItemClickListener {
                override fun onItemClick(uid: String, displayName: String?) {
                    viewModel.sendFriendRequest(uid, displayName!!)
                }
            }
        )
        val pendingRequestsRecyclerViewAdapter = PendingRequestsRecyclerViewAdapter()
        pendingRequestsRecyclerViewAdapter.setOnDismissClickListener(
            object : PendingRequestsRecyclerViewAdapter.onItemClickListener {
                override fun onItemClick(uid: String) {
                    viewModel.removePendingRequest(uid)
                }
            }
        )

        searchResultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchResultsRecyclerView.setHasFixedSize(true)
        searchResultsRecyclerView.setItemViewCacheSize(20)
        searchResultsRecyclerView.adapter = userSearchRecyclerViewAdapter

        searchView.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchPotentialFriends(query)
                }
                return@setOnEditorActionListener true
            }
            false
        }
        searchView.addTransitionListener { searchView, previousState, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                viewModel.clearSearchResults()
            }
        }
        searchView.setupWithSearchBar(searchBar)

        pendingRequestsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        pendingRequestsRecyclerView.setHasFixedSize(true)
        pendingRequestsRecyclerView.setItemViewCacheSize(20)
        pendingRequestsRecyclerView.adapter = pendingRequestsRecyclerViewAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sentFriendRequests.collect { sentFriendRequests ->
                    pendingRequestsRecyclerViewAdapter.updateSearchResults(sentFriendRequests)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSearching.collect {
                    searchingProgressBar.visibility = if (!it) View.GONE else View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSearchResults.collect { userSearchResults ->
                    if (userSearchResults.isEmpty()) userSearchRecyclerViewAdapter.clearItems()
                    else userSearchRecyclerViewAdapter.updateSearchResults(userSearchResults)
                }
            }
        }

    }

}