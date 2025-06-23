package com.example.shelfship.services

import com.example.shelfship.models.GBSearchInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GBSeachClient {

    const val BASE_URL = "https://www.googleapis.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val gbSearchService = retrofit.create(GBSearchInterface::class.java)

}