package com.example.shelfship.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.example.shelfship.R
import com.example.shelfship.views.theme.ShelfShipTheme
import com.example.shelfship.views.ShelfShipApp
import com.example.shelfship.ShelfShipApplication

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            ShelfShipTheme {
                ShelfShipApp(appContainer = (application as ShelfShipApplication))
            }
        }
    }
}