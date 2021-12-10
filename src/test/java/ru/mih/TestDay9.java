package ru.mih;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.Test;
import scala.util.control.Exception;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TestDay9 {

    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @Test
    public void testFirstOne() throws ExecutionException, InterruptedException {

        final Source<String, CompletionStage<IOResult>> inSource =
                FileIO.fromPath(
                                Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day9/test2.txt"))
                        .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                        .filterNot(ByteString::isEmpty)
                        .map(bs -> bs.utf8String());

        List<List<Byte>> matix = new ArrayList<>();

        inSource
                .toMat(Sink.foreach(line -> {
                    List<Byte> row = new ArrayList<>();
                    for(int j=0; j < line.length();j++){
                        row.add(Byte.parseByte(line.substring(j, j+1)));
                    }
                    matix.add(row);
                }), Keep.right())
                .run(testSystem)
                .toCompletableFuture().get();

        int maxC = matix.get(0).size();

        List<Pair<Integer, Integer>> locMins = new ArrayList<>();

        for(int r = 0; r < matix.size(); r++){
            for(int c = 0; c < maxC; c++){
                Byte rcCheck = matix.get(r).get(c);
                Byte left = (c > 0) ? matix.get(r).get(c-1):9;
                Byte right = (c < maxC-1) ? matix.get(r).get(c+1):9;
                Byte up = (r > 0) ? matix.get(r-1).get(c):9;
                Byte down = (r < matix.size()-1) ? matix.get(r+1).get(c):9;
                if (rcCheck < left && rcCheck < right && rcCheck < up && rcCheck < down) locMins.add(new Pair<>(r,c));
            }
        }

        System.out.println(locMins.stream()
                .map(pair -> matix.get(pair.first()).get(pair.second())+1)
                        .reduce((a,b) -> a+b));


    }

}
