package ru.mih;

import akka.actor.ActorSystem;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import org.junit.AfterClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static ru.mih.Day2.foundPosition;
import static ru.mih.Day2.foundPositionWithAim;

public class TestDay2 {
    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @AfterClass
    public static void terminate(){
       testSystem.terminate();
    }


    @Test
    public void testForwardPositions() throws ExecutionException, InterruptedException, TimeoutException {

        final CompletionStage<Day2.Position> future =
                Source.from(Arrays.asList("forward 22", "forward 77")
                                .stream().map( x -> ByteString.fromString(x)).collect(Collectors.toList()))
                        .runWith(foundPosition(), testSystem);

        final Day2.Position result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(99, result.horizontal);
        assertEquals(0, result.depth);

        final CompletionStage<Day2.Position> future2 =
                Source.from(Arrays.asList("up 22", "down 22")
                                .stream().map( x -> ByteString.fromString(x)).collect(Collectors.toList()))
                        .runWith(foundPosition(), testSystem);

        final Day2.Position result2 = future2.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(0, result2.horizontal);
        assertEquals(0, result2.depth);
    }

    @Test
    public void testDepthPositions() throws ExecutionException, InterruptedException, TimeoutException {

        final CompletionStage<Day2.Position> future2 =
                Source.from(Arrays.asList("up 22", "down 22")
                                .stream().map( x -> ByteString.fromString(x)).collect(Collectors.toList()))
                        .runWith(foundPosition(), testSystem);

        final Day2.Position result2 = future2.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(0, result2.horizontal);
        assertEquals(0, result2.depth);
    }
    @Test
    public void testMix() throws ExecutionException, InterruptedException, TimeoutException {

        final CompletionStage<Day2.Position> future2 =
                Source.from(Arrays.asList("up 22", "forward 22", "down 77")
                                .stream().map( x -> ByteString.fromString(x)).collect(Collectors.toList()))
                        .runWith(foundPosition(), testSystem);

        final Day2.Position result2 = future2.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(22, result2.horizontal);
        assertEquals(55, result2.depth);
    }

    @Test
    public void testNoPositions() throws ExecutionException, InterruptedException, TimeoutException {

        final CompletionStage<Day2.Position> future2 =
                Source.from(Arrays.asList("")
                                .stream().map( x -> ByteString.fromString(x)).collect(Collectors.toList()))
                        .runWith(foundPosition(), testSystem);

        final Day2.Position result2 = future2.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(0, result2.horizontal);
        assertEquals(0, result2.depth);
    }

    @Test
    public void testPartTow() throws ExecutionException, InterruptedException, TimeoutException {
        final CompletionStage<Day2.PositionWithAim> future
            = FileIO.fromPath(
                    Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day2/test1.txt"))
            .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                        .runWith(foundPositionWithAim(), testSystem);


        final Day2.Position result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(15, result.horizontal);
        assertEquals(60, result.depth);
    }

    @Test
    public void testPartTowResult() throws ExecutionException, InterruptedException, TimeoutException {
        final CompletionStage<Day2.PositionWithAim> future
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day2/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .runWith(foundPositionWithAim(), testSystem);


        final Day2.Position result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals("h: 1923, d: 1030939, result: 1982495697", result.toString());
    }

    @Test
    public void testMixPartTwo() throws ExecutionException, InterruptedException, TimeoutException {

        final CompletionStage<Day2.PositionWithAim> future2 =
                Source.from(Arrays.asList("up 22", "forward 22", "down 77")
                                .stream().map( x -> ByteString.fromString(x)).collect(Collectors.toList()))
                        .runWith(foundPositionWithAim(), testSystem);

        final Day2.Position result2 = future2.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals(22, result2.horizontal);
        assertEquals(-22*22, result2.depth);
    }
}
