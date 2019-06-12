package examples
import kotlinx.coroutines.*
import main.Query
import main.Tweet


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