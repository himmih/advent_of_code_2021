package ru.mih;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class TestDay13 {

    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");


    private Flow<Pair<Integer, Integer>, Pair<Integer, Integer>, NotUsed> foldY(int yy) {
        return Flow.<Pair<Integer, Integer>>create().map(pair -> {
            Integer y =  (pair.second() > yy)?Integer.valueOf(2*yy-pair.second()):Integer.valueOf(pair.second());
            return new Pair<>(Integer.valueOf(pair.first()), y);
        });
    }

    private Flow<Pair<Integer, Integer>, Pair<Integer, Integer>, NotUsed> foldX(int xx) {
        return Flow.<Pair<Integer, Integer>>create().map(pair -> {
            Integer x =  (pair.first() > xx)?Integer.valueOf(2*xx-pair.first()):Integer.valueOf(pair.first());
            return new Pair<>(x, Integer.valueOf(pair.second()));
        });
    }

    @Test
    public void loadPaths1() throws ExecutionException, InterruptedException {
        Flow<Pair<Integer, Integer>, Pair<Integer, Integer>, NotUsed> flow = foldY(7);
        assertEquals(17, count("test1",  flow).size());
    }

    @Test
    public void loadPaths2() throws ExecutionException, InterruptedException {
        Flow<Pair<Integer, Integer>, Pair<Integer, Integer>, NotUsed> flow = foldX(655);
        assertEquals(737, count("test2",  flow).size());
    }

    @Test
    public void loadPaths3() throws ExecutionException, InterruptedException {
        Flow<Pair<Integer, Integer>, Pair<Integer, Integer>, NotUsed> flow
                = foldX(655)
                .via(foldY(447))
                .via(foldX(327))
                .via(foldY(223))
                .via(foldX(163))
                .via(foldY(111))
                .via(foldX(81))
                .via(foldY(55))
                .via(foldX(40))
                .via(foldY(27))
                .via(foldY(13))
                .via(foldY(6));

        print("test2", flow);

        assertEquals(737, count("test2",  flow).size());
    }


    private void print(String fileName, Flow<Pair<Integer, Integer>, Pair<Integer, Integer>, NotUsed> flow) throws ExecutionException, InterruptedException {
        Source<Pair<Integer, Integer>, ?> readXY = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day13/" + fileName + ".txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .map( line -> {
                    String[] xy = line.split(",");
                    int x = Integer.parseInt(xy[0]);
                    int y = Integer.parseInt(xy[1]);
                    return new Pair<>(x, y);
                });

        List<Pair<Integer, Integer>> pairs = readXY.via(flow)
                .toMat(Sink.seq() , Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

        Set<Pair<Integer, Integer>> set = new HashSet<>(pairs);

        Pair<Integer, Integer> maxPair =
        set.stream().reduce( new Pair<>(0, 0),
                (acc, a) -> new Pair(
                        (a.first() > acc.first()?a.first():acc.first()),
                        (a.second() > acc.second()?a.second():acc.second()))
        );

        System.out.println(maxPair);
        System.out.println();

        List<List<Byte>> matrix = new ArrayList<>();
        for (int i = 0; i < maxPair.first()+1; i++) {
            matrix.add(new ArrayList<>());
                for (int j = 0; j < maxPair.second()+1; j++){
                        matrix.get(i).add(null);
                }
        }
        for (Pair<Integer, Integer> pair: set) {
           matrix.get(pair.first()).set(pair.second(), (byte)8);
        }

        printMatrix(matrix);

    }

    private Set count(String fileName, Flow<Pair<Integer, Integer>, Pair<Integer, Integer>, NotUsed> flow) throws ExecutionException, InterruptedException {
        Source<Pair<Integer, Integer>, ?> readXY = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day13/" + fileName + ".txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .map( line -> {
                    String[] xy = line.split(",");
                    int x = Integer.parseInt(xy[0]);
                    int y = Integer.parseInt(xy[1]);
                    return new Pair<>(x, y);
                });

                List<Pair<Integer, Integer>> out = readXY.via(flow)

                .toMat(Sink.seq(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

        return new HashSet<>(out);

    }

    private List<List<Byte>> getMatrix(String fileName) throws ExecutionException, InterruptedException {
//        final Source<HashMap<String, List<String>>, CompletionStage<IOResult>> inSource =
        List<List<Byte>> matrix = new ArrayList<>();
        FileIO.fromPath(
                    Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day13/" + fileName + ".txt"))
            .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
            .filterNot(ByteString::isEmpty)
            .map(bs -> bs.utf8String())
//                .toMat(Sink.foreach(line -> {
//                    String[] xy = line.split(",");
//                    int x = Integer.parseInt(xy[0]);
//                    int y = Integer.parseInt(xy[1]);
//                    matrix.get().set(Integer.parseInt(xy[1]), (byte)0);
//                }), Keep.right())
//                .toMat(Sink.foreach(System.out::println), Keep.right())
            .run(testSystem)
            .toCompletableFuture()
            .get();

        return matrix;

    }

    private void printMatrix(List<List<Byte>> matrix) {
        for(int r = 0; r < matrix.size(); r++) {
            for(int c = 0; c < matrix.get(r).size(); c++) {
                if (matrix.get(r).get(c) == null){
                    System.out.print(" ");
                }else
                System.out.print("0");
            }
            System.out.println();
        }
    }

}
