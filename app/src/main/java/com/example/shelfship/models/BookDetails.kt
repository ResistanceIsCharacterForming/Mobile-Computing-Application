package com.example.shelfship.models

data class BookDetails(
    val id: String,
    val volumeInfo: GBVolumeInfo,
    val assignedGenre: String? = null,
    val ownerBookShelves: ArrayList<String> = arrayListOf<String>()
)

