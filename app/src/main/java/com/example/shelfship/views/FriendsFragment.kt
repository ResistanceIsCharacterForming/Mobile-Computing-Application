package com.example.shelfship.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shelfship.R
import com.example.shelfship.utils.FriendsRecyclerViewAdapter
import com.example.shelfship.viewmodels.FriendScreenViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class FriendsFragment : Fragment(R.layout.fragment_friends) {

    private lateinit var progressBar: ProgressBar
    private lateinit var friendsRecyclerView: RecyclerView
    private val viewModel: FriendScreenViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progress_bar)
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)

        val friendsRecyclerViewAdapter = FriendsRecyclerViewAdapter()
        friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendsRecyclerView.setHasFixedSize(true)
        friendsRecyclerView.setItemViewCacheSize(20)
        friendsRecyclerView.adapter = friendsRecyclerViewAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.friends.collect { friends ->
                    friendsRecyclerViewAdapter.updateSearchResults(friends)
                }
            }
        }
    }

}