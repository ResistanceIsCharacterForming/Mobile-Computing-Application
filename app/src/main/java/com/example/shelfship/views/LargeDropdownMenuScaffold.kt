package com.example.shelfship.views

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shelfship.utils.getCurrentActivityName
import com.example.shelfship.views.homeScreen.HomeActivity

@Composable
fun LargeDropdownMenuScaffold(
    context: Context,
    screenTitle: String,
    content: @Composable () -> Unit = {}
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val darkRed = Color(0xFF8C1717)

    val currentActivityName = context.getCurrentActivityName()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, color = Color.White) },
                backgroundColor = darkRed,
                contentColor = Color.White,
                navigationIcon = {},

                actions = {
                    IconButton(onClick = {
                    }) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }

                    Box {
                        IconButton(onClick = {
                            isMenuOpen = !isMenuOpen
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                        }

                        DropdownMenu(
                            expanded = isMenuOpen,
                            onDismissRequest = { isMenuOpen = false },
                            modifier = Modifier
                                .width(250.dp)
                                .background(darkRed)
                        ) {
                            if (currentActivityName != "HomeActivity") {
                                DropdownMenuItem(onClick = {
                                    context.startActivity(Intent(context, HomeActivity::class.java))
                                    isMenuOpen = false
                                }) {
                                    Text("Home", color = Color.White)
                                }
                            }

                            if (currentActivityName != "BookshelfActivity") {
                                DropdownMenuItem(onClick = {
                                    context.startActivity(Intent(context, BookshelfActivity::class.java))
                                    isMenuOpen = false
                                }) {
                                    Text("Bookshelf", color = Color.White)
                                }
                            }

                            if (currentActivityName != "FriendScreen") {
                                DropdownMenuItem(onClick = {
                                    context.startActivity(Intent(context, FriendScreen::class.java))
                                    isMenuOpen = false
                                }) {
                                    Text("Friends", color = Color.White)
                                }
                            }

                            if (currentActivityName != "ProfilePageActivity") {
                                DropdownMenuItem(onClick = {
                                    context.startActivity(Intent(context, ProfilePageActivity::class.java))
                                    isMenuOpen = false
                                }) {
                                    Text("Profile", color = Color.White)
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}
