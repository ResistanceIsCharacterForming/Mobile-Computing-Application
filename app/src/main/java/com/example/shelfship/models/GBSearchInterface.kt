package com.example.shelfship.models

import com.example.shelfship.BuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GBSearchInterface {

    companion object ApiKey {
        const val API_KEY = BuildConfig.BOOKS_API_KEY
    }

    @GET("books/v1/volumes")
    suspend fun searchBooks(@Query("q") query: String,
                            @Query("subject") subject: String,
                            @Query("startIndex") startIndex: Int = 0,
                            @Query("maxResults") maxResults: Int = 40,
                            @Query("printType") printType: String = "books",
                            @Query("langRestrict") lang: String = "en",
                            @Query("orderBy") orderBy: String = "relevance",
                            @Query("key") key: String = API_KEY): Response<GBSearchResult>

    @GET("books/v1/volumes/{id}")
    suspend fun getBookDetails(@Path("id") bookId: String,
                               @Query("key") key: String = API_KEY): Response<BookDetails>

}