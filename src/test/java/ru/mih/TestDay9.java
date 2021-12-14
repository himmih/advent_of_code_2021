package ru.mih;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.*;
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

    public static class Coordinate {
        public final int r;
        public final int c;

        public Coordinate(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinate that = (Coordinate) o;
            return r == that.r && c == that.c;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, c);
        }

        @Override
        public String toString() {
            return r + "," + c;
        }
    }

    public class Basin{
        public final Set<Coordinate> coordinates;

        public Basin(Set<Coordinate> coordinates) {
            this.coordinates = coordinates;
        }

        public Basin(Coordinate c) {
            Set<Coordinate> coordinates = new HashSet<>();
            coordinates.add(c);
            this.coordinates = coordinates;
        }

        public void add(Coordinate c){
            this.coordinates.add(c);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Basin basin = (Basin) o;
            for(Coordinate c: basin.coordinates){
                if (!this.coordinates.contains(c)){
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(coordinates);
        }

        @Override
        public String toString() {
            return String.valueOf(coordinates);
        }

        public boolean contains(Coordinate a) {
            return this.coordinates.contains(a);
        }

        public void addAll(Basin b) {
            this.coordinates.addAll(b.coordinates);
        }
    }

    @Test
    public void testSecondOne() throws ExecutionException, InterruptedException {

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
        int step = 0;

        Set<Basin> basins = new HashSet<>();
        Set<Basin> basinsRowBefore = new HashSet<>();
        Set<Basin> basinsCurrentRow = new HashSet<>();
        final Byte NINE = Byte.valueOf((byte)9);

        for(int r = 0; r < matix.size(); r++){
            basinsCurrentRow = new HashSet<>();
            for(int c = 0; c < maxC; c++){
                Byte currentValue = matix.get(r).get(c);
                Coordinate currentPosition = new Coordinate(r, c);

                if (NINE.equals(currentValue)) continue;
                boolean added = false;
                Basin leftSet = null;
                if (c > 0) { //adding to left set
                    Coordinate leftPosition = new Coordinate(r, c - 1);
                    Byte leftValue = matix.get(r).get(c - 1);
                    if (!NINE.equals(leftValue)){
                        for(Basin next :  basinsCurrentRow){
                            if (next.contains(leftPosition)){
                                next.add(currentPosition);
                                leftSet = next;
                                added = true;
                                break;
                            }
                        }
                    }
                }
//System.out.println("leftSet = " + leftSet);
//System.out.println("basinsCurrentRow = " + basinsCurrentRow);
                if (r > 0) { //adding to up set
                    Byte upValue = matix.get(r-1).get(c);
                    Coordinate upPosition = new Coordinate(r-1, c);
                    if (!NINE.equals(upValue)) {
                        for (Basin upSet : basinsRowBefore) {
                            if (upSet.contains(upPosition)) {
                                upSet.add(currentPosition);
                                basinsCurrentRow.add(upSet);
                                if (leftSet != null){
                                    upSet.addAll(leftSet);
                                }
                                added = true;
                                break;
                            }
                        }
                    }
                }
                if (!added) { //create new set
//                  System.out.print(current);
                    basinsCurrentRow.add(new Basin(currentPosition));
                }
            } //columns
            basins.addAll(basinsCurrentRow);
            basinsRowBefore = basinsCurrentRow;
        }
        basins.addAll(basinsRowBefore);
        List<Basin> orderedBasis = basins.stream().sorted(Comparator.comparingInt(b -> -b.coordinates.size())).collect(Collectors.toList());

        List<Basin> newBases = new ArrayList<>();

        for(Basin b: orderedBasis){
            if (newBases.size() == 0) {
                newBases.add(b);
            }else{
                boolean found = false;
                for(Basin next : newBases){
                    Set<Coordinate> intersects = new HashSet<>(next.coordinates);
                    intersects.retainAll(b.coordinates);
                    if (intersects.size() > 0 ) {
                        next.coordinates.addAll(b.coordinates);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    newBases.add(b);
                }
            }
        }

        newBases =
                newBases.stream()
                        .sorted(Comparator.comparingInt(b -> -b.coordinates.size())).collect(Collectors.toList());

        System.out.println( newBases.get(0).coordinates.size());
        System.out.println( newBases.get(1).coordinates.size());
        System.out.println( newBases.get(2).coordinates.size());

        System.out.println(
                newBases.get(0).coordinates.size()
                        *newBases.get(1).coordinates.size()
                        *newBases.get(2).coordinates.size()
        );


    }
}
