package com.example.shelfship.models

data class BookDetails(
    val id: String,
    val volumeInfo: GBVolumeInfo,
    var assignedGenre: String? = null,
    var ownerBookShelves: List<Boolean> = listOf<Boolean>(false, false, false, false)
)

