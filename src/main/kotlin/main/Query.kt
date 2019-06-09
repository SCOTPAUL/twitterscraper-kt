package main

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import java.lang.IllegalStateException
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import kotlin.math.sin

class Query : AutoCloseable {

    private val client = HttpClient()

    private fun makeQueryUrl(query: String, pos: Int? = null): String {
        if(pos != null){
            return "https://twitter.com/i/search/timeline?f=tweets&vertical=" +
            "default&include_available_features=1&include_entities=1&" +
            "reset_error_state=false&src=typd&max_position=$pos&q=$query&l="
        }


        return "https://twitter.com/search?f=tweets&vertical=default&q=$query&l="
    }

    private fun linspace(start: Int, stop: Int, num: Int): List<Int> = (start..stop step (stop - start) / (num - 1)).toList()

    private suspend fun runOneQuery(singleQuery: String, pos: Int?): Pair<List<String>, Int> {
        val url = makeQueryUrl(singleQuery, pos)

        val htmlContent = client.get<String>(url)

        return Pair(listOf(htmlContent), 1)
    }

    private suspend fun queryTweetsOnce(singleQuery: String, limit: Int? = null, pos: Int? = null): List<String> {
        println("Querying $singleQuery")

        var currPos = pos
        val tweets = mutableListOf<String>()

        var numTweets = 0

        while(true){
            val (newTweets, newPos) = runOneQuery(singleQuery, currPos)

            if(newTweets.isNotEmpty()){
                tweets.addAll(tweets)
                println("Added ${newTweets.size} new tweets")
            }
            else {
                return tweets
            }

            currPos = newPos
            numTweets += newTweets.size

            if(limit != null && numTweets > limit){
                return tweets
            }
        }
    }

    suspend fun queryTweets(query: String,
                            limit: Int? = null,
                            startDate: LocalDate = LocalDate.of(2006, 3, 21),
                            endDate: LocalDate = LocalDate.now(),
                            maxPoolSize: Int = 20){



        val numDays = DAYS.between(startDate, endDate).toInt()

        val poolSize = if(maxPoolSize > numDays) numDays else maxPoolSize

        val dateRanges = linspace(0, numDays, poolSize + 1).map { startDate.plusDays(it.toLong()) }

        val limitPerPool = (limit?.div(poolSize))?.plus(1)

        val queries = (dateRanges.dropLast(1) zip dateRanges.drop(1)).map { (since, until) -> "$query since: $since until: $until" }

        println(queries)

        val results = queries.map { q -> queryTweetsOnce(q, limit) }

    }

    override fun close() {
        client.close()
    }
}