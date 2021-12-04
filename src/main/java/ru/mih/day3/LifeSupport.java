package ru.mih.day3;

import akka.Done;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class LifeSupport {


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final ActorSystem system = ActorSystem.create("TestSystem");

        Source<String, ?> stringsSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day3/test2_header.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String());

        String oxygenBinary = getOxygen(system, stringsSource);
        String co2Binary = getCO2(system, stringsSource);


        int oxygen =  Integer.parseInt(oxygenBinary, 2);
        int co2 =  Integer.parseInt(co2Binary, 2);

        System.out.println("oxygen: " + oxygenBinary + ", D: " + oxygen);
        System.out.println("co2: " + co2Binary + ", D: " + co2);
        System.out.println("multiply: " + oxygen*co2);


        system.terminate();


    }

    private static String getOxygen(ActorSystem system, Source<String, ?> stringsSource) throws InterruptedException, ExecutionException {
        Sink<String, CompletionStage<String>> getFirst = Flow.of(String.class)
                .toMat(Sink.head(), Keep.right());

        String firstLine = stringsSource.runWith(getFirst, system).toCompletableFuture().get();

        String foundNumber = null;

        for(int i = 0; i< firstLine.length(); i++){
            final int j = i;
            final char max = stringsSource //<-- todo it changing
                    .runWith(findMax(i), system).toCompletableFuture().get();

            List<String> nextList = stringsSource
                    .filter(line -> {
                        return line.charAt(j) == max;
                    })
                    .runWith(Sink.seq(), system).toCompletableFuture().get();

            if (nextList.size() == 1) {
                foundNumber = nextList.get(0);
                break;
            }

            stringsSource = Source.from(nextList);

        }
        return foundNumber;
    }

    private static String getCO2(ActorSystem system, Source<String, ?> stringsSource) throws InterruptedException, ExecutionException {
        Sink<String, CompletionStage<String>> getFirst = Flow.of(String.class)
                .toMat(Sink.head(), Keep.right());

        String firstLine = stringsSource.runWith(getFirst, system).toCompletableFuture().get();

        String foundNumber = null;

        for(int i = 0; i< firstLine.length(); i++){
            final int j = i;
            final char min = stringsSource //<-- todo it changing
                    .runWith(findMin(i), system).toCompletableFuture().get();

            System.out.println("index: " + j + " - " + min);

            List<String> nextList = stringsSource
                    .filter(line -> (line.charAt(j) == min))
                    .runWith(Sink.seq(), system).toCompletableFuture().get();

            if (nextList.size() == 1) {
                foundNumber = nextList.get(0);
                break;
            }

            stringsSource = Source.from(nextList);

        }
        return foundNumber;
    }

    public static Sink<String, CompletionStage<Character>> findMax(final int index){
        return Flow.of(String.class)
                .fold(new Pair<Integer, Integer>(0, 0), (acc, element) -> {
                    if (element.charAt(index) == '1'){
                        return new Pair(acc.first() + 1, acc.second()+1);
                    }else{
                        return new Pair(acc.first(), acc.second()+1);
                    }
                })
                .via(Flow.fromFunction(p -> {
                   Pair<Integer, Integer> pair = (Pair<Integer, Integer>) p;
                    return (pair.first() >= pair.second() - pair.first())?'1':'0';
                }))
                .toMat(Sink.head(), Keep.right());
    }

    public static Sink<String, CompletionStage<Character>> findMin(final int index){
        return Flow.of(String.class)
                .fold(new Pair<Integer, Integer>(0, 0), (acc, element) -> {
                    if (element.charAt(index) == '1'){
                        return new Pair(acc.first() + 1, acc.second()+1);
                    }else{
                        return new Pair(acc.first(), acc.second()+1);
                    }
                })
                .via(Flow.fromFunction(p -> {
                    Pair<Integer, Integer> pair = (Pair<Integer, Integer>) p;
                    return (pair.first() < pair.second() - pair.first())?'1':'0';
                }))
                .toMat(Sink.head(), Keep.right());
    }


    public static Sink<ByteString, CompletionStage<Done>> foundOxygen(){
        return Flow.of(ByteString.class)
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .toMat(Sink.foreach(System.out::println), Keep.right());
    }


}
