package com.example.shelfship.views.matchScreen

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shelfship.databinding.FragmentMatchBinding
import com.example.shelfship.models.Match
import com.example.shelfship.utils.FirebaseUtils
import com.example.shelfship.viewmodels.MatchViewModel
import com.example.shelfship.views.FriendScreen
import com.example.shelfship.views.matchScreen.MatchAdapter.MatchResultListener
import kotlinx.coroutines.launch

// class for our fragment used for the home (match).
class MatchFragment : Fragment(), MatchResultListener {

    // Initialize our direct binding to the layout, our viewmodel, and adapter, respectively.
    private lateinit var binding: FragmentMatchBinding
    private lateinit var viewModel: MatchViewModel
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MatchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatchBinding.inflate(inflater, container, false)

        // This binding is for the 'Find Match' button. We set up a listener.
        binding.btnChatNow.setOnClickListener {
            // First turn of this button when it's clicked. Then call startMatchMaking in viewModel. Besides this viewModel isn't called inside this fragment.
            binding.btnChatNow.isEnabled = false
            viewModel.startMatchMaking()
        }

        // The RecyclerView used for the matches wont hold that many elements. At most 3. However this solution felt cleanest.
        val listView = binding.matchListView
        listView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = MatchAdapter(this)
        binding.matchListView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            // 'Listen' / wait for data to display.
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.matchResults.collect { matchResult ->
                    if (matchResult.isNotEmpty()) {
                        adapter.updateMatches(matchResult)
                        // When we fetch the results also make the card wrapping them visible.
                        binding.matchListViewWrapper.visibility = View.VISIBLE
                    } else {
                        Log.w("onViewCreated -- viewModel", "No data in viewModel.")
                    }
                }
            }
        }
    }

    // This is the logic for what happens when a match is clicked. We send a friend request, remove ourself from matchmaking, and hide the result window.
    override fun sendRequest(match: Match) {

        lifecycleScope.launch {
            Log.d("sendRequest -- match", match.toString())

            FirebaseUtils.sendRequest(match)

            // I'm not happy with this solution, but it works. Just something to complete the basic functionality.
            //The code is taken from here: https://stackoverflow.com/a/59563393
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setMessage("Request sent!")
            dialogBuilder.setPositiveButton("Go to friends.",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    context?.startActivity(
                        Intent(context, FriendScreen::class.java)
                    )
                })
            val b = dialogBuilder.create()
            b.show()

            binding.btnChatNow.isEnabled = true
            binding.matchListViewWrapper.visibility = View.GONE

        }

    }

    companion object {
        fun newInstance(): MatchFragment {
            return MatchFragment()
        }
    }
}