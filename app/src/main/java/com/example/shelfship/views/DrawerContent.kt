package com.example.shelfship.views

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shelfship.utils.getCurrentActivityName

@Composable
fun DrawerContent(context: Context, closeDrawer: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Menu", style = MaterialTheme.typography.h6, modifier = Modifier.padding(16.dp))
        Divider()

        DrawerItem("Home") {
            if (context.getCurrentActivityName() != "HomeScreen") {
                context.startActivity(Intent(context, HomeScreen::class.java))
            }
            closeDrawer()
        }

        DrawerItem("Friends") {
            if (context.getCurrentActivityName() != "FriendScreen") {
                context.startActivity(Intent(context, FriendScreen::class.java))
            }
            closeDrawer()
        }

        DrawerItem("Profile") {
            if (context.getCurrentActivityName() != "ProfilePageActivity") {
                context.startActivity(Intent(context, ProfilePageActivity::class.java))
            }
            closeDrawer()
        }
    }
}

@Composable
fun DrawerItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    )
}
