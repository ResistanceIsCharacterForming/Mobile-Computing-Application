package com.example.shelfship.models

data class GBVolumeInfo(
    val title: String,
    val authors: List<String> = emptyList(),
    val description: String? = null,
    val categories: List<String> = emptyList(),
    val averageRating: Double? = null,
    val publishedDate: String? = null,
    val publisher: String? = null,
    val pageCount: Int? = null,
    val language: String? = null,
    val imageLinks: GBImageLinks
)
