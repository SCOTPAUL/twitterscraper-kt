package main

import com.beust.klaxon.Klaxon
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.*
import java.net.URLEncoder
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

class Query : AutoCloseable {

    private data class TwitterJsonResponse(val items_html: String, val min_position: String?)

    private val client = HttpClient()

    private fun makeQueryUrl(query: String, pos: String? = null): String {
        if(pos != null){
            return "https://twitter.com/i/search/timeline?f=tweets&vertical=" +
            "default&include_available_features=1&include_entities=1&" +
            "reset_error_state=false&src=typd&max_position=${URLEncoder.encode(pos, "UTF-8")}&q=${URLEncoder.encode(query, "UTF-8")}&l="
        }


        return "https://twitter.com/search?f=tweets&vertical=default&q=${URLEncoder.encode(query, "UTF-8")}&l="
    }

    private fun linspace(start: Int, stop: Int, num: Int): List<Int> = (start..stop step (stop - start) / (num - 1)).toList()

    private suspend fun runOneQuery(singleQuery: String, pos: String?): Pair<List<Tweet>, String?> {
        val url = makeQueryUrl(singleQuery, pos)


        println(url)


        val htmlContent = client.get<String>(url) {
            header("User-Agent", USER_AGENTS)
        }

        val response: TwitterJsonResponse? = if (pos != null) {
            Klaxon().parse(htmlContent)
        } else {
            TwitterJsonResponse(htmlContent, null)
        }

        val tweets = Tweet.fromHTML(response!!.items_html)

        if(tweets.isEmpty()){
            return Pair(listOf(), pos)
        }

        if(response.min_position == null){
            return Pair(Tweet.fromHTML(response.items_html), "TWEET-${tweets.last().id}-${tweets.first().id}")
        }

        return Pair(tweets, response.min_position)
    }

    private suspend fun queryTweetsOnce(singleQuery: String, limit: Int? = null, pos: String? = null): List<Tweet> {
        println("Querying $singleQuery")

        var currPos = pos
        val tweets = mutableListOf<Tweet>()

        var numTweets: Int

        while(true){
            val (newTweets, newPos) = runOneQuery(singleQuery, currPos)

            if(newTweets.isNotEmpty()){
                tweets.addAll(newTweets)
                println("Added ${newTweets.size} new tweets")
            }
            else {
                return tweets
            }

            currPos = newPos
            numTweets = tweets.size

            if(limit != null && numTweets > limit){
                return tweets
            }
        }
    }

    /**
     * @param query the search query to run on twitter, as compiled with the Twitter Advanced Search
     * @param limit the maximum number of tweets to attempt to retrieve, though more or fewer can be returned depending on dates and pool size (defaults to unlimited)
     * @param startDate the date to start searching for tweets (defaults to Twitter's creation date)
     * @param endDate the date to end searching for tweets (defaults to the current date)
     * @param maxPoolSize the maximum number of separate searches to carry out for tweets (defaults to 20 but can be fewer depending on date selection)
     * @return A list of Tweet objects matching the search criteria
     */
    suspend fun queryTweets(query: String,
                            limit: Int? = null,
                            startDate: LocalDate = LocalDate.of(2006, 3, 21),
                            endDate: LocalDate = LocalDate.now(),
                            maxPoolSize: Int = 20): List<Tweet> {



        val numDays = DAYS.between(startDate, endDate).toInt()

        val poolSize = if(maxPoolSize > numDays) numDays else maxPoolSize

        val dateRanges = linspace(0, numDays, poolSize + 1).map { startDate.plusDays(it.toLong()) }

        val limitPerPool = (limit?.div(poolSize))?.plus(1)

        println("Limit per pool $limitPerPool")

        val queries = (dateRanges.dropLast(1) zip dateRanges.drop(1)).map { (since, until) -> "$query since:$since until:$until" }

        println(queries)

        var results: List<Tweet> = emptyList()
        coroutineScope {
            val jobs = queries.map { q ->  async {
                    val tweets = queryTweetsOnce(q, limitPerPool)
                    println("Result for query: $q is $tweets")
                    tweets
                }
            }
            results = jobs.awaitAll().flatten()
        }

        println("Found ${results.size} results")

        return results
    }

    override fun close() {
        client.close()
    }
}