package ru.mih;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.util.ByteString;
import org.junit.AfterClass;
import org.junit.Test;
import ru.mih.day3.DiagnosticReport;
import ru.mih.day3.LifeSupport;

import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static ru.mih.day3.DiagnosticReport.foundConditions;
import static ru.mih.day3.LifeSupport.foundOxygen;

public class TestDay3 {

    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @AfterClass
    public static void terminate(){
        testSystem.terminate();
    }


    @Test
    public void testPartOneExample() throws ExecutionException, InterruptedException, TimeoutException {
        final CompletionStage<DiagnosticReport> future
            = FileIO.fromPath(
                    Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day3/test1.txt"))
            .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                        .runWith(foundConditions(), testSystem);


        final DiagnosticReport result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals("10110", result.getGammaRateBinary());
        assertEquals("01001", result.getEpsilonRateBinary());
        assertEquals(22, result.getGammaRate());
        assertEquals(9, result.getEpsilonRate());
    }

    @Test
    public void testPartOneExample2() throws ExecutionException, InterruptedException, TimeoutException {
        final CompletionStage<DiagnosticReport> future
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day3/test1.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .runWith(foundConditions(), testSystem);


        final DiagnosticReport result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals("GR(B): 10110, GR(D): 22, ER(B): 01001, ER(D): 9, PS(D): 198", result.toString());

    }

    @Test
    public void testPartOne() throws ExecutionException, InterruptedException, TimeoutException {
        ActorSystem testSystem = ActorSystem.create("TestSystem2");
        final CompletionStage<DiagnosticReport> future
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day3/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .runWith(foundConditions(), testSystem);


        final DiagnosticReport result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);

        assertEquals("GR(B): 010010010110, GR(D): 1174, ER(B): 101101101001, ER(D): 2921, PS(D): 3429254", result.toString());

        assertEquals(result.getGammaRate()* result.getEpsilonRate(), result.getPowerConsumption());

    }

    @Test
    public void testPartTwo() throws ExecutionException, InterruptedException, TimeoutException {
        final CompletionStage<Done> future
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day3/test1.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .runWith(foundOxygen(), testSystem);


        final Done result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertEquals("GR(B): 10110, GR(D): 22, ER(B): 01001, ER(D): 9, PS(D): 198", result.toString());

    }
}
