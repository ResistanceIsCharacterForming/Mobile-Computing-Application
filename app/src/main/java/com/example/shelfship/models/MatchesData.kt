package com.example.shelfship.models

import com.example.shelfship.models.Reaction

// val matches = List<Match>

data class Match(
    val username: String,
    val uid: String,
    val profilePictureUrl: String,
    val added: String
)