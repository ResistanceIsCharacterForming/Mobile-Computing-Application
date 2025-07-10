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
import com.example.shelfship.utils.FriendRequestsRecyclerViewAdapter
import com.example.shelfship.viewmodels.FriendScreenViewModel
import kotlinx.coroutines.launch


class FriendRequestsFragment : Fragment(R.layout.fragment_friend_requests) {

    private lateinit var progressBar: ProgressBar
    private lateinit var friendRequestsRecyclerView: RecyclerView
    private val viewModel: FriendScreenViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendRequestsRecyclerView = view.findViewById(R.id.friend_requests_recycler_view)

        val friendRequestsRecyclerViewAdapter = FriendRequestsRecyclerViewAdapter()
        friendRequestsRecyclerViewAdapter.setOnRejectClickListener(
            object : FriendRequestsRecyclerViewAdapter.onItemClickListener {
                override fun onItemClick(uid: String, displayName: String?) {
                    viewModel.rejectFriendRequest(uid)
                }
            }
        )
        friendRequestsRecyclerViewAdapter.setOnAcceptClickListener(
            object : FriendRequestsRecyclerViewAdapter.onItemClickListener {
                override fun onItemClick(uid: String, displayName: String?) {
                    viewModel.acceptFriendRequest(uid, displayName!!)
                }
            }
        )

        friendRequestsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendRequestsRecyclerView.setHasFixedSize(true)
        friendRequestsRecyclerView.setItemViewCacheSize(20)
        friendRequestsRecyclerView.adapter = friendRequestsRecyclerViewAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.friendRequests.collect { friendRequests ->
                    friendRequestsRecyclerViewAdapter.updateSearchResults(friendRequests)
                }
            }
        }
    }

}