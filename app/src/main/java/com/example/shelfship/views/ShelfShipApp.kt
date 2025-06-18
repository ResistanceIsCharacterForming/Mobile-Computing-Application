package com.example.shelfship.views

import androidx.compose.foundation.layout.padding
//import androidx.compose.material.Scaffold
//import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.navigation.NavGraphBuilder
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.rememberNavController
import com.example.shelfship.views.ChatScreen
import com.example.shelfship.ShelfShipApplication
//import kotlinx.serialization.Serializable
//import androidx.navigation.compose.composable

enum class Route {
    Chat
}


@Composable
fun ShelfShipApp(appContainer: ShelfShipApplication) {
    // Setting up dependencies

    /*
    val navController = rememberNavController()

    Scaffold() {padding->
        NavHost(
            navController = navController,
            startDestination = Route.Chat.name,
            modifier = Modifier
                .padding(padding)
        ) {
            //routes definitions
            composable(route = Route.Chat.name) {
                ChatScreen()
            }

        }
    }*/

    ChatScreen()

}