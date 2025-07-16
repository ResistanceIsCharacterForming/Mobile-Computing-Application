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
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MatchViewModel : ViewModel() {

    private val _matchResults = MutableStateFlow<List<Match>>(emptyList())
    val matchResults: StateFlow<List<Match>> = _matchResults

    // This code is from: https://stackoverflow.com/a/22913525
    // I didn't feel I had the mathematical knowledge to implement this myself.
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
    suspend fun calculateScore(
        UID: String,
        username: String,
        profilePictureUrl: String,
        myBookshelf: List<FirestoreBookDetails>,
        yourBookshelf: List<FirestoreBookDetails>
    ): MatchScore {

        Log.i("calculateScore -- myBookshelf", myBookshelf.toString())
        Log.i("calculateScore -- yourBookshelf", yourBookshelf.toString())

        // These genres are the same for all users.
        val genres = listOf(
            "Fantasy",
            "SciFie",
            "Mystery",
            "Romance",
            "Thriller",
            "Horror",
            "Non-fiction"
        )

        // I'm using mutableListOf so I can add to them ass needed. Felt simple.
        val myRatings = mutableListOf<Int>()
        val yourRatings = mutableListOf<Int>()
        val differenceRatings = mutableListOf<Int>()

        // Loop through the genres.
        for (genre in genres) {
            // We are building a list for both users. This will have 7 entries. Each is a int.
            var value1 = 0
            var value2 = 0
            // If the book is a favorite -- ownerBookShelves[0] = true -- and its assignedGenre matches the current genre, add the userRating to the given user's value.
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
            // Here we add the total value for the whole genre.
            myRatings.add(value1)
            yourRatings.add(value2)
        }

        // If either user has a genre with no entries, make that genre count as 0 for both.
        for (index in 0..6) {
            if(myRatings[index] == 0 || yourRatings[index] == 0) {
                myRatings[index] = 0
                yourRatings[index] = 0
            }
        }

        // Build another list of 7 entries. We want user a - user b rating for each genre.
        for (index in 0..6) {
            val difference = (myRatings[index] - yourRatings[index])
            differenceRatings.add(abs(difference))
        }

        Log.i("calculateScore -- differenceRatings", differenceRatings.toString())

        // Two lines to ensure we don't divide by zero. If we do have a fallback and make the result 1.
        val maximumDifferenceRatings = differenceRatings.maxOrNull() ?: 1
        val maximumValue = if (maximumDifferenceRatings == 0) 1 else maximumDifferenceRatings

        Log.i("calculateScore -- maximumValue", maximumValue.toString())

        // Our score is a float.
        var finalScore = 0f

        // Normalize the result. Take the difference and divide it by the maximum.
        for (index in 0..6) {
            finalScore += differenceRatings[index].toFloat() / maximumValue.toFloat()
        }

        Log.i("calculateScore -- finalScore", finalScore.toString())

        // Our output.
        val output = MatchScore(
            uid = UID,
            username = username,
            profilePictureUrl = profilePictureUrl,
            score = finalScore
        )

        Log.i("calculateScore -- output", output.toString())

        // Send the output back into the waiting list.
        return output
    }

    // This is the big function handling all the matchmaking logic.
    fun startMatchMaking() {

        Log.d("TESTING", "fun startMatchMaking()")

        // We need the viewModelScope since we are calling suspend funcitons.
        viewModelScope.launch {
            // Add the logged in user to matchmaking.
            FirebaseUtils.addToQueue()

            // Get a Query object containing the 10 oldest documents in Matchmaking collection.
            val inQueue = FirebaseUtils.fetchFromMatchmaking()
            if (inQueue == null) {
                Log.w("startMatchMaking", "Something went wrong. There is no data in inQueue.")
                return@launch
            }
            Log.i("startMatchMaking -- inQueue", inQueue.toString())

            // The following code is for calculating the score of each user, in comparison to the logged in user.
            // This might be more complex than it needed to be but I wanted to experiment.
            // Each of the 10 potential matches is placed into this list.
            // Calculating each score can occur simultaneously, then we just wait for all the results.
            // Return type is a map for ease of use. We want the score itself and that user's UID.
            val matchmakingScores = mutableListOf<Deferred<MatchScore>>()
            val userBookshelves = mutableListOf<Deferred<List<FirestoreBookDetails>>>()

            // The following is my own implementation. But I want to link this since I used it to understand how to use Deferred properly:
            // https://developer.android.com/kotlin/coroutines/coroutines-adv
            // Threading / jobs feel natural for this task. Our application is not too big, but I'm thinking in terms of scaling.

            // We have the data about the users but we also need their bookshelfs, stored inside the sub collection library.
            for (thisUser in inQueue) {
                val thisUID = thisUser.getString("uid").toString()
                Log.i("startMatchMaking -- thisUID", thisUID)
                // We call getAllBooks for each user.
                userBookshelves.add(
                    async { FirebaseUtils.getAllBooks(thisUID) }
                )
            }

            // At the end we add the current user's bookshelf.
            userBookshelves.add(
                async { FirebaseUtils.getAllBooks() }
            )

            // Wait until the functions are all done and we should have their data.
            val retrievedBookshelves = userBookshelves.awaitAll()
            Log.i("startMatchMaking -- retrievedBookshelves", retrievedBookshelves.toString())

            // Not a pretty loop but it works. thisUser is mostly to get the UID. Then index is to understand who's bookshelf we are passing. Since the logged in user's bookshelf is the last entry it's easy to find.
            var index = 0
            for (thisUser in inQueue) {
                // Note: I have to redeclare both myBookshelf and yourBookshelf each time. Otherwise it would say here -- inside the loop -- that it was passing unique data. Inside the function the data acted like a reference, giving you last entry only.
                val thisUID = thisUser.getString("uid").toString()
                val myBookshelf = retrievedBookshelves[index]
                val yourBookshelf = retrievedBookshelves.last()
                // Populate matchmakingScores with calls to calculateScore. Note that this... UID/User is the person we are comparing the logged in user against.
                // Passing username and profilePictureUrl from one object to the other is a little messy. But I couldn't think of a easy other way.
                matchmakingScores.add(
                    async { calculateScore(
                        thisUID,
                        thisUser.getString("username").toString(),
                        thisUser.getString("profilePictureUrl").toString(),
                        myBookshelf,
                        yourBookshelf
                    ) }
                )
                index++
            }

            // Wait for all the scores to come back.
            var calculatedScores = matchmakingScores.awaitAll()
            // Then sort the scores. Lower is better.
            calculatedScores = calculatedScores.sortedBy { it.score }
            Log.i("startMatchMaking -- calculatedScores", calculatedScores.toString())

            // I call this index2 because of the same reason as above. Be sure it's treated as a new variable.
            var index2 = 0
            for (thisScore in calculatedScores) {
                // Only get the top 3 matches.
                if (index2 >= 3) break
                // Add our results to the StateFlow.
                _matchResults.value += Match(
                    username = thisScore.username,
                    uid = thisScore.uid,
                    profilePictureUrl = thisScore.profilePictureUrl
                )
                index2++
            }

        }
    }

}