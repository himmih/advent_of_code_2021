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
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TestDay14 {


    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");


    @Test
    public void testEx() throws ExecutionException, InterruptedException {
       assertEquals(2188189693529L, countDiff("NNCB", "test1"));
    }

    @Test
    public void testOne() throws ExecutionException, InterruptedException {
        assertEquals(3831, countDiff("CKKOHNSBPCPCHVNKHFFK", "test2"));
    }

    public long countDiff(String in, String fileChanges) throws ExecutionException, InterruptedException {
        Map<Pair<String, String>, String> changes = getPaths(fileChanges);

        Source<String, NotUsed> start =
                Source.from(in.chars()
                        .mapToObj(x -> String.valueOf((char)x)).collect(Collectors.toList()));

        Flow<String, Pair<String, String>, NotUsed> startFlow =
                Flow.of(String.class)
                        .sliding(2, 1)
                        .map(list -> new Pair<String, String>(list.get(0), list.get(1)));

        Flow<Pair<String, String>, Pair<String, String>, NotUsed> changeFlow =
                Flow.<Pair<String, String>>create()
                        .mapConcat(pair -> {
                            ArrayList<Pair<String, String>> out = new ArrayList<>();
                            if (changes.containsKey(pair)){
                                String changeTo = changes.get(pair);
                                out.add(new Pair<String, String>(pair.first(), changeTo));
                                out.add(new Pair<String, String>(changeTo, pair.second()));
                                return out;
                            }else {
                                out.add(pair);
                                return out;
                            }
                        });

        Flow<Pair<String,String>, Pair<String, String>, NotUsed> workFlow = changeFlow;

        for (int i = 0; i < 40; i++) { //< step - 1
            workFlow = workFlow.via(changeFlow);
        }

        final String lastOne = start.via(startFlow).via(workFlow)
                .map(pair -> pair.second())
                .toMat(Sink.last(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

       return start.via(startFlow).via(workFlow)
                .map(pair -> pair.first())
//                .fold(new StringBuffer(), (acc, e) -> acc.append(e))
//                .map(a -> a.length()+1)
                .fold(new HashMap<String, Long>(), (groups, ch) -> {
                    if (groups.containsKey(ch)){
                        groups.put(ch, groups.get(ch)+1);
                    }else {
                        if (ch.equals(lastOne))
                            groups.put(ch, 2L) ;
                        else
                            groups.put(ch, 1L) ;
                    }
                    return new HashMap<>(groups);
                })
                .mapConcat(map -> map.values())
                .fold(new Pair<>(0L, Long.MAX_VALUE), (acc, i) -> {
                   long max = (i > acc.first())?i:acc.first();
                   long min = (i < acc.second())?i:acc.second();
                   return new Pair<>(max, min);
                })
                .map(pair -> pair.first() - pair.second())
                .toMat(Sink.head(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

    }

//    private ArrayList<Pair<String, String>> insert(ArrayList<Pair<String, String>> trans, Pair<String, String> change ) { //changes AB -> C
//
//        ArrayList<Pair<String, String>> out = new ArrayList(trans);
//
//        Pair<String, String> changeFrom = new Pair<>(String.valueOf(change.first().charAt(0)), String.valueOf(change.first().charAt(1)));
//
//        for (int i = 0; i < trans.size(); i++) {
//
//        }
//
//        while(out.contains(changeFrom)){
//            int index = out.indexOf(changeFrom);
//            out.remove(index);
//            out.set(index, new Pair(changeFrom.first(), change.second()));
//            out.set(index+1, new Pair(change.second(), changeFrom.second()));
//        }
//
//        return new ArrayList<>(out);
//    }


    private Map<Pair<String, String>, String> getPaths(String fileName) throws ExecutionException, InterruptedException {
//        final List<Pair<String, String>> inSource =
//        List<Pair<Pair<String, String>, String>> list =
                return FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day14/" + fileName + ".txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .map(line -> {
                    String[] xy = line.split(" -> ");
                    return new Pair<Pair<String, String>, String>(new Pair<>(String.valueOf(xy[0].charAt(0)), String.valueOf(xy[0].charAt(1))), xy[1]);
                })
                .toMat(Sink.seq(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get().stream().collect(Collectors.toMap(x -> x.first(), y -> y.second()));




    }

}
