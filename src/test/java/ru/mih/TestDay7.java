package ru.mih;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.Test;
import scala.Int;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestDay7 {

    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @Test
    public void testMove1() throws ExecutionException, InterruptedException {
        assertEquals(new Pair<>(2, 37),getMinPositionAndSteps("test1.txt"));
    }

    @Test
    public void testMove11() throws ExecutionException, InterruptedException {
        assertEquals(new Pair<>(5, 168),getMinPositionAndCosts("test1.txt"));
    }

    @Test
    public void testMove2() throws ExecutionException, InterruptedException {
        assertEquals(new Pair<>(3, 7),getMinPositionAndSteps("test2.txt"));
    }

    @Test
    public void testMove3() throws ExecutionException, InterruptedException {
        assertEquals(new Pair<>(6, 10),getMinPositionAndSteps("test3.txt"));
    }

    @Test
    public void testMove4() throws ExecutionException, InterruptedException {
        assertEquals(new Pair<>(349, 355592),getMinPositionAndSteps("test4.txt.txt"));
    }

    @Test
    public void testMove5() throws ExecutionException, InterruptedException {
        assertEquals(new Pair<>(488, 101618069),getMinPositionAndCosts("test4.txt.txt"));
    }
    @Test
    public void testMove6() throws ExecutionException, InterruptedException {
        assertEquals(new Pair<>(488, new BigDecimal(101618069)),getMinPositionAndBigCosts("test4.txt.txt"));
    }

    public Pair<Integer, Integer> getMinPositionAndSteps(String fileName) throws ExecutionException, InterruptedException {

        final Source<Integer, ?> inSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day7/" + fileName))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String()));

        Integer max = inSource.reduce((a,b) -> Math.max(a,b))
                .toMat(Sink.head(), Keep.right())
                .run(testSystem).toCompletableFuture().get();

        return Source.range(0, max).mapAsync(8, ( hole ->
                        inSource.map(i -> new Pair<>(i, Math.abs(hole - i)))
                                .reduce((a,b) -> new Pair<>(hole, a.second()+b.second()))
                                .toMat(Sink.head(), Keep.right())
                                .run(testSystem).toCompletableFuture()
                ))
                .reduce( (a, b) -> (a.second() < b.second())
                        ?new Pair<>(a.first(), a.second())
                        :new Pair<>(b.first(), b.second()) )

                .toMat(Sink.head(), Keep.right()).run(testSystem)
                .toCompletableFuture().get();
    }

    public Pair<Integer, Integer> getMinPositionAndCosts(String fileName) throws ExecutionException, InterruptedException {

        final Source<Integer, ?> inSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day7/" + fileName))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String()));

        Integer max = inSource.reduce((a,b) -> Math.max(a,b))
                .toMat(Sink.head(), Keep.right())
                .run(testSystem).toCompletableFuture().get();

        return Source.range(0, max).mapAsync(8, ( hole ->
                        inSource.map(i -> new Pair<Integer, Integer>(i, getCost(i, hole)))
                                    .reduce((a,b) -> new Pair<>(hole, a.second()+b.second()))
                                    .toMat(Sink.head(), Keep.right())
                                    .run(testSystem).toCompletableFuture()
                ))
                .reduce( (a, b) -> (a.second() < b.second())
                        ?new Pair<>(a.first(), a.second())
                        :new Pair<>(b.first(), b.second()) )

                .toMat(Sink.head(), Keep.right()).run(testSystem)
                .toCompletableFuture().get();
    }

    public Pair<Integer, BigDecimal> getMinPositionAndBigCosts(String fileName) throws ExecutionException, InterruptedException {

        final Source<Integer, ?> inSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day7/" + fileName))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String()));

        Integer max = inSource.reduce((a,b) -> Math.max(a,b))
                .toMat(Sink.head(), Keep.right())
                .run(testSystem).toCompletableFuture().get();

        return Source.range(0, max).mapAsync(8, ( hole ->
                        inSource.map(i -> {
                                    int an = Math.abs(hole - i);
                                    return new Pair<Integer, BigDecimal>(i,
                                            (new BigDecimal(1).add(new BigDecimal(an))).multiply(new BigDecimal(an)).divide(new BigDecimal(2)));
                                })
                                .reduce((a,b) -> new Pair<>(hole, a.second().add(b.second())))
                                .toMat(Sink.head(), Keep.right())
                                .run(testSystem).toCompletableFuture()
                ))
                .reduce( (a, b) -> (a.second().compareTo(b.second()) == -1)
                        ?new Pair<>(a.first(), a.second())
                        :new Pair<>(b.first(), b.second()) )

                .toMat(Sink.head(), Keep.right()).run(testSystem)
                .toCompletableFuture().get();
    }

    @Test
    public void checkFirst(){
        assertSame(66, getCost(16, 5));
        assertSame(10, getCost(1,5));
        assertSame(6, getCost(2,5));
        assertSame(15, getCost(0,5));
        assertSame(1, getCost(4,5));
        assertSame(6, getCost(2,5));
        assertSame(3, getCost(7,5));
        assertSame(10, getCost(1,5));
        assertSame(6, getCost(2,5));
        assertSame( 45, getCost(14,5));

    }

    public int getCost(int i, int hole){
        int an = Math.abs(hole - i);
        return ((1 + an)*an/2);
    }

}
