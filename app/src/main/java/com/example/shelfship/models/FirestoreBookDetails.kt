package com.example.shelfship.models

// all values are assigned default values because firebase requires a no argument constructor
data class FirestoreBookDetails(
    val id: String = "",
    val title: String = "",
    val thumbnail: String? = null,
    var assignedGenre: String = "",
    val ownerBookShelves: List<Boolean> = listOf(false, false, false, false),
    val userRating: Int = 0
    // ownerBookShelves[0] = favorites
    // ownerBookShelves[1] = simply read
    // ownerBookShelves[2] = currently reading
    // ownerBookShelves[3] = wishlist
)
