package com.example.a

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class FriendScreen : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var btnMyFriends: Button
    private lateinit var btnSuggestions: Button
    private lateinit var btnSearch: Button
    private lateinit var btnHamburger: ImageButton

    private val myFriends = listOf("Alice", "Bob", "Charlie")
    private val suggestions = listOf("David", "Ella")
    private val searchResults = listOf("John", "Jane", "Emily")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        btnHamburger = findViewById(R.id.btnHamburger)
        btnHamburger.setOnClickListener {
            showPopupMenu(it)
        }

        listView = findViewById(R.id.listView)
        btnMyFriends = findViewById(R.id.btnMyFriends)
        btnSuggestions = findViewById(R.id.btnSuggestions)
        btnSearch = findViewById(R.id.btnSearch)

        btnMyFriends.setOnClickListener {
            showList(
                data = myFriends,
                showMessageButton = true
            ) { name ->
                val intent = Intent(this, ChatScreen::class.java)
                intent.putExtra("friendName", name)
                startActivity(intent)
            }
        }

        btnSuggestions.setOnClickListener {
            showList(
                data = suggestions,
                showSuggestedButtons = true,
                onAcceptClick = { /* No process */ },
                onRejectClick = { /* No process */ }
            )
        }

        btnSearch.setOnClickListener {
            showList(
                data = searchResults,
                showAddButton = true,
                onAddClick = { /* No porcess */ }
            )
        }


        // Show My Friends at the begining
        showList(
            data = myFriends,
            showMessageButton = true
        ) { name ->
            val intent = Intent(this, ChatScreen::class.java)
            intent.putExtra("friendName", name)
            startActivity(intent)
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_popup, popup.menu)
        popup.menu.findItem(R.id.menu_friends)?.isVisible = false
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeScreen::class.java))
                    true
                }
                R.id.menu_messages -> {

                    true
                }
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfilePageActivity::class.java))
                    true
                }
                R.id.menu_friends -> {

                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showList(
        data: List<String>,
        showMessageButton: Boolean = false,
        showSuggestedButtons: Boolean = false,
        showAddButton: Boolean = false,
        onMessageClick: (String) -> Unit = {},
        onAcceptClick: (String) -> Unit = {},
        onRejectClick: (String) -> Unit = {},
        onAddClick: (String) -> Unit = {}
    ) {
        val adapter = FriendAdapter(
            this, data,
            showMessageButton,
            showSuggestedButtons,
            showAddButton,
            onMessageClick,
            onAcceptClick,
            onRejectClick,
            onAddClick
        )
        listView.adapter = adapter
    }
}

class FriendAdapter(
    context: android.content.Context,
    private val friends: List<String>,
    private val showMessageButton: Boolean,
    private val showSuggestedButtons: Boolean,
    private val showAddButton: Boolean,
    private val onMessageClick: (String) -> Unit,
    private val onAcceptClick: (String) -> Unit,
    private val onRejectClick: (String) -> Unit,
    private val onAddClick: (String) -> Unit
) : ArrayAdapter<String>(context, 0, friends) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false)
        val friendName = friends[position]

        val tvName = view.findViewById<TextView>(R.id.friendName)
        val btnMessage = view.findViewById<ImageButton>(R.id.btnMessage)
        val btnAccept = view.findViewById<ImageButton>(R.id.btnAccept)
        val btnReject = view.findViewById<ImageButton>(R.id.btnReject)
        val btnAdd = view.findViewById<ImageButton>(R.id.btnAdd)

        tvName.text = friendName

        // Hide all the buttons
        btnMessage.visibility = View.GONE
        btnAccept.visibility = View.GONE
        btnReject.visibility = View.GONE
        btnAdd.visibility = View.GONE

        if (showMessageButton) {
            btnMessage.visibility = View.VISIBLE
            btnMessage.setOnClickListener {
                Toast.makeText(context, "Message clicked: $friendName", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, ChatScreen::class.java)
                intent.putExtra("friendName", friendName)
                context.startActivity(intent)
            }

        }

        if (showSuggestedButtons) {
            btnAccept.visibility = View.VISIBLE
            btnReject.visibility = View.VISIBLE
            btnAccept.setOnClickListener { onAcceptClick(friendName) }
            btnReject.setOnClickListener { onRejectClick(friendName) }
        }

        if (showAddButton) {
            btnAdd.visibility = View.VISIBLE
            btnAdd.setOnClickListener { onAddClick(friendName) }
        }

        return view
    }
}

