package com.example.shelfship.views.matchScreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R
import com.example.shelfship.views.LargeDropdownMenuScaffold

class MatchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        val composeView = findViewById<androidx.compose.ui.platform.ComposeView>(R.id.compose_scaffold)
        composeView.setContent {
            LargeDropdownMenuScaffold(context = this, screenTitle = "")
        }

        if (savedInstanceState == null) {
            val fragment = MatchFragment.newInstance()

            supportFragmentManager.beginTransaction()
                .replace(R.id.matchFragment, fragment)
                .commit()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MatchActivity::class.java)
            context.startActivity(intent)
        }
    }
}