package com.example.shelfship.views

import com.example.shelfship.views.DrawerScaffold
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R

class HomeScreen : AppCompatActivity() {

    private lateinit var notificationList: LinearLayout
    private lateinit var matchedUsersContainer: LinearLayout
    private lateinit var chatNowButton: Button
    private lateinit var filterButton: Button
    private lateinit var filterPanel: LinearLayout
    private lateinit var parameterList: ScrollView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val composeView = findViewById<androidx.compose.ui.platform.ComposeView>(R.id.compose_scaffold)
        composeView.setContent {
            LargeDropdownMenuScaffold(context = this, screenTitle = "")
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
            filterPanel.visibility = if (filterPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
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