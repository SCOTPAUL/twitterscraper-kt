package main

import org.jsoup.Jsoup
import java.time.Instant

data class Tweet(val user: String,
                val timestamp: Instant,
                val text: String,
                val id: String) {

    companion object {
        fun fromHTML(html: String): List<Tweet> {
            val tweetDoc = Jsoup.parse(html)

            return tweetDoc.run {
                select("li.js-stream-item").map { element ->
                    val user = element.select("span.username").text()
                    val timestamp = Instant.ofEpochMilli(element.select("span._timestamp").attr("data-time-ms").toLong())
                    val text = element.select("p.tweet-text").text()
                    val id = element.attr("data-item-id")

                    Tweet(user, timestamp, text, id)
                }


            }

        }
    }
}