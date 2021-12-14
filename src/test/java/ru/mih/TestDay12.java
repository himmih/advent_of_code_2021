package ru.mih;

import akka.actor.ActorSystem;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TestDay12 {


    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");


    @Test
    public void loadPaths21() throws ExecutionException, InterruptedException {
        final String fileName = "test1";
        List<List<String>> out = getPaths(fileName, true);
        assertEquals(36, out.size());

    }

    @Test
    public void loadPaths22() throws ExecutionException, InterruptedException {
        final String fileName = "test2";
        List<List<String>> out = getPaths(fileName, true);
        assertEquals(103, out.size());

    }

    @Test
    public void loadPaths23() throws ExecutionException, InterruptedException {
        final String fileName = "test3";
        List<List<String>> out = getPaths(fileName, true);
        assertEquals(3509, out.size());

    }

    @Test
    public void loadPaths24() throws ExecutionException, InterruptedException {
        final String fileName = "test4";
        List<List<String>> out = getPaths(fileName, true);
        assertEquals(89592, out.size());

    }

    @Test
    public void loadPaths1() throws ExecutionException, InterruptedException {
        final String fileName = "test1";
        List<List<String>> out = getPaths(fileName, false);
        assertEquals(10, out.size());

    }
    @Test
    public void loadPaths2() throws ExecutionException, InterruptedException {
        final String fileName = "test2";
        List<List<String>> out = getPaths(fileName, false);
        assertEquals(19, out.size());

    }
    @Test
    public void loadPaths3() throws ExecutionException, InterruptedException {
        final String fileName = "test3";
        List<List<String>> out = getPaths(fileName, false);
        assertEquals(226, out.size());

    }
    @Test
    public void loadPaths4() throws ExecutionException, InterruptedException {
        final String fileName = "test4";
        List<List<String>> out = getPaths(fileName, false);
        assertEquals(3292, out.size());

    }
    private List<List<String>> getPaths(String fileName, boolean secondPart) throws ExecutionException, InterruptedException {
        final Source<HashMap<String, List<String>>, CompletionStage<IOResult>> inSource =
                FileIO.fromPath(
                                Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day12/" + fileName + ".txt"))
                        .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                        .filterNot(ByteString::isEmpty)
                        .map(bs -> bs.utf8String())
                        .fold(new LinkedHashMap<>(), (acc, a) -> {
                            String[] xy = a.split("-");
                            List<String> out = acc.computeIfAbsent(xy[0], key -> new ArrayList<>());
                            out.add(xy[1]);
                            out = acc.computeIfAbsent(xy[1], key -> new ArrayList<>());
                            out.add(xy[0]);
                            return acc;
                        });

        HashMap<String, List<String>> g = inSource
                .toMat(Sink.head(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

        if (secondPart)
            return dfs2("start", g, new HashMap<>(), new ArrayList<String>(), new ArrayList<>());
        else
            return dfs("start", g, new HashSet<>(), new ArrayList<String>(), new ArrayList<>());
    }

    private List<List<String>> dfs(String v, HashMap<String, List<String>> g, Set<String> used, ArrayList<String> path, List<List<String>> out){
        if (v.equals("end")) { path.add("end"); out.add(path); return out; } else { path.add(v); }

        if (v.equals(v.toLowerCase())) used.add(v);

        for(String n: g.get(v)){
           if (!"start".equals(n) && !used.contains(n)){
               dfs(n, g, new HashSet<>(used), new ArrayList<>(path), out);
           }
        }

        return out;

    }

    private List<List<String>> dfs2(String v, HashMap<String, List<String>> g,
                                    HashMap<String, Integer> used, ArrayList<String> path, List<List<String>> out){
        if (v.equals("end")) { path.add("end"); out.add(path); return out; } else { path.add(v); }
        if (v.equals(v.toLowerCase())){
            used.computeIfPresent(v, (key,value) -> 2);
            used.putIfAbsent(v, 1);
        }
        for(String n: g.get(v)){
            if ( !"start".equals(n)
                    && (!n.equals(n.toLowerCase())
                    || !used.containsKey(n)
                    || !used.values().stream().filter(x -> x == 2).findAny().isPresent()) ){
                dfs2(n, g, new HashMap<>(used), new ArrayList<>(path), out);
            }
        }
        return out;

    }

}
