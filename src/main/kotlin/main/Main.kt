package main
import kotlinx.coroutines.*


fun main(){
    runBlocking {
        val query = Query()

        launch { query.queryTweets("is cancelled", limit = 5) }
        println("Hello")
        println("World")
    }

}