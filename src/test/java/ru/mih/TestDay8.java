package ru.mih;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.Test;
import ru.mih.day8.Clock;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TestDay8 {
    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @Test
    public void testFirstOne() throws ExecutionException, InterruptedException {

        final Source<Pair<List<String>, List<String>>, CompletionStage<IOResult>> inSignalsSegments =
                FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day8/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .mapConcat(x -> Arrays.asList(x.split("\\|")))
                .grouped(2)
                .map(m -> new Pair<>(
                        Arrays.asList(m.get(0).trim().replaceAll("[^ abcdefg]", "").split(" ")),
                        Arrays.asList(m.get(1).trim().replaceAll("[^ abcdefg]", "").split(" "))));

        inSignalsSegments
                        .mapConcat(pair -> pair.second())
                        .filter(x -> x.length() == 2 || x.length() == 4 || x.length() == 3 || x.length() == 7)
                        .fold(0, (acc, x) -> acc+1)
                        .toMat(Sink.foreach(System.out::println), Keep.right())
                .run(testSystem).toCompletableFuture().get();


    }

    @Test
    public void testSecond() throws ExecutionException, InterruptedException {

        final Source<Pair<List<String>, List<String>>, CompletionStage<IOResult>> inSignalsSegments =
                FileIO.fromPath(
                                Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day8/test2.txt"))
                        .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                        .filterNot(ByteString::isEmpty)
                        .map(bs -> bs.utf8String())
                        .mapConcat(x -> Arrays.asList(x.split("\\|")))
                        .grouped(2)
                        .map(m -> new Pair<>(
                                Arrays.stream(m.get(0).trim().replaceAll("[^ abcdefg]", "").split(" ")).collect(Collectors.toList()),
                                Arrays.stream(m.get(1).trim().replaceAll("[^ abcdefg]", "").split(" ")).collect(Collectors.toList())));


        inSignalsSegments
                .mapConcat(pair -> pair.second())
                .filter(x -> x.length() == 2 || x.length() == 4 || x.length() == 3 || x.length() == 7)
                .fold(0, (acc, x) -> acc+1)
                .toMat(Sink.foreach(System.out::println), Keep.right())
                .run(testSystem).toCompletableFuture().get();


    }

    @Test
    public void testMinus(){

        Clock.Signal s1 = new Clock.Signal("abcd");
        Clock.Signal s2 = new Clock.Signal("abcdefg");

        assertEquals(new Clock.Signal("bcd"), s1.minus(new Clock.Signal("a")));
        assertEquals(new Clock.Signal("feg"), s2.minus(s1));
        assertEquals(new Clock.Signal("adbc"), s1.minus(new Clock.Signal("")));

    }

    @Test
    public void testIntersection(){

        Clock.Signal s1 = new Clock.Signal("abcd");
        Clock.Signal s2 = new Clock.Signal("abcdefg");

        assertEquals(new Clock.Signal("abdc"), s2.intersection(s1));
        assertEquals(s1.intersection(s2), s2.intersection(s1));

        assertEquals(new Clock.Signal("abdc"), s1.intersection(s1));
    }

    @Test
    public void testUnion() {
        assertEquals(new Clock.Signal("abdc"), new Clock.Signal("").union(new Clock.Signal("abcd")));
        assertEquals(new Clock.Signal("fabdc"), new Clock.Signal("abcdf").union(new Clock.Signal("abcd")));
    }


}
