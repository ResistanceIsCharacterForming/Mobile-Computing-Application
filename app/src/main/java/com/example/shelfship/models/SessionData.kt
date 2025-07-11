package com.example.shelfship.models

import com.example.shelfship.models.Message

data class SessionData (
    val messages: List<Message>,
    val participants: List<String>
)