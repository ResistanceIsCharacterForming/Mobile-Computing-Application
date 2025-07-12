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
import kotlin.random.Random

class MatchViewModel : ViewModel() {

    private val _matchResults = MutableStateFlow<List<Match>>(emptyList())
    val matchResults: StateFlow<List<Match>> = _matchResults

    fun equalListLength(toInvestigate: List<Int>, length: Int): List<Int> {
        return if (toInvestigate.size >= length) toInvestigate
        else toInvestigate + List(length - toInvestigate.size) { 0 }
    }

    // myBookshelf is the FirestoreBookDetails object for a person in the queue who isn't the logged in user.
    // yourBookshelf is the FirestoreBookDetails object for the logged in user who's also in the queue.
    suspend fun calculateScore(UID: String, myBookshelf: List<FirestoreBookDetails>, yourBookshelf: List<FirestoreBookDetails>): MatchScore {


        infix fun List<Int>.dot(other: List<Int>): Double {
            var out = 0.0
            for (i in indices) out += this[i] * other[i]
            return out
        }


        val myFantasy: List<Int> = myBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Fantasy" }.map { it.userRating }
        val yourFantasy: List<Int> = yourBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Fantasy" }.map { it.userRating }

        val fantasy1 = ZipUneven(myFantasy, yourFantasy)
        val fantasy2 = ZipUneven(yourFantasy, myFantasy)

        val fantasyLength = if (fantasy1.size > fantasy2.size) fantasy1.size else fantasy2.size

        val fantasy1P = equalListLength(fantasy1, fantasyLength)
        val fantasy2P = equalListLength(fantasy2, fantasyLength)

        val fantasySimilarity = fantasy1P dot fantasy2P


        val mySciFie: List<Int> = myBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "SciFie" }.map { it.userRating }
        val yourSciFie: List<Int> = yourBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "SciFie" }.map { it.userRating }

        val sciFie1 = ZipUneven(mySciFie, yourSciFie)
        val sciFie2 = ZipUneven(yourSciFie, mySciFie)

        val sciFieLength = if (sciFie1.size > sciFie2.size) sciFie1.size else sciFie2.size

        val sciFie1P = equalListLength(sciFie1, sciFieLength)
        val sciFie2P = equalListLength(sciFie2, sciFieLength)

        val sciFieSimilarity = sciFie1P dot sciFie2P


        val myMystery: List<Int> = myBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Mystery" }.map { it.userRating }
        val yourMystery: List<Int> = yourBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Mystery" }.map { it.userRating }

        val mystery1 = ZipUneven(myMystery, yourMystery)
        val mystery2 = ZipUneven(yourMystery, myMystery)

        val mysteryLength = if (mystery1.size > mystery2.size) mystery1.size else mystery2.size

        val mystery1P = equalListLength(mystery1, mysteryLength)
        val mystery2P = equalListLength(mystery2, mysteryLength)

        val mysterySimilarity = mystery1P dot mystery2P


        val myRomance: List<Int> = myBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Romance" }.map { it.userRating }
        val yourRomance: List<Int> = yourBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Romance" }.map { it.userRating }

        val romance1 = ZipUneven(myRomance, yourRomance)
        val romance2 = ZipUneven(yourRomance, myRomance)

        val romanceLength = if (romance1.size > romance2.size) romance1.size else romance2.size

        val romance1P = equalListLength(romance1, romanceLength)
        val romance2P = equalListLength(romance2, romanceLength)

        val romanceSimilarity = romance1P dot romance2P


        val myThriller: List<Int> = myBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Thriller" }.map { it.userRating }
        val yourThriller: List<Int> = yourBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Thriller" }.map { it.userRating }

        val thriller1 = ZipUneven(myThriller, yourThriller)
        val thriller2 = ZipUneven(yourThriller, myThriller)

        val thrillerLength = if (thriller1.size > thriller2.size) thriller1.size else thriller2.size

        val thriller1P = equalListLength(thriller1, thrillerLength)
        val thriller2P = equalListLength(thriller2, thrillerLength)

        val thrillerSimilarity = thriller1P dot thriller2P


        val myHorror: List<Int> = myBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Horror" }.map { it.userRating }
        val yourHorror: List<Int> = yourBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Horror" }.map { it.userRating }

        val horror1 = ZipUneven(myHorror, yourHorror)
        val horror2 = ZipUneven(yourHorror, myHorror)

        val horrorLength = if (horror1.size > horror2.size) horror1.size else horror2.size

        val horror1P = equalListLength(horror1, horrorLength)
        val horror2P = equalListLength(horror2, horrorLength)

        val horrorSimilarity = horror1P dot horror2P


        val myNonfiction: List<Int> = myBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Non-fiction" }.map { it.userRating }
        val yourNonfiction: List<Int> = yourBookshelf.filter { it.ownerBookShelves[0] && it.assignedGenre == "Non-fiction" }.map { it.userRating }

        val nonfiction1 = ZipUneven(myHorror, yourHorror)
        val nonfiction2 = ZipUneven(yourHorror, myHorror)

        val nonfictionLength = if (nonfiction1.size > nonfiction2.size) nonfiction1.size else nonfiction2.size

        val nonfiction1P = equalListLength(nonfiction1, nonfictionLength)
        val nonfiction2P = equalListLength(nonfiction2, nonfictionLength)

        val nonfictionSimilarity = nonfiction1P dot nonfiction2P


        val finalScore = fantasySimilarity
        + sciFieSimilarity
        + mysterySimilarity
        + romanceSimilarity
        + thrillerSimilarity
        + horrorSimilarity
        + nonfictionSimilarity


        val output = MatchScore(
            uid = UID,
            score = finalScore
        )

        return output
    }

    // TODO -- write here
    // https://stackoverflow.com/a/55404579
    fun ZipUneven(list1: List<Int>, list2: List<Int>): List<Int> {
        return sequence {
            val first = list1.iterator()
            val second = list2.iterator()
            while (first.hasNext() && second.hasNext()) {
                yield(first.next())
                yield(second.next())
            }
            yieldAll(first)
            yieldAll(second)
        }.toList()
    }

    fun startMatchMaking() {

        Log.d("TESTING", "fun startMatchMaking()")

        viewModelScope.launch {
            FirebaseUtils.addToQueue()

            // Get a Query object containing the 10 oldest documents in Matchmaking collection.
            val inQueue = FirebaseUtils.fetchFromMatchmaking()

            //

            // This is the scores for each user compared to the logged in user.
            // This is more complex than it needs to be but I wanted to experiment.
            // Each of the 10 potential matches is placed into this list.
            // Calculating each score can occur simultaneously, then we just wait for all the results.
            // Return type is a map for ease of use. We want the score itself and that user's UID.
            val matchmakingScores = mutableListOf<Deferred<MatchScore>>()
            val userBookshelves = mutableListOf<Deferred<List<FirestoreBookDetails>>>()

            // The following is my implementation. But I want to link this since I used it to understand how to use Deferred properly:
            // https://developer.android.com/kotlin/coroutines/coroutines-adv

            var thisUID = ""

            // Although there is some redundancy in running the same loop twice, it feels safer.
            // I want to get all the bookshelves from the potential 11 (logged in user + 10 in queue).

            for (thisUser in inQueue) {

                thisUID = thisUser.getString("uid").toString()

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

                thisUID = thisUser.getString("uid").toString()

                Log.d("PIF!", retrievedBookshelves[index].toString())
                Log.d("PIF!", retrievedBookshelves.last().toString())
                Log.d("PIF!", "")

                matchmakingScores.add(
                    async { calculateScore(
                        thisUID,
                        retrievedBookshelves[index],
                        retrievedBookshelves.last()
                    ) }
                )

                index++
            }





            var calculatedScores = matchmakingScores.awaitAll()

            calculatedScores = calculatedScores.sortedBy { it.score }


            for (thisScore in calculatedScores) {
                println("UID: ${thisScore.uid}, Score: ${thisScore.score}")
            }


            /*
            Log.d("TESTING!!", matchmakingScores.toString())


            val userA = intArrayOf(5, 3, 2, 0, 4, 1, 0, 9, 7)
            val userB = intArrayOf(4, 2, 3, 1, 5, 0, 1, 2, 0)

            val newUserA = userA.map { it.toDouble() }.toDoubleArray()
            val newUserB = userB.map { it.toDouble() }.toDoubleArray()
*/


            /* Source for this code: https://stackoverflow.com/a/64734693
            My group knew we wanted to use vectors for compatibility, but I wasn't sure how to implement it.
            I only used parts of this article to understand this concept better:
            https://www.instaclustr.com/education/vector-database/what-is-vector-similarity-search-pros-cons-and-5-tips-for-success/
            This is a relative simple way to compare similarity within genres between two users.
            Otherwise you get headaches such as determining how to value higher rated genres, or wider read genres.
            */
           /* infix fun DoubleArray.dot(other: DoubleArray): Double {
                var out = 0.0
                for (i in indices) out += this[i] * other[i]
                return out
            }

            val similarity = newUserA dot newUserB*/

           // Log.d("THE SUM", similarity.toString())

        }
    }

}