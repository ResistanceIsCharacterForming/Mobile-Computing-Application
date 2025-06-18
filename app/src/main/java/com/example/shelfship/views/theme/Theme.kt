package com.example.shelfship.views.theme

import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.darkColors
//import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun ShelfShipTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        //
    } else {
        //
    }
/*
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )*/
}