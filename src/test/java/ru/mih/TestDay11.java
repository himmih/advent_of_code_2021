package ru.mih;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.Test;

import java.nio.channels.Pipe;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestDay11 {

    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @Test
    public void testFlash() {

        List<List<Byte>> matrix = loadMatrix("test2.txt");
        upLevel(matrix);
        flashes(matrix);
        printMatrix(matrix);

        System.out.println();

        matrix = loadMatrix("test3.txt");
        upLevel(matrix);
        flashes(matrix);
        printMatrix(matrix);

        System.out.println();

        matrix = loadMatrix("test4.txt.txt");
        upLevel(matrix);
        flashes(matrix);
        printMatrix(matrix);

    }

    @Test
    public void testAllFlash0() {
        List<List<Byte>> matrix = loadMatrix("test0.txt");
//        printMatrix(matrix);
//        System.out.println();
//        System.out.println();

        for(int i = 0; i < 2; i++ ) {
            List<Pair<Integer, Integer>> newFlasher = upLevel(matrix);
            while (newFlasher.size() > 0) {
                newFlasher = flash(matrix, newFlasher);
            }
            zero(matrix);
            printMatrix(matrix);
            System.out.println();
            System.out.println();
        }
    }

    @Test
    public void testAllFlash5() {
        List<List<Byte>> matrix = loadMatrix("test5.txt");
//        printMatrix(matrix);
//        System.out.println();
//        System.out.println();

        long zeros = 0;

        for(int i = 0; i < 100; i++ ) {
            List<Pair<Integer, Integer>> newFlasher = upLevel(matrix);
            while (newFlasher.size() > 0) {
                newFlasher = flash(matrix, newFlasher);
            }
            zeros += zero(matrix);
//            printMatrix(matrix);
//            System.out.println();
        }
        assertEquals(1625, zeros);

    }

    @Test
    public void testAllFlash1() {
        List<List<Byte>> matrix = loadMatrix("test1.txt");
//        printMatrix(matrix);
//        System.out.println();
//        System.out.println();

        long zeros = 0;

        for(int i = 0; i < 100; i++ ) {
            List<Pair<Integer, Integer>> newFlasher = upLevel(matrix);
            while (newFlasher.size() > 0) {
                newFlasher = flash(matrix, newFlasher);
            }
            zeros += zero(matrix);
        }

//        printMatrix(matrix);
//        System.out.println();
        assertEquals(1656, zeros);

    }

    @Test
    public void testAllFlashSync() {
        List<List<Byte>> matrix = loadMatrix("test5.txt");
//        printMatrix(matrix);
//        System.out.println();
//        System.out.println();

        int newZeros = 0;
        int i = 0;
        while (newZeros != matrix.size()*matrix.get(0).size()){
            i++;
            List<Pair<Integer, Integer>> newFlasher = upLevel(matrix);
            while (newFlasher.size() > 0) {
                newFlasher = flash(matrix, newFlasher);
            }
            newZeros = zero(matrix);
        }
        System.out.println("Found: " + i);

        printMatrix(matrix);
        System.out.println();
        assertEquals(195, i);

    }

    private int zero(List<List<Byte>> matrix) {
        int out = 0;
        for(int r = 0; r < matrix.size(); r++) {
            for (int c = 0; c < matrix.get(r).size(); c++) {
                if (matrix.get(r).get(c) > 9) {
                    matrix.get(r).set(c, (byte) 0);
                    out++;
                }
            }
        }
        return out;
    }

    private void printMatrix(List<List<Byte>> matrix) {
        for(int r = 0; r < matrix.size(); r++) {
            for(int c = 0; c < matrix.get(r).size(); c++) {
                String out = String.valueOf(matrix.get(r).get(c));
                out = (out.length() > 1) ? " " + out : "  " + out;
                System.out.print(out);
            }
            System.out.println();
        }
    }

    private List<List<Byte>> loadMatrix(String fileName){
        try {
            List<List<Byte>> matrix = new ArrayList<>();

            final Source<String, CompletionStage<IOResult>> inSource =
                    FileIO.fromPath(
                                    Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day11/" + fileName))
                            .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                            .filterNot(ByteString::isEmpty)
                            .map(bs -> bs.utf8String());
            inSource
                    .toMat(Sink.foreach(line -> {
                        List<Byte> row = new ArrayList<>();
                        for(int j=0; j < line.length();j++){
                            row.add(Byte.parseByte(line.substring(j, j+1)));
                        }
                        matrix.add(row);
                    }), Keep.right())
                    .run(testSystem)
                    .toCompletableFuture().get();

            return matrix;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private List<Pair<Integer, Integer>> flash(List<List<Byte>> matrix, List<Pair<Integer, Integer>> inFlashers) {
        List<Pair<Integer, Integer>> newFlashers = new ArrayList<>();
        for(Pair<Integer, Integer> flasher: inFlashers){
            int r = flasher.first(); int c = flasher.second();
            Pair<Integer, Integer> a = upCell(matrix, r-1, c-1); if (a != null) newFlashers.add(a) ;
            a = upCell(matrix, r-1, c); if (a != null) newFlashers.add(a) ;
            a = upCell(matrix, r-1, c + 1); if (a != null) newFlashers.add(a) ;
            a = upCell(matrix, r, c + 1); if (a != null) newFlashers.add(a) ;
            a = upCell(matrix, r+1, c + 1); if (a != null) newFlashers.add(a) ;
            a = upCell(matrix, r+1, c); if (a != null) newFlashers.add(a) ;
            a = upCell(matrix, r+1, c - 1); if (a != null) newFlashers.add(a) ;
            a = upCell(matrix, r, c - 1); if (a != null) newFlashers.add(a) ;
        }
        return newFlashers;
    }

    private List<Pair<Integer, Integer>> flashes(List<List<Byte>> matrix) {
        List<Pair<Integer, Integer>> newFlashers = new ArrayList<>();
        for(int r = 0; r < matrix.size(); r++){
            for(int c = 0; c < matrix.get(r).size(); c++){
                if (matrix.get(r).get(c) < 10){
                    Pair<Integer, Integer> a = upCell(matrix, r-1, c-1); if (a != null) newFlashers.add(a) ;
                    a = upCell(matrix, r-1, c); if (a != null) newFlashers.add(a) ;
                    a = upCell(matrix, r-1, c + 1); if (a != null) newFlashers.add(a) ;
                    a = upCell(matrix, r, c + 1); if (a != null) newFlashers.add(a) ;
                    a = upCell(matrix, r+1, c + 1); if (a != null) newFlashers.add(a) ;
                    a = upCell(matrix, r+1, c); if (a != null) newFlashers.add(a) ;
                    a = upCell(matrix, r+1, c - 1); if (a != null) newFlashers.add(a) ;
                    a = upCell(matrix, r, c - 1); if (a != null) newFlashers.add(a) ;
                }
            }
        }
        return newFlashers;
    }

    //return new flasher
    public Pair<Integer, Integer> upCell(List<List<Byte>> matrix, int r, int c){
        if (r >= 0 && r < matrix.size() && c >= 0 && c < matrix.get(r).size() ) {
            matrix.get(r).set(c, (byte) (matrix.get(r).get(c) + 1));
            if (matrix.get(r).get(c) == 10){
                return new Pair<>(r, c) ;
            }
        }
        return null;
    }

    public List<Pair<Integer, Integer>> upLevel(List<List<Byte>> matrix){
        List<Pair<Integer, Integer>> newFlashers = new ArrayList<>();
        for(int r = 0; r < matrix.size();r++) {
            for(int c = 0; c < matrix.get(r).size();c++) {
                Pair<Integer, Integer> a = upCell(matrix, r, c); if (a != null) newFlashers.add(new Pair<>(r, c));
            }
        }

        return newFlashers;
    }


}
