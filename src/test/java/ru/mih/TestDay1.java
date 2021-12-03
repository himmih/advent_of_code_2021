package ru.mih;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static ru.mih.Day1.numDepthIncreases;

public class TestDay1 {
    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @AfterClass
    public static void terminate(){
       testSystem.terminate();
    }


    @Test
    public void testNumDepthIncreases() throws ExecutionException, InterruptedException, TimeoutException {

        final CompletionStage<Integer> future =
                Source.from(Arrays.asList(199, 200, 208, 210, 200, 207, 240, 269, 260, 263))
                        .runWith(numDepthIncreases(), testSystem);

        final Integer result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(7, result.intValue());

        assertEquals(1, Source.from(Arrays.asList(199, 200))
                        .runWith(numDepthIncreases(), testSystem)
                        .toCompletableFuture().get(3, TimeUnit.SECONDS).intValue());

        assertEquals(0, Source.from(Arrays.asList(199))
                .runWith(numDepthIncreases(), testSystem)
                .toCompletableFuture().get(3, TimeUnit.SECONDS).intValue());

        assertEquals(6, Source.from(Arrays.asList(199, 200, 208, 210, 200, 207, 240, 269, 260))
                .runWith(numDepthIncreases(), testSystem)
                .toCompletableFuture().get(3, TimeUnit.SECONDS).intValue());
    }

}
