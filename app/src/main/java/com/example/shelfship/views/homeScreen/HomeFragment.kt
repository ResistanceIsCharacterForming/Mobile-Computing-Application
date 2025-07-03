package com.example.shelfship.views.homeScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shelfship.R
import com.example.shelfship.databinding.FragmentHomeBinding
import com.example.shelfship.viewmodels.HomeViewModel
import com.example.shelfship.views.matchWindow.MatchFragment

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.btnChatNow.setOnClickListener {
            val existing = childFragmentManager.findFragmentById(R.id.matchWindowContainer)
            if (existing == null) {

                viewModel.startMatchMaking()


                childFragmentManager.beginTransaction()
                    .replace(R.id.matchWindowContainer, MatchFragment())
                    .commit()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*Log.d("ChatFragment", "onViewCreated: entered")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatMessages.collect { chatSession ->
                    Log.d("ChatFragment", "Received messages: ${chatSession.messages.size}")
                    adapter.setMessages(chatSession.messages)
                    binding.listView.scrollToPosition(chatSession.messages.size - 1)
                }
            }
        }*/

        //refreshPlaceList()
    }


companion object {
    //private const val chatUUI = ""

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaceListFragment.
     */
    fun newInstance(): HomeFragment {
        return HomeFragment()
    }
}
}