package com.example.shelfship.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import android.content.Context
import android.content.Intent

@Composable
fun LargeDropdownMenuScaffold(
    context: Context,
    screenTitle: String,
    content: @Composable () -> Unit = {}
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val DarkRed = Color(0xFF8C1717)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, color = Color.White) },
                backgroundColor = DarkRed,
                contentColor = Color.White,
                navigationIcon = {},

                actions = {
                    IconButton(onClick = {
                    }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
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
                                .background(DarkRed)
                        ) {
                            DropdownMenuItem(onClick = {
                                context.startActivity(Intent(context, HomeScreen::class.java))
                                isMenuOpen = false
                            }) {
                                Text("Home", color = Color.White)
                            }

                            DropdownMenuItem(onClick = {
                                context.startActivity(Intent(context, FriendScreen::class.java))
                                isMenuOpen = false
                            }) {
                                Text("Friends", color = Color.White)
                            }

                            DropdownMenuItem(onClick = {
                                context.startActivity(Intent(context, ProfilePageActivity::class.java))
                                isMenuOpen = false
                            }) {
                                Text("Profile", color = Color.White)
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
