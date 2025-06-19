package com.example.shelfship.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R
import androidx.core.view.isVisible

class HomeScreen : AppCompatActivity() {

    private lateinit var notificationList: LinearLayout
    private lateinit var btnHamburger: ImageButton
    private lateinit var matchedUsersContainer: LinearLayout
    private lateinit var chatNowButton: Button
    private lateinit var filterButton: Button
    private lateinit var filterPanel: LinearLayout
    private lateinit var parameterList: ScrollView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnHamburger = findViewById(R.id.btnHamburger)
        btnHamburger.setOnClickListener { view ->
            val popup = PopupMenu(this, view) // Popup
            popup.menuInflater.inflate(R.menu.menu_popup, popup.menu)
            popup.menu.findItem(R.id.menu_home)?.isVisible = false
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_profile -> {
                        startActivity(Intent(this, ProfilePageActivity::class.java))
                        true
                    }
                    R.id.menu_friends -> {
                        startActivity(Intent(this, FriendScreen::class.java))
                        true
                    }
                    R.id.menu_messages -> {

                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
        val btnNotifications = findViewById<ImageButton>(R.id.btnNotifications)
        btnNotifications.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.popup_notifications, null)
            val popupWindow = PopupWindow(popupView, 600, LinearLayout.LayoutParams.WRAP_CONTENT)
            popupWindow.elevation = 10f
            popupWindow.isFocusable = true
            popupWindow.showAsDropDown(btnNotifications, 0, 20)
        }

        notificationList = findViewById(R.id.notificationList)
        matchedUsersContainer = findViewById(R.id.matchedUsersContainer)
        chatNowButton = findViewById(R.id.btnChatNow)
        chatNowButton.setOnClickListener {
            val intent = Intent(this, QuickSearchResultActivity::class.java)
            startActivity(intent)
        }

        filterButton = findViewById(R.id.btnFilter)
        filterPanel = findViewById(R.id.filterPanel)
        parameterList = findViewById(R.id.parameterScroll)





        // filter (Costumize) button
        filterButton.setOnClickListener {
            filterPanel.visibility = if (filterPanel.isVisible) View.GONE else View.VISIBLE
        }



    }

    private fun addNotification(header: String, content: String) {
        val view = TextView(this)
        view.text = "$header: $content"
        view.setPadding(8, 8, 8, 8)
        view.setOnClickListener {
            if (header == "Friends") {
                startActivity(Intent(this, FriendScreen::class.java))
            }
        }
        notificationList.addView(view)
    }


    }

