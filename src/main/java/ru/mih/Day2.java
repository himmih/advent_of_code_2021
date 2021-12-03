package ru.mih;

import akka.actor.ActorSystem;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Day2 {


    public static void main(String[] args) throws ExecutionException, InterruptedException, URISyntaxException {
        final ActorSystem system = ActorSystem.create("Day2");

        Source<ByteString, CompletionStage<IOResult>> movesSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day2/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW));


        final CompletionStage<Position> future = movesSource.runWith(foundPosition(), system);

        future
            .thenAccept(result -> System.out.println(result))
            .thenAccept(any -> system.terminate())
            .toCompletableFuture().get();

    }

    public static class PositionWithAim extends Position {
        final public int aim;

        public static PositionWithAim START = new PositionWithAim(0, 0, 0);

        public PositionWithAim(int horizontal, int depth, int aim) {
            super(horizontal, depth);
            this.aim = aim;
        }

        public CompletionStage<PositionWithAim> goWithAim(String move) {
            if (move.startsWith("forward")) {
                return CompletableFuture.supplyAsync(
                        () -> {
                            int units = parse(move);
                            return new PositionWithAim(horizontal + units, depth + aim * units, aim);
                        });
            } else if (move.startsWith("down")) {
                return CompletableFuture.supplyAsync(
                        () -> new PositionWithAim(horizontal, depth, aim + parse(move)));
            } else if (move.startsWith("up")) {
                return CompletableFuture.supplyAsync(
                        () -> new PositionWithAim(horizontal, depth, aim - parse(move)));
            } else {
                throw new IllegalStateException("wrong move - " + move);
            }
        }
    }

    public static class Position{
        final public int horizontal;
        final public int depth;

        public static Position START = new Position(0, 0);

        public Position(int horizontal, int depth) {
            this.horizontal = horizontal;
            this.depth = depth;
        }

        public CompletionStage<Position> go(String move){
            if (move.startsWith("forward")){
                return CompletableFuture.supplyAsync(() -> new Position(horizontal + parse(move), depth));
            }else if(move.startsWith("down")){
                return CompletableFuture.supplyAsync(() -> new Position(horizontal, depth + parse(move)));
            }else if(move.startsWith("up")) {
                return CompletableFuture.supplyAsync(() -> new Position(horizontal, depth - parse(move)));
            }else {
                throw new IllegalStateException("wrong move - " + move);
            }
        }

        public int parse(String move) {
            int i = Integer.parseInt(move.replaceAll("[^0-9]", ""));
            return i;
        }

        @Override
        public String toString() {
            return "h: " + horizontal + ", d: " + depth + ", result: " + horizontal*depth;
        }
    }


    public static Sink<ByteString, CompletionStage<Position>> foundPosition(){
        return Flow.of(ByteString.class)
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .foldAsync(Position.START, (acc, element) -> acc.go(element))
                .toMat(Sink.head(), Keep.right());
    }

    public static Sink<ByteString, CompletionStage<PositionWithAim>> foundPositionWithAim(){
        return Flow.of(ByteString.class)
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .foldAsync(PositionWithAim.START, (acc, element) -> acc.goWithAim(element))
                .toMat(Sink.head(), Keep.right());
    }

}
