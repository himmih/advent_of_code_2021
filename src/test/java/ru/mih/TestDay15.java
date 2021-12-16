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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestDay15 {
    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    static class Point{
        public final int r;
        public final int c;
        public final int priority;

        Point(int r, int c, int priority) {
            this.r = r;
            this.c = c;
            this.priority = priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return r == point.r && c == point.c;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, c);
        }

        @Override
        public String toString() {
            return "["+r+","+c+"]";
        }
    }

    public static Comparator<Point> pointComparator = Comparator.comparingInt(p -> p.priority);

    @Test
    public void testEx() throws ExecutionException, InterruptedException {
        List<List<Byte>> matrix = loadMatrix("test1.txt");
//        printMatrix(matrix);
        assertSame(40, getShortestConst(matrix, false));
    }

    @Test
    public void testEx11() throws ExecutionException, InterruptedException {
        List<List<Byte>> matrix = loadMatrix("test11.txt");
//        printMatrix(matrix);
        assertEquals(Optional.ofNullable(315), Optional.ofNullable(getShortestConst(matrix, false)));
    }

    @Test
    public void testExBig() throws ExecutionException, InterruptedException {
        List<List<Byte>> matrix = loadMatrix("test1.txt");
//        printMatrix(matrix);
        assertEquals(315, (int)getShortestConst(matrix, true));
    }

    @Test
    public void testSecondBig() throws ExecutionException, InterruptedException {
        List<List<Byte>> matrix = loadMatrix("test2.txt");
//        printMatrix(matrix);
        assertEquals(2927, (int)getShortestConst(matrix, true));
    }

    @Test
    public void testFirstPart() throws ExecutionException, InterruptedException {
        List<List<Byte>> matrix = loadMatrix("test2.txt");
//        printMatrix(matrix);
        assertEquals(Optional.ofNullable(583), Optional.ofNullable(getShortestConst(matrix, false)));
    }

    @Test
    public void testBigCost(){
        List<List<Byte>> matrixSmall = loadMatrix("test1.txt");

        List<List<Byte>> matrix = loadMatrix("test11.txt");

        assertSame(matrix.get(9).get(11).intValue(), cost(matrixSmall, 9, 11, true));
        assertSame(matrix.get(10).get(10).intValue(), cost(matrixSmall, 10, 10, true));
        assertSame(matrix.get(20).get(23).intValue(), cost(matrixSmall, 20, 23, true));
        assertSame(matrix.get(44).get(35).intValue(), cost(matrixSmall, 44, 35, true));
        assertSame(matrix.get(0).get(34).intValue(), cost(matrixSmall, 0, 34, true));

        for(int r = 0; r<50; r++){
            for(int c = 0; c<50; c++) {
                System.out.println(r + ", " +c);
                assertSame(matrix.get(r).get(c).intValue(), cost(matrixSmall, r, c, true));
            }
        }

    }

    private Integer getShortestConst(List<List<Byte>> matrix, boolean isBig) throws InterruptedException, ExecutionException {
        //A*
        PriorityQueue<Point> frontier = new PriorityQueue<>(matrix.size(), pointComparator);
        Point start = new Point(0, 0, matrix.get(0).get(0));

        Point goal = (isBig)?
                new Point(matrix.size()*5-1, matrix.get(0).size()*5-1,
                        cost(matrix, matrix.size()*5-1, matrix.get(0).size()*5-1, isBig)):
                new Point(matrix.size()-1, matrix.get(0).size()-1, matrix.get(matrix.size()-1).get(matrix.get(0).size()-1));
        frontier.add(start);
        LinkedHashMap<Point, Point> cameFrom = new LinkedHashMap<>();
        LinkedHashMap<Point, Integer> costSoFar = new LinkedHashMap<>();
        costSoFar.put(start, 0);

        int j = 0;

        while (!frontier.isEmpty()){
            Point current = frontier.poll();
            if (current.equals(goal)) break;

            List<Point> neighbors = isBig?neighborsBig(matrix, current):neighbors(matrix, current);

            for(Point next: neighbors){
                int newCost = costSoFar.get(current) + cost(matrix, next.r, next.c, isBig);
                if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)){
                   costSoFar.put(new Point(next.r, next.c, next.priority), newCost);
                   frontier.add(new Point(next.r, next.c, newCost));
                   cameFrom.put(new Point(next.r, next.c, newCost), current);
                }
            }
        }

        Point current = goal;
        List<Point> path = new ArrayList<>();
        while (!current.equals(start)){
           current = cameFrom.get(current);
           path.add(current);
        }
        path.remove(start);
        Collections.reverse(path);
        path.add(goal);

        System.out.println(path);

        return Source.from(path)
           .fold(0, (acc, x) -> acc + cost(matrix, x.r, x.c, isBig))
           .toMat(Sink.head(), Keep.right())
           .run(testSystem)
           .toCompletableFuture()
           .get();
    }

    public static Integer cost(List<List<Byte>> matrix, int r, int c, boolean isBig) {
        if (isBig){
            int maxC = matrix.get(0).size();
            int maxR = matrix.size();

            int sectorR = r / maxR;
            int oldR = r % maxR;
            int sectorC = c / maxC;
            int oldC = c % maxC;

            int oldValue = matrix.get(oldR).get(oldC);

            int rowChange = (oldValue + sectorR);
            rowChange = (rowChange > 9)?rowChange-9:rowChange;
            int colChange = (rowChange + sectorC);
            colChange = (colChange > 9)?colChange-9:colChange;

            return  colChange;
        }else {
           return matrix.get(r).get(c).intValue();
        }
    }

    private List<Point> neighbors(List<List<Byte>> matrix, Point current) {
        List<Point> out = new ArrayList<>();
        int maxC = matrix.get(0).size();
        if (current.c > 0)  out.add(new Point(current.r, current.c-1, cost(matrix, current.r, current.c-1, false)));
        if (current.c < maxC-1) out.add(new Point(current.r, current.c+1, cost(matrix, current.r, current.c+1, false)));
        if (current.r > 0) out.add(new Point(current.r-1, current.c, cost(matrix, current.r-1, current.c, false)));
        if (current.r < matrix.size()-1) out.add(new Point(current.r+1, current.c, cost(matrix, current.r+1, current.c, false)));
        return out;
    }

    private List<Point> neighborsBig(List<List<Byte>> matrix, Point current) {
        List<Point> out = new ArrayList<>();
        int maxC = matrix.get(0).size()*5;
        int maxR = matrix.size()*5;
        if (current.c > 0)  out.add(new Point(current.r, current.c-1, cost(matrix, current.r, current.c-1, true)));
        if (current.c < maxC-1) out.add(new Point(current.r, current.c+1, cost(matrix, current.r, current.c+1, true)));
        if (current.r > 0) out.add(new Point(current.r-1, current.c, cost(matrix, current.r-1, current.c, true)));
        if (current.r < maxR-1) out.add(new Point(current.r+1, current.c, cost(matrix, current.r+1, current.c, true)));
        return out;
    }

    private List<List<Byte>> loadMatrix(String fileName){
        try {
            List<List<Byte>> matrix = new ArrayList<>();

            final Source<String, CompletionStage<IOResult>> inSource =
                    FileIO.fromPath(
                                    Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day15/" + fileName))
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
}
