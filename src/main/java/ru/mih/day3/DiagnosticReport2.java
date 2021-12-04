package ru.mih.day3;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.*;
import akka.stream.javadsl.*;
import akka.stream.scaladsl.UnzipWith4;
import akka.stream.scaladsl.UnzipWithApply;
import akka.util.ByteString;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class DiagnosticReport2 {


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final ActorSystem system = ActorSystem.create("TestSystem");

        Source<String, ?> stringsSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day3/test1.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String());


        Sink<Integer, CompletionStage<Done>> printSink = Sink.foreach(System.out::println);
        final Flow<String, Integer, NotUsed> f1 = Flow.of(String.class)
                .map(line -> '1' == line.charAt(0)?1:0).reduce( (a, b) -> a+b );
        final Flow<String, Integer, NotUsed> f2 = Flow.of(String.class)
                .map(line -> '1' == line.charAt(1)?1:0).reduce( (a, b) -> a+b );
        final Flow<String, Integer, NotUsed> f3 = Flow.of(String.class)
                .map(line -> '1' == line.charAt(2)?1:0).reduce( (a, b) -> a+b );
        final Flow<String, Integer, NotUsed> f4 = Flow.of(String.class)
                .map(line -> '1' == line.charAt(3)?1:0).reduce( (a, b) -> a+b );
        final Flow<String, Integer, NotUsed> f5 = Flow.of(String.class)
                .map(line -> '1' == line.charAt(4)?1:0).reduce( (a, b) -> a+b );

        final RunnableGraph<CompletionStage<Done>> result =
            RunnableGraph.fromGraph(
                GraphDSL
                    .create(
                        printSink,
                        (builder, out) -> {
                            final Outlet<String> source = builder.add(stringsSource).out();
                            final UniformFanOutShape<String, String> bcast = builder.add(Broadcast.create(5));

                            final UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(5));


                            builder.from(source).viaFanOut(bcast).via(builder.add(f1))
                                    .viaFanIn(merge).to(out);

                            builder.from(bcast).via(builder.add(f2)).toFanIn(merge);
                            builder.from(bcast).via(builder.add(f3)).toFanIn(merge);
                            builder.from(bcast).via(builder.add(f4)).toFanIn(merge);
                            builder.from(bcast).via(builder.add(f5)).toFanIn(merge);


                            return ClosedShape.getInstance();
                        }));



        result.run(system).toCompletableFuture().get();


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
