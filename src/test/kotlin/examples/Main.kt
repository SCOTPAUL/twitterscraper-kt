package examples
import kotlinx.coroutines.*
import uk.co.paulcowie.twitterscraper.Query
import uk.co.paulcowie.twitterscraper.Tweet

fun main(){

    val query = Query()

    var results: List<Tweet>? = null

    query.use {
        runBlocking {
            launch {
                results = query.queryTweets("\"is cancelled\" FROM:daftlimmy", limit = 20000)
            }
        }
    }

    println("Done")

    println(results)
}