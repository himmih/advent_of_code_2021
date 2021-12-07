package ru.mih;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.AfterClass;
import org.junit.Test;
import ru.mih.day5.Vents;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestDay5 {

    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @AfterClass
    public static void terminate(){
        testSystem.terminate();
    }

    @Test
    public void testDiagonal(){

        Vents.Vent v1 = new Vents.Vent(3, 9, 9, 3);

        assertNotSame(0, v1.points.size());

        Vents.Vent v2 = new Vents.Vent(1, 1, 3, 3);

        assertEquals(Arrays.stream(new Vents.Point[]{new Vents.Point(1, 1), new Vents.Point(2, 2), new Vents.Point(3, 3)})
                        .collect(Collectors.toSet()),
                v2.points);

        Vents.Vent v3 = new Vents.Vent(3, 3, 1, 1);
        assertEquals(Arrays.stream(new Vents.Point[]{new Vents.Point(3, 3), new Vents.Point(2, 2), new Vents.Point(1, 1)})
                        .collect(Collectors.toSet()),
                v3.points);

        Vents.Vent v4 = new Vents.Vent(9, 7, 7, 9);

        assertEquals(Arrays.stream(new Vents.Point[]{new Vents.Point(9, 7), new Vents.Point(8, 8), new Vents.Point(7, 9)})
                        .collect(Collectors.toSet()),
                v4.points);
    }

    @Test
    public void testCreation(){

        Vents.Vent v1 = new Vents.Vent(0, 9, 3, 9);
        Vents.Vent v2 = new Vents.Vent(3, 9, 0, 9);

        assertNotSame(0, v1.points.size());
        assertNotSame(0, v2.points.size());
        assertEquals(v1, v2);

//        Vents.Vent v1 = new Vents.Vent(0, 9, 3, 9);
//        Vents.Vent v2 = new Vents.Vent(2, 9, 7, 9);
    }

    @Test
    public void testIntersection(){
        Vents.Vent v1 = new Vents.Vent(0, 9, 3, 9);
        Vents.Vent v2 = new Vents.Vent(2, 9, 7, 9);

        assertEquals(Arrays.stream(new Vents.Point[]{new Vents.Point(2, 9), new Vents.Point(3, 9)})
                        .collect(Collectors.toSet()),
                v1.intersects(v2));

    }

    public static class ResultMap{
        final HashMap<Vents.Point, Integer> points = new HashMap<>();

        public void add(Vents.Point point) {
            if (points.containsKey(point)) {
                points.put(point, points.get(point) + 1);
            } else {
                points.put(point, 1);
            }
        }
    }

    public static class Result{
        final List<Pair<Integer, Vents.Point>> points;

        public Result(List<Pair<Integer, Vents.Point>> points) {
            this.points = points;
        }

        public Result add(Vents.Point point) {

            Optional<Pair<Integer, Vents.Point>> foundPoint
                    = points.stream().filter(pair -> pair.second().equals(point)).findAny();

            List<Pair<Integer, Vents.Point>> newPoints =
                    points.stream()
                            .filter(pair -> !pair.second().equals(point))
                            .map(p ->
                                (Pair<Integer, Vents.Point>) new Pair(p.first(), new Vents.Point(p.second().x, p.second().y))
                            )
                            .collect(Collectors.toList());
            if (foundPoint.isPresent()) {
                newPoints.add(new Pair<>(foundPoint.get().first()+1, new Vents.Point(point.x, point.y)));
            }else {
                newPoints.add(new Pair<>(1, new Vents.Point(point.x, point.y)));
            }

            Result out = new Result(newPoints);
//            System.out.println(out);
            return out;
        }

        @Override
        public String toString() {
            return "Result(" + points.size();
        }
    }

    @Test
    public void runExample() throws ExecutionException, InterruptedException {
        final Source<String, CompletionStage<IOResult>> linesSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day5/test1.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .mapConcat(x -> Arrays.asList(x.split(" -> ")));


        Flow<String, Vents.Vent, NotUsed> sinkVents =
                Flow.of(String.class)
                        .grouped(2)
                        .map(ab -> new Vents.Vent(ab.get(0).split(","), ab.get(1).split(",")))
                                .filter(Vents.Vent::isHorizontalOrVertical);


        final List<Vents.Vent> vents =
                linesSource.
                via(sinkVents)
                .toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();


        Source.from(vents)
                .mapConcat(vent -> vent.intersects(vents))
                .fold(new Result(new ArrayList<>()), (acc, element) -> acc.add(element))
                .mapConcat(x -> x.points)
                .filter(x -> x.first() > 1)
                .fold(0, (acc, p2) -> acc+1)

                .toMat(Sink.foreach(System.out::println), Keep.right()).run(testSystem);


    }

    @Test
    public void runPartOne() throws ExecutionException, InterruptedException {
        final Source<String, CompletionStage<IOResult>> linesSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day5/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .mapConcat(x -> Arrays.asList(x.split(" -> ")));


        Flow<String, Vents.Vent, NotUsed> sinkVents =
                Flow.of(String.class)
                        .grouped(2)
                        .map(ab -> new Vents.Vent(ab.get(0).split(","), ab.get(1).split(",")))
                        .filter(Vents.Vent::isHorizontalOrVertical);

        final List<Vents.Vent> vents =
                linesSource.
                        via(sinkVents)
                        .toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();

                List<Vents.Point> intersects = Source.from(vents)
                .mapConcat(vent -> vent.intersects(vents))
                        .toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();

                ResultMap resultMap = new ResultMap();
                for(Vents.Point point: intersects){
                    resultMap.add(point);
                }

                System.out.println(resultMap.points.entrySet().stream()
                        .filter(entry -> entry.getValue() > 1)
                        .count());


    }
}
