package com.example.shelfship.models

// val matches = List<Match>

data class Match(
    val username: String = "",
    val uid: String = "",
    val profilePictureUrl: String = "",
    val added: String = ""
)

data class MatchScore(
    val uid: String = "",
    val username: String = "",
    val score: Float = 0f
)