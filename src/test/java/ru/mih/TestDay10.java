package ru.mih;

import akka.actor.ActorSystem;
import akka.japi.pf.PFBuilder;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestDay10 {

    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @Test
    public void testScanFirstLine() throws ExecutionException, InterruptedException {

        assertEquals(Integer.valueOf(1197), getWrongSymbol("{([(<{}[<>[]}>{[]{[(<()>"));
        assertEquals(Integer.valueOf(3), getWrongSymbol("[[<[([]))<([[{}[[()]]]"));
        assertEquals(Integer.valueOf(57), getWrongSymbol("[{[{({}]{}}([{[{{{}}([]"));
        assertEquals(Integer.valueOf(3), getWrongSymbol("[<(<(<(<{}))><([]([]()"));
        assertEquals(Integer.valueOf(25137), getWrongSymbol("<{([([[(<>()){}]>(<<{{"));
        assertEquals(Integer.valueOf(57), getWrongSymbol("]"));

        //corrEquals
        assertEquals(Integer.valueOf(0), getWrongSymbol("["));
        assertEquals(Integer.valueOf(0), getWrongSymbol("[({(<(())[]>[[{[]{<()<>>"));

    }

    @Test
    public void testSolve() throws ExecutionException, InterruptedException {

        assertEquals(Integer.valueOf(288957), getSolve("[({(<(())[]>[[{[]{<()<>>"));
        assertEquals(Integer.valueOf(5566), getSolve("[(()[<>])]({[<{<<[]>>("));
        assertEquals(Integer.valueOf(1480781), getSolve("(((({<>}<{<{<>}{[]{[]{}"));
        assertEquals(Integer.valueOf(995444), getSolve("{<[[]]>}<{[{[{[]{()[[[]"));
        assertEquals(Integer.valueOf(294), getSolve("<{([{{}}[<[[[<>{}]]]>[]]"));
        assertEquals(Integer.valueOf(0), getSolve("{([(<{}[<>[]}>{[]{[(<()>"));
        assertEquals(Integer.valueOf(0), getSolve("]"));
        assertEquals(Integer.valueOf(0), getSolve("[]"));
//
//        //corrEquals
//        assertEquals(Integer.valueOf(0), getWrongSymbol("["));
//        assertEquals(Integer.valueOf(0), getWrongSymbol("[({(<(())[]>[[{[]{<()<>>"));

    }

    @Test
    public void testFirst() throws ExecutionException, InterruptedException {

                FileIO.fromPath(
                                Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day10/test2.txt"))
                        .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                        .filterNot(ByteString::isEmpty)
                        .map(bs -> bs.utf8String())
                        .map(line -> getWrongSymbol(line))
                        .reduce((acc, a) -> acc+a)
                        .toMat(Sink.foreach(System.out::println), Keep.right())
                        .run(testSystem)
                        .toCompletableFuture()
                        .get();
    }

    @Test
    public void testSecond() throws ExecutionException, InterruptedException {

        List<Number> out = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day10/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .map(line -> getSolve(line))
                .filter(x -> x.longValue() != 0)
                .toMat(Sink.seq(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();

        out = out.stream().sorted().collect(Collectors.toList());
        System.out.println(out.get(out.size()/2));



    }
    public Number getSolve(String line) throws ExecutionException, InterruptedException {

        return
                Source.from(line.chars()
                        .mapToObj(c -> (char) c).collect(Collectors.toList()))
                .fold(new ArrayList<Character>(), (acc, elm) -> {

                    Character lastElm = (acc.size() > 0) ? acc.get(acc.size() - 1) : Character.MIN_VALUE;

                    if (isClosing(elm)) {
                        if (elm == ')' && lastElm.equals('(')
                                || elm == '}' && lastElm.equals('{')
                                || elm == ']' && lastElm.equals('[')
                                || elm == '>' && lastElm.equals('<')) {
                            acc = removeLast(acc);
                        } else {
                            throw new IllegalStateException("");
                        }
                    } else {// if (isOpening(elm)) {
                        acc = add(acc, elm);
                    }
                    return acc;
                })
                .recover(
                        new PFBuilder<Throwable, ArrayList<Character>>()
                                .match(IllegalStateException.class, th -> {
                                    ArrayList<Character> out = new ArrayList<>();
                                    out.add(Character.MIN_VALUE);
                                    return out;
                                })
                                .build())
                        .map(list -> {
                            if (list.size() == 0) return 0;
                            if (list.get(0) == Character.MIN_VALUE) return 0;

                            ArrayList<Integer> inverted = (ArrayList<Integer>) list.stream().map(ch -> invert(ch)).collect(Collectors.toList());
                            Collections.reverse(inverted);
                            long acc = 0;
                            for(Integer c: inverted){
                               acc = acc * 5 + c;
                            }
                            return Long.valueOf(acc);
                        })
                .toMat(Sink.head(), Keep.right())
                .run(testSystem)
                .toCompletableFuture().get();
    }

    private List<Integer> findSolution(ArrayList<Character> acc) {

        List<Integer> solution = new ArrayList<>();
        for(int j = acc.size() -1; j >= 0; j--){
            solution.add(invert(acc.get(j)));
        }
        Collections.reverse(solution);
        return solution;
    }

    private Integer invert(Character ch) {
        switch (ch){
            case '(': {return 1;}
            case '[': {return 2;}
            case '{': {return 3;}
            case '<': {return 4;}
        }
        return 0;
    }

    public Integer getWrongSymbol(String line) throws ExecutionException, InterruptedException {

        return Source.from(line.chars()
                        .mapToObj(c -> (char) c).collect(Collectors.toList()))
                .fold(new ArrayList<Character>(), (acc, elm) -> {

                    Character lastElm = (acc.size() > 0) ? acc.get(acc.size() - 1) : Character.MIN_VALUE;

                    if (isClosing(elm)) {
                        if (elm == ')' && lastElm.equals('(')
                                || elm == '}' && lastElm.equals('{')
                                || elm == ']' && lastElm.equals('[')
                                || elm == '>' && lastElm.equals('<')) {
                            acc = removeLast(acc);
                        } else {
                            throw new IllegalStateException("" + elm);
                        }
                    } else {// if (isOpening(elm)) {
                        acc = add(acc, elm);
                    }
                    return acc;
                })
                .recover(
                        new PFBuilder<Throwable, ArrayList<Character>>()
                                .match(IllegalStateException.class, th -> {
                                    ArrayList<Character> out = new ArrayList<>();
                                    out.add(Character.valueOf(th.getMessage().charAt(0)));
                                    return out;
                                })
                                .build())
                .fold(-1, (acc, ch) -> {
                    if (acc > -1){
                        return acc;
                    } else {
                        if (isClosing(ch.get(0))){
                            switch (ch.get(0)){
                                case ')': {acc = 3;break;}
                                case ']': {acc = 57;break;}
                                case '}': {acc = 1197;break;}
                                case '>': {acc = 25137;break;}
                            }
                            return acc;
                        }
                        return 0;
                    }
                })
                .toMat(Sink.head(), Keep.right())
                .run(testSystem)
                .toCompletableFuture()
                .get();
    }


    private ArrayList<Character> add(List<Character> in, Character el){
        ArrayList<Character> list = new ArrayList<>();
        for(Character ch: in){
            list.add(ch);
        }
        list.add(el);
        return list;
    }

    private ArrayList<Character> removeLast(List<Character> in){
        ArrayList<Character> list = new ArrayList<>();
        for(int i = 0; i < in.size()-1; i++){
            list.add(in.get(i));
        }
        return list;
    }


    private boolean isOpening(Character ch) {
        return (ch == '(' || ch == '{' || ch == '[' || ch == '<');
    }

    private boolean isClosing(Character ch) {
        return (ch == ')' || ch == '}' || ch == ']' || ch == '>');
    }
}

