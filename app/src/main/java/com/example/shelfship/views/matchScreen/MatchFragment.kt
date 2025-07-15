package com.example.shelfship.views.matchScreen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shelfship.R
import com.example.shelfship.databinding.FragmentMatchBinding
import com.example.shelfship.viewmodels.MatchViewModel
import com.example.shelfship.views.matchScreen.MatchAdapter.MatchResultListener
import kotlinx.coroutines.launch

class MatchFragment : Fragment(), MatchResultListener {

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

        binding.btnChatNow.setOnClickListener {
            binding.btnChatNow.isEnabled = false
            viewModel.startMatchMaking()
        }

        val listView = binding.matchListView
        listView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = MatchAdapter(this)
        binding.matchListView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MatchFragment", "onViewCreated launched.")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.matchResults.collect { matchResult ->
                    if (matchResult.isNotEmpty()) {
                        Log.d("MatchFragment", "Received match results: $matchResult")
                        adapter.updateMatches(matchResult)
                        // When we fetch the results also make the card wrapping them visible.
                        binding.matchListViewWrapper.visibility = View.VISIBLE
                    } else {
                        Log.w("MatchFragment", "No data in viewModel.")
                    }
                }
            }
        }
    }

companion object {
    //private const val chatUUI = ""

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaceListFragment.
     */
    fun newInstance(): MatchFragment {
        return MatchFragment()
    }
}
}