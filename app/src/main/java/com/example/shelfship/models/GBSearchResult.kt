package com.example.shelfship.models

data class GBSearchResult(
    val totalItems: Int,
    val items: ArrayList<GBSearchBook> = arrayListOf<GBSearchBook>()
)
