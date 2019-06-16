package examples;

import main.Query;
import main.Tweet;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
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
