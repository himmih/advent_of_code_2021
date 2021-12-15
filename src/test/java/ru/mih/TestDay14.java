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
        assertEquals(5725739914284L, countDiff("CKKOHNSBPCPCHVNKHFFK", "test2"));
    }

    @Test
    public void testOne2() throws ExecutionException, InterruptedException {
        assertEquals(5725739914283L, countDiff("CKKOHNSBPCPCHVNKHFFK", "test2"));
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

        final Flow<Pair<String, String>, Pair<String, String>, NotUsed> changeFlow =
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

        for (int i = 0; i < 4; i++) { //< step - 1
            workFlow = workFlow.via(changeFlow);
        }
        LinkedHashMap<Pair<String, String>, Long> firstStep =
               start.via(startFlow)
                .via(workFlow)
                .fold(new LinkedHashMap<Pair<String, String>, Long>(), (groups, pair) -> {
                    if (groups.containsKey(pair)){
                        groups.put(pair, groups.get(pair)+1L);
                    }else {
                        groups.put(pair, 1L) ;
                    }
                    return new LinkedHashMap<>(groups);
                })
               .toMat(Sink.head(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

        return Source.from(firstStep.entrySet())
                .mapAsync(8, entry -> {
                    Flow<Pair<String,String>, Pair<String, String>, NotUsed> makeLine = changeFlow;
                    for (int i = 0; i < 4; i++) { //< step - 1
                        makeLine = makeLine.via(changeFlow);
                    }
                    String lastLetter = Source.single(entry.getKey()).
                            via(makeLine)
                            .map(pair -> pair.second())
                            .toMat(Sink.last(), Keep.right())
                            .run(testSystem)
                            .toCompletableFuture()
                            .get();

                    return Source.single(entry.getKey())
                            .via(makeLine)
                            .map(pair -> pair.first())
                            .fold(new HashMap<String, Long>(), (groups, ch) -> {
                                if (groups.containsKey(ch)){
                                    groups.put(ch, groups.get(ch)+1L);
                                }else {
                                    if (ch.equals(lastLetter)) {
                                        groups.put(ch, 1L);
                                    }else {
                                        groups.put(ch, 1L);
                                    }
                                }
                                return new HashMap<>(groups);
                            })
                            .map(map -> {
                                HashMap<String, Long> out = new HashMap<>();
                                for(Map.Entry<String, Long> e: map.entrySet()){
                                    out.put(e.getKey(), e.getValue()*entry.getValue());
                                }
                                return out;
                                })
                            .toMat(Sink.head(), Keep.right())
                            .run(testSystem)
                            .toCompletableFuture();
                })
                .fold(new HashMap<String, Long>(), (acc, b) -> {
                    HashMap<String, Long> out  = new LinkedHashMap<>(acc);
                        for(Map.Entry<String, Long> pair: b.entrySet()){
                            if (out.containsKey(pair.getKey())){
                                out.put(pair.getKey(), out.get(pair.getKey()) + pair.getValue());
                            }else {
                                out.put(pair.getKey(), pair.getValue());
                            }
                        }
                        return out;
                })
                .mapConcat(map -> map.values())
                .fold(new Pair<>(0L, Long.MAX_VALUE), (acc, i) -> {
                    long max = (i > acc.first())?i:acc.first();
                    long min = (i < acc.second())?i:acc.second();
                    return new Pair<>(max, min);
                })
                .map(pair -> pair.first() - pair.second() - 1)
                .toMat(Sink.head(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

    }

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
    public long countDiffFirst(String in, String fileChanges) throws ExecutionException, InterruptedException {
        Map<Pair<String, String>, String> changes = getPaths(fileChanges);

        Source<String, NotUsed> start =
                Source.from(in.chars()
                        .mapToObj(x -> String.valueOf((char)x)).collect(Collectors.toList()));

        Flow<String, Pair<String, String>, NotUsed> startFlow =
                Flow.of(String.class)
                        .sliding(2, 1)
                        .map(list -> new Pair<String, String>(list.get(0), list.get(1)));

        final Flow<Pair<String, String>, Pair<String, String>, NotUsed> changeFlow =
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

        for (int i = 0; i < 4; i++) { //< step - 1
            workFlow = workFlow.via(changeFlow);
        }
        LinkedHashMap<Pair<String, String>, Long> firstStep =
                start.via(startFlow)
                        .via(workFlow)
                        .fold(new LinkedHashMap<Pair<String, String>, Long>(), (groups, pair) -> {
                            if (groups.containsKey(pair)){
                                groups.put(pair, groups.get(pair)+1L);
                            }else {
                                groups.put(pair, 1L) ;
                            }
                            return new LinkedHashMap<>(groups);
                        })
                        .toMat(Sink.head(), Keep.right())
                        .run(testSystem)
                        .toCompletableFuture()
                        .get();

        return Source.from(firstStep.entrySet())
                .mapAsync(8, entry -> {
                    Flow<Pair<String,String>, Pair<String, String>, NotUsed> makeLine = changeFlow;
                    for (int i = 0; i < 4; i++) { //< step - 1
                        makeLine = makeLine.via(changeFlow);
                    }
                    String lastLetter = Source.single(entry.getKey()).
                            via(makeLine)
                            .map(pair -> pair.second())
                            .toMat(Sink.last(), Keep.right())
                            .run(testSystem)
                            .toCompletableFuture()
                            .get();

                    return Source.single(entry.getKey())
                            .via(makeLine)
                            .map(pair -> pair.first())
                            .fold(new HashMap<String, Long>(), (groups, ch) -> {
                                if (groups.containsKey(ch)){
                                    groups.put(ch, groups.get(ch)+1L);
                                }else {
                                    if (ch.equals(lastLetter)) {
                                        groups.put(ch, 1L);
                                    }else {
                                        groups.put(ch, 1L);
                                    }
                                }
                                return new HashMap<>(groups);
                            })
                            .map(map -> {
                                HashMap<String, Long> out = new HashMap<>();
                                for(Map.Entry<String, Long> e: map.entrySet()){
                                    out.put(e.getKey(), e.getValue()*entry.getValue());
                                }
                                return out;
                            })
                            .toMat(Sink.head(), Keep.right())
                            .run(testSystem)
                            .toCompletableFuture();
                })
                .fold(new HashMap<String, Long>(), (acc, b) -> {
                    HashMap<String, Long> out  = new LinkedHashMap<>(acc);
                    for(Map.Entry<String, Long> pair: b.entrySet()){
                        if (out.containsKey(pair.getKey())){
                            out.put(pair.getKey(), out.get(pair.getKey()) + pair.getValue());
                        }else {
                            out.put(pair.getKey(), pair.getValue());
                        }
                    }
                    return out;
                })
                .mapConcat(map -> map.values())
                .fold(new Pair<>(0L, Long.MAX_VALUE), (acc, i) -> {
                    long max = (i > acc.first())?i:acc.first();
                    long min = (i < acc.second())?i:acc.second();
                    return new Pair<>(max, min);
                })
                .map(pair -> pair.first() - pair.second() - 1)
                .toMat(Sink.head(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

    }

}
