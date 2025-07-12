package com.example.shelfship.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.FirestoreBookDetails
import com.example.shelfship.models.Match
import com.example.shelfship.models.MatchScore
import com.example.shelfship.utils.FirebaseUtils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class MatchViewModel : ViewModel() {

    private val _matchResults = MutableStateFlow<List<Match>>(emptyList())
    val matchResults: StateFlow<List<Match>> = _matchResults

    fun equalListLength(toInvestigate: List<Int>, length: Int): List<Int> {
        return if (toInvestigate.size >= length) toInvestigate
        else toInvestigate + List(length - toInvestigate.size) { 0 }
    }


    // https://stackoverflow.com/a/22913525
    fun cosineSimilarity(vectorA: DoubleArray, vectorB: DoubleArray): Double {
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i].pow(2.0)
            normB += vectorB[i].pow(2.0)
        }
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }



    // myBookshelf is the FirestoreBookDetails object for a person in the queue who isn't the logged in user.
    // yourBookshelf is the FirestoreBookDetails object for the logged in user who's also in the queue.
    suspend fun calculateScore(UID: String, username: String, myBookshelf: List<FirestoreBookDetails>, yourBookshelf: List<FirestoreBookDetails>): MatchScore {

        val genres = listOf(
            "Fantasy",
            "SciFie",
            "Mystery",
            "Romance",
            "Thriller",
            "Horror",
            "Non-fiction"
        )

        val myRatings = mutableListOf<Int>()
        val yourRatings = mutableListOf<Int>()
        val differenceRatings = mutableListOf<Int>()

        for (genre in genres) {
            var value1 = 0
            var value2 = 0
            for (book in myBookshelf) {
                if (book.ownerBookShelves[0] && book.assignedGenre == genre) {
                    value1 += book.userRating
                }
            }
            for (book in yourBookshelf) {
                if (book.ownerBookShelves[0] && book.assignedGenre == genre) {
                    value2 += book.userRating
                }
            }
            myRatings.add(value1)
            yourRatings.add(value2)
        }

        for (index in 0..6) {
            if(myRatings[index] == 0 || yourRatings[index] == 0) {
                myRatings[index] = 0
                yourRatings[index] = 0
            }
        }

        for (index in 0..6) {
            val difference = (myRatings[index] - yourRatings[index])
            differenceRatings.add(difference)
        }

        val maximum = differenceRatings.max()
        var finalScore = 0f

        for (index in 0..6) {
            finalScore += differenceRatings[index].toFloat() / maximum.toFloat()
        }

        val output = MatchScore(
            uid = UID,
            username = username,
            score = finalScore
        )

        Log.d("SCOREEEE", finalScore.toString())

        return output
    }

    fun startMatchMaking() {

        Log.d("TESTING", "fun startMatchMaking()")

        viewModelScope.launch {
            FirebaseUtils.addToQueue()

            // Get a Query object containing the 10 oldest documents in Matchmaking collection.
            val inQueue = FirebaseUtils.fetchFromMatchmaking()

            // This is the scores for each user compared to the logged in user.
            // This is more complex than it needs to be but I wanted to experiment.
            // Each of the 10 potential matches is placed into this list.
            // Calculating each score can occur simultaneously, then we just wait for all the results.
            // Return type is a map for ease of use. We want the score itself and that user's UID.
            val matchmakingScores = mutableListOf<Deferred<MatchScore>>()
            val userBookshelves = mutableListOf<Deferred<List<FirestoreBookDetails>>>()

            // The following is my implementation. But I want to link this since I used it to understand how to use Deferred properly:
            // https://developer.android.com/kotlin/coroutines/coroutines-adv

            // Although there is some redundancy in running the same loop twice, it feels safer.
            // I want to get all the bookshelves from the potential 11 (logged in user + 10 in queue).

            for (thisUser in inQueue) {

                val thisUID = thisUser.getString("uid").toString()

                userBookshelves.add(
                    async { FirebaseUtils.getAllBooks(thisUID) }
                )

            }

            // At the end we add the current user's bookshelf.
            userBookshelves.add(
                async { FirebaseUtils.getAllBooks() }
            )

            val retrievedBookshelves = userBookshelves.awaitAll()

            Log.d("I AM PIF!", retrievedBookshelves.toString())

            var index = 0

            // Fetch each user (document) from inQueue object.
            for (thisUser in inQueue) {

                val thisUID = thisUser.getString("uid").toString()

                val myBookshelf = retrievedBookshelves[index]
                val yourBookshelf = retrievedBookshelves.last()

                matchmakingScores.add(
                    async { calculateScore(
                        thisUID,
                        thisUser.getString("username").toString(),
                        myBookshelf,
                        yourBookshelf
                    ) }
                )

                index++
            }





            var calculatedScores = matchmakingScores.awaitAll()

            calculatedScores = calculatedScores.sortedBy { it.score }

            Log.d("RESULTS!!", calculatedScores.toString())


            var index2 = 0
            for (thisScore in calculatedScores) {

                if (index2 >= 3) break

                Log.d("UID ff s - outside:", thisScore.score.toString())

                _matchResults.value += Match(
                    username = thisScore.username,
                    uid = thisScore.uid,
                    profilePictureUrl = ""
                )

                index2++
            }

        }
    }

}