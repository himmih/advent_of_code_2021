package ru.mih;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.javadsl.*;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import org.junit.Test;
import ru.mih.day6.School;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.*;

public class TestDay6 {


    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

//    @AfterClass
//    public static void terminate(){
//        testSystem.terminate();
//    }

    @Test
    public void testCreation() throws InterruptedException, ExecutionException, TimeoutException {

        final Source<School.Fish, ?> headerSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day6/test1.txt"))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> new School.Fish(Integer.parseInt(bs.utf8String()))) ;

        Flow<School.Fish, School.Fish, NotUsed>
                nextDayFlow = Flow.of(School.Fish.class)
                .mapConcat(f -> f.nextDay());

        Source<School.Fish, ?> nextDay = headerSource;
        for(int i = 0; i < 80; i++){
            nextDay = nextDay.async().via(nextDayFlow);
        }

        nextDay.fold(0, (acc, element) -> acc+1)
                .toMat(Sink.foreach(System.out::print), Keep.right()).run(testSystem)
                .toCompletableFuture().get();

    }


    @Test
    public void testPartTwo() throws InterruptedException, ExecutionException, TimeoutException {

        final Source<Integer, ?> headerSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day6/test1.txt"))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String())) ;

        Flow<Integer, Integer, NotUsed>
                nextDayFlow = Flow.of(Integer.class)
                .mapConcat(i -> {
                    ArrayList out = new ArrayList<>(2);
                    if (i == 0){
                        out.add(6);
                        out.add(8);
                    }else {
                        out.add(i-1);
                    }
                    return out;
                });

        Source<Integer, ?> nextDay = headerSource;
        for(int i = 0; i < 256; i++){
            nextDay = nextDay.via(nextDayFlow).async();
        }

        nextDay.fold(0, (acc, element) -> acc+1)
                .toMat(Sink.foreach(System.out::print), Keep.right()).run(testSystem)
                .toCompletableFuture().get();

    }

    //Pair(number, count)
    public Flow<Integer, Pair<Integer, Integer>, NotUsed> foundUniqueGroups() {

                return Flow.of(Integer.class)
                        .groupBy(5, x -> x)
                                .fold(new Pair<>(0, 0), (acc, next) -> new Pair<>(next, acc.second()+1))
                                        .mergeSubstreams();

    }

    public Flow<Integer, Integer, NotUsed> getChildrenNumber(int days){
        Flow<Integer, Integer, NotUsed>
                nextDayFlow = Flow.of(Integer.class)
                .mapConcat(i -> {
                    ArrayList out = new ArrayList<>(2);
                    if (i == 0){
                        out.add(6);
                        out.add(8);
                    }else {
                        out.add(i-1);
                    }
                    return out;
                });

        Flow<Integer, Integer, NotUsed> nextDay = Flow.of(Integer.class);
        for(int i = 0; i < days; i++){
            nextDay = nextDay.via(nextDayFlow);
        }

        return nextDay.fold(0, (acc, element) -> acc+1);

    }

    public Flow<Integer, Pair<Integer, Integer>, NotUsed> getChildrenGroups(int days){
        Flow<Integer, Integer, NotUsed>
                nextDayFlow = Flow.of(Integer.class)
                .mapConcat(i -> {
                    ArrayList<Integer> out = new ArrayList<Integer>(2);
                    if (i == 0){
                        out.add(6);
                        out.add(8);
                    }else {
                        out.add(i-1);
                    }
                    return out;
                });

        Flow<Integer, Integer, NotUsed> nextDay = Flow.of(Integer.class);
        for(int i = 0; i < days; i++){
            nextDay = nextDay.via(nextDayFlow);
        }

        return nextDay
                .groupBy(10, x -> x)
                .fold(new Pair<>(0, 0), (acc, next) -> new Pair<>(next, acc.second()+1))
                .mergeSubstreams();
    }


    @Test
    public void getTotal() throws ExecutionException, InterruptedException {

        final Source<Integer, ?> inSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day6/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String())) ;

        inSource.via(getChildrenGroups(100))

                .mapAsync(8, (pair ->
                   Source.single(pair.first()).
                           via(getChildrenNumber(156))
                           .map(x -> new BigDecimal(x).multiply(new BigDecimal(pair.second())))
                           .toMat(Sink.head(), Keep.right())
                           .run(testSystem).toCompletableFuture()
                )).reduce((acc, b) -> acc.add(b))
                .toMat(Sink.foreach(x -> System.out.println(x)), Keep.right())
                .run(testSystem)
                .toCompletableFuture().get();

    }



    @Test
    public void testOneNumber() throws InterruptedException, ExecutionException, TimeoutException {

        Source<Integer, ?> oneNumberSource = Source.single(1);

        Flow<Integer, Integer, NotUsed>
                nextDayFlow = Flow.of(Integer.class)
                .mapConcat(i -> {
                    ArrayList out = new ArrayList<>(2);
                    if (i == 0){
                        out.add(6);
                        out.add(8);
                    }else {
                        out.add(i-1);
                    }
                    return out;
                });

        Source<Integer, ?> nextDay = oneNumberSource;
        for(int i = 0; i < 256; i++){
            nextDay = nextDay.via(nextDayFlow).async();
        }

        nextDay.fold(0, (acc, element) -> acc+1)
                .toMat(Sink.foreach(System.out::print), Keep.right()).run(testSystem)
                .toCompletableFuture().get();

    }
}
