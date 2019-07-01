uk.co.paulcowie.twitterscraper-kt
=================

A port of [uk.co.paulcowie.twitterscraper](https://github.com/taspinar/uk.co.paulcowie.twitterscraper) to the JVM.

Only provides a subset of the functionality of the original uk.co.paulcowie.twitterscraper, to allow text from a tweet search to be returned.

## Example Usage

### Kotlin

```kotlin
fun uk.co.paulcowie.twitterscraper(){
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
```

### Java

```java
public class Main {
    public static void uk.co.paulcowie.twitterscraper(String[] args) {
        try(Query query = new Query()){
            CompletableFuture<List<Tweet>> future = query.queryTweetsAsync("\"is cancelled\" FROM:daftlimmy",
                    20000,
                    LocalDate.of(2016, 1, 1),
                    LocalDate.now(),
                    20);

            future.thenAccept(tweets -> {
                System.out.println("Done");
                System.out.println(tweets);
            });

            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
```

