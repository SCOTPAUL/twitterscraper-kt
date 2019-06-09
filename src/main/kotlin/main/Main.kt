package main
import kotlinx.coroutines.*


fun main(){
    runBlocking {
        val query = Query()

        launch { query.queryTweets("\"is cancelled\" FROM:daftlimmy", limit = 20) }
    }

    println("Done")

}