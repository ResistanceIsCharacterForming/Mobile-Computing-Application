package com.example.shelfship.models

data class UserData(
    val uid: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",
    val queryName: String = "",
    val aboutMe: String = "",
    val age: Int = 0,
    val interests: String = "",
    val location: String = "",
)
