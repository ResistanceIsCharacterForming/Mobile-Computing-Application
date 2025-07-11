package com.example.shelfship.views

import android.content.Intent
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
import com.example.shelfship.utils.FriendsRecyclerViewAdapter
import com.example.shelfship.viewmodels.FriendScreenViewModel
import com.example.shelfship.views.chatScreen.ChatActivity
import kotlinx.coroutines.launch

class FriendsFragment : Fragment(R.layout.fragment_friends) {

    private lateinit var progressBar: ProgressBar
    private lateinit var friendsRecyclerView: RecyclerView
    private val viewModel: FriendScreenViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progress_bar)
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)

        val friendsRecyclerViewAdapter = FriendsRecyclerViewAdapter()

        friendsRecyclerViewAdapter.setOnChatClickListener(
            object : FriendsRecyclerViewAdapter.onChatClickListener {
                override fun onChatClick(sessionID: String) {
                    if (sessionID.isNotEmpty()) {
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra("chatUUI", sessionID)
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(requireContext(), "Session ID not valid.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        friendsRecyclerViewAdapter.setOnItemClickListener(
            object : FriendsRecyclerViewAdapter.onItemClickListener {
                override fun onItemClick(uid: String) {
                    val intent = Intent(requireContext(), OtherUserProfileActivity::class.java)
                    intent.putExtra("userId", uid)
                    startActivity(intent)
                }
            }
        )


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
