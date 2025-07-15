package com.example.shelfship.views

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import com.example.shelfship.views.DrawerContent
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun DrawerScaffold(
    context: Context,
    screenTitle: String,
    content: @Composable () -> Unit = {}
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { scaffoldState.drawerState.open() }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", modifier = Modifier.size(24.dp))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Notifications clicked", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", modifier = Modifier.size(24.dp))
                    }
                }
            )
        },
        drawerContent = {
            DrawerContent(context = context) {
                scope.launch { scaffoldState.drawerState.close() }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .height(0.dp)
                .padding(paddingValues)
        ) {
        }
    }
}
