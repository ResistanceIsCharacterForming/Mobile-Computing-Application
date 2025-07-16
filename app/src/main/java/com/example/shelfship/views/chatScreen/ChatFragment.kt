package com.example.shelfship.views.chatScreen

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shelfship.views.chatScreen.ChatAdapter.ChatContentListener
import com.example.shelfship.viewmodels.ChatViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.shelfship.databinding.FragmentChatBinding
import com.example.shelfship.utils.FirebaseUtils
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// class for our fragment used for the chat.
class ChatFragment : Fragment(), ChatContentListener {

    // Initialize our direct binding to the layout, our viewmodel, and adapter, respectively. chatUUI is a ID for the given chat that is 'active'.
    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatUUI: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatUUI = requireArguments().getString("chatUUI") ?: error("Need chatUUI.")

        viewModel = ViewModelProvider(this, ChatViewModel.Factory(chatUUI))
            .get(ChatViewModel::class.java)

        Log.d("ChatFragment", "Created ViewModel with param: chatUUI: $chatUUI")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment




        binding = FragmentChatBinding.inflate(inflater, container, false)

        //repository = PlacesRepositoryInMemoryImpl.getInstance(true)

        binding.submitButton.setOnClickListener { newMessageEntered() }
        binding.messageField.setOnKeyListener { _: View, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    newMessageEntered()
                    return@setOnKeyListener true
                }
            }
            false
        }

        Log.d("ChatFragment", "Collecting chatMessages from ViewModel soon (tm)")

        val listView = binding.listView
        listView.layoutManager = LinearLayoutManager(context)
        adapter = ChatAdapter(this)
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
        binding.listView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ChatFragment", "onViewCreated: entered")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatMessages.collect { chatSession ->
                    Log.d("ChatFragment", "Received messages: ${chatSession.messages.size}")
                    adapter.setMessages(chatSession.messages)
                    binding.listView.scrollToPosition(chatSession.messages.size - 1)
                }
            }
        }

        //refreshPlaceList()
    }

    private fun newMessageEntered() {
        val content = binding.messageField.text
        //repository.addPlace(Place(placeName.toString()))
        //refreshPlaceList()
        lifecycleScope.launch {
                val timestampNow = DateTimeFormatter
                    .ofPattern("dd.MM.yyyy HH:mm")
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now())

                FirebaseUtils.saveMessageToFirebase(
                    uid = chatUUI,
                    userMessage = content.toString(),
                    timestamp = timestampNow
                )
            }
        binding.messageField.setText("")
    }

    override fun onDetach() {
        super.onDetach()
        //listener = null
    }

    companion object {
        //private const val chatUUI = ""
        fun newInstance(chatUUI: String): ChatFragment {
            return ChatFragment().apply {
                arguments = Bundle().apply {
                    putString("chatUUI", chatUUI)
                }
            }
        }
    }
}