package com.example.shelfship.models

import com.example.shelfship.models.Reaction

data class Message(
    val edited: String = "",
    val id: String = "",
    val reactions: List<Reaction> = emptyList(),
    val message: String = "",
    val readby: List<String> = emptyList(),
    val sender: String = "",
    val timestamp: String = ""
)