package main

import org.jsoup.Jsoup
import java.time.Instant

/**
 * @property user the username of the user who posted the tweet (i.e. the @ name)
 * @property timestamp the Instant that the tweet was posted
 * @property text the textual content of the tweet
 * @property id the unique identifier of the tweet
 */
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