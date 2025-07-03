package com.example.shelfship.views.matchWindow

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shelfship.databinding.FragmentHomeBinding
import com.example.shelfship.databinding.FragmentMatchWindowBinding
import com.example.shelfship.viewModels.HomeViewModel
import com.example.shelfship.views.homeScreen.HomeFragment
import com.example.shelfship.views.matchWindow.MatchAdapter
import com.example.shelfship.views.matchWindow.MatchAdapter.MatchResultListener
import kotlinx.coroutines.launch

class MatchFragment : Fragment(), MatchResultListener {

    private lateinit var binding: FragmentMatchWindowBinding
    private lateinit var adapter: MatchAdapter

    private val viewModel: HomeViewModel by viewModels({ requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MatchFragment", "onViewCreated launched.")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.matchResults.collect { matchResult ->
                    Log.d("MatchFragment", "Received match results: $matchResult")
                    adapter.updateMatches(matchResult)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatchWindowBinding.inflate(inflater, container, false)

        val listView = binding.matchListView
        listView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = MatchAdapter(this)
        binding.matchListView.adapter = adapter

        return binding.root
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