package ru.mih;

import akka.NotUsed;
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
                .map(pair -> solve(pair.first(), pair.second()))
                .fold(0, (acc, x) -> acc+x)
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

    @Test
    public void testOneLine() throws ExecutionException, InterruptedException {

        final Source<Object, NotUsed> inSignalsSegments =
                Source.single("acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab |cdfeb fcadb cdfeb cdbaf")
                        .mapConcat(x -> Arrays.asList(x.split("\\|")))
                        .grouped(2)
                        .map(m -> new Pair<List<String >, List<String>>(
                                Arrays.stream(m.get(0).trim().replaceAll("[^ abcdefg]", "").split(" ")).collect(Collectors.toList()),
                                Arrays.stream(m.get(1).trim().replaceAll("[^ abcdefg]", "").split(" ")).collect(Collectors.toList())));

        inSignalsSegments
                .map(x -> (Pair<List<String>, List<String>>)x)
                .map(pair -> solve(pair.first(), pair.second()))
//                .fold(0, (acc, x) -> acc+x)
                .toMat(Sink.foreach(System.out::println), Keep.right())
                .run(testSystem).toCompletableFuture().get();


    }

    public int solve(List<String> inSignals, List<String> outSegments ){
        Clock.Signal[] signals = new Clock.Signal[10];

        Clock.Signal intersection2_3_5 = null;
        Clock.Signal intersection0_6_9 = null;

        for(String signal: inSignals){
            if (signal.length() == 6) {
                if (intersection0_6_9 == null) {
                    intersection0_6_9 = new Clock.Signal(signal);
                }else {
                    intersection0_6_9 = intersection0_6_9.intersection(new Clock.Signal(signal));
                }
            }else if (signal.length() == 5) {
                if (intersection2_3_5 == null) {
                    intersection2_3_5 = new Clock.Signal(signal);
                }else {
                    intersection2_3_5 = intersection2_3_5.intersection(new Clock.Signal(signal));
                }
            }else if (signal.length() == 2) {
                signals[1] = new Clock.Signal(signal);
            }else if (signal.length() == 4) {
                signals[4] = new Clock.Signal(signal);
            }else if (signal.length() == 3) {
                signals[7] = new Clock.Signal(signal);
            }else if (signal.length() == 7) {
                signals[8] = new Clock.Signal(signal);
            }
        }

        Clock.Signal s7 = intersection2_3_5.intersection(signals[4]);
//        System.out.println("s7 == " + s7);
        Clock.Signal s6 = intersection2_3_5.intersection(signals[7]);
//        System.out.println("s6 == " + s6);
        Clock.Signal s2 = intersection0_6_9.intersection(signals[1]);
//        System.out.println("s2 == " + s2);
        Clock.Signal s1 = signals[1].minus(s2);
//        System.out.println("s1 == " + s1);
        Clock.Signal s5 = signals[4].minus(s1).minus(s2).minus(s7);
//        System.out.println("s5 == " + s5);
        Clock.Signal s3 = intersection0_6_9.minus(s6).minus(s5).minus(s2);
//        System.out.println("s3 == " + s3);
        Clock.Signal s4 = signals[8].minus(s1).minus(s2).minus(s3).minus(s5).minus(s6).minus(s7);
//        System.out.println("s4 == " + s4);

        signals[0] = s1.union(s2).union(s3).union(s4).union(s5).union(s6);
        signals[2] = s6.union(s1).union(s7).union(s4).union(s3);
        signals[3] = s6.union(s1).union(s7).union(s2).union(s3);
        signals[5] = s6.union(s5).union(s7).union(s2).union(s3);
        signals[6] = s6.union(s5).union(s7).union(s2).union(s3).union(s4);
        signals[9] = s3.union(s2).union(s7).union(s5).union(s6).union(s1);

//        for(int i = 0; i< 10; i++){
//                System.out.println(signals[i].words.stream().map(c -> String.valueOf(c)).collect(Collectors.joining()) + " - " + i);
//        }

//            String wrongSegment: outSegments
        int out = 0;
        for(int j = 0; j < 4; j++){
            Clock.Signal wSignal = new Clock.Signal(outSegments.get(j));
            for(int i = 0; i< 10; i++){
                if (wSignal.equals(signals[i])){
//                    System.out.print(i);
                    out += i*Math.pow(10, 3-j);
                }
            }
        }

        return out;

    }


}
