package ru.mih;

import akka.Done;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import org.junit.AfterClass;
import org.junit.Test;
import ru.mih.day4.Bingo;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestDay4 {
    public static final ActorSystem testSystem = ActorSystem.create("TestSystem");

    @AfterClass
    public static void terminate(){
        testSystem.terminate();
    }


    @Test
    public void testBoard() {

        Bingo.Board board = new Bingo.Board();
        board.addRow("1 2 3 4 5");

        assertEquals("12345", board.getRow(0).stream().map(x -> String.valueOf(x)).reduce((a, b) -> a + b).get());
        assertEquals("1", board.getColumn(0).stream().map(x -> String.valueOf(x)).collect(Collectors.joining()));

        board.addRow("2 3 4 5 6");


        assertEquals("23456", board.getRow(1).stream().map(x -> String.valueOf(x)).reduce((a, b) -> a + b).get());
        assertEquals("12", board.getColumn(0).stream().map(x -> String.valueOf(x)).collect(Collectors.joining()));
        assertEquals("56", board.getColumn(4).stream().map(x -> String.valueOf(x)).collect(Collectors.joining()));

    }

    @Test
    public void testClone(){

        Bingo.Board board = new Bingo.Board();
        board.addRow("1 2 3 4 5");
        board.addRow("2 3 4 5 6");

        board = board.copy();


        assertEquals("23456", board.getRow(1).stream().map(x -> String.valueOf(x)).reduce((a, b) -> a + b).get());
        assertEquals("12", board.getColumn(0).stream().map(x -> String.valueOf(x)).collect(Collectors.joining()));
        assertEquals("56", board.getColumn(4).stream().map(x -> String.valueOf(x)).collect(Collectors.joining()));

    }

    @Test
    public void testMark(){

        Bingo.Board board = new Bingo.Board();
        board.addRow("1 2 3 4 5");
        board.addRow("2 3 4 5 6");
        board.addRow("12 7 7 7 7");

        board = board.mark(1);
        board = board.mark(2);
        board = board.mark(6);


//        assertEquals(true, board.getRow(0).get(0).marked); //1
        assertEquals(true, board.getRow(0).get(1).marked); //2
        assertEquals(true, board.getRow(1).get(0).marked); //2
        assertEquals(true, board.getRow(1).get(4).marked); //6
        assertEquals(false, board.getRow(1).get(3).marked); //5
        assertEquals(false, board.getRow(0).get(2).marked); //3

        assertEquals(false, board.getWin().isPresent());
        assertEquals(false, board.getWin().isPresent());

        Bingo.Board boardMarkedRow = board.mark(12);

        assertEquals(new Pair("c", 0), boardMarkedRow.getWin().get());

        Bingo.Board boardMarkedColumn = new Bingo.Board();
        boardMarkedColumn.addRow("1 2 3 4 5");
        boardMarkedColumn.addRow("2 3 4 5 6");
        boardMarkedColumn.addRow("12 7 7 7 7");
        boardMarkedColumn.addRow("2 3 4 5 6");
        boardMarkedColumn = boardMarkedColumn.mark(20);
        boardMarkedColumn = boardMarkedColumn.mark(2);
        boardMarkedColumn = boardMarkedColumn.mark(3);
        boardMarkedColumn = boardMarkedColumn.mark(4);
        boardMarkedColumn = boardMarkedColumn.mark(5);
        boardMarkedColumn = boardMarkedColumn.mark(6);

        assertEquals(new Pair("r", 1), boardMarkedColumn.getWin().get());
    }

    @Test
    public void testGetWinSumUnmarkedNumber(){

        Bingo.Board board = new Bingo.Board();
        board.addRow("1 2 ");
        board.addRow("2 3 ");

        board = board.mark(1);
        board = board.mark(2);

        assertEquals(true, board.getWin().isPresent());
        assertSame(3, board.getWinSumUnmarkedNumber());

        board = board.mark(3);
        assertSame(0, board.getWinSumUnmarkedNumber());

        board = new Bingo.Board();
        board.addRow("1 2 4");
        board.addRow("2 3 7");

        board = board.mark(4);
        board = board.mark(7);

        assertSame(8, board.getWinSumUnmarkedNumber());
    }

    @Test
    public void testResultExamplePartOne() throws ExecutionException, InterruptedException {

        final Source<Integer, CompletionStage<IOResult>> headerSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day4/test1_header.txt"))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String())) ;

        List<Integer> numbers = headerSource.toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();

        final Source<Bingo.Board, CompletionStage<IOResult>> boardsSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day4/test1.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .map(bs -> bs.utf8String())
                .map(line -> line.trim())
                .filterNot(String::isEmpty)
                .grouped(5)
                .map(lines -> {
                    Bingo.Board board = new Bingo.Board();
                    lines.forEach(line -> board.addRow(line));
                    return board;
                });

        List<Bingo.Board> boardList = boardsSource.toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();


        for(Integer number: numbers){
            boardList = boardList.stream().map(board -> board.mark(number)).collect(Collectors.toList());
            Optional<Bingo.Board> winBoard = boardList.stream().filter(b -> b.getWin().isPresent()).findFirst();
            if (winBoard.isPresent()){
                System.out.println(winBoard.get().getWinSumUnmarkedNumber()*number);
                break;
            }
        }

    }

    @Test
    public void testResultPartOne() throws ExecutionException, InterruptedException {

        final Source<Integer, CompletionStage<IOResult>> headerSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day4/test2_header.txt"))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String())) ;

//        headerSource.toMat(Sink.foreach(System.out::println), Keep.right()).run(testSystem);
        List<Integer> numbers = headerSource.toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();


        final Source<Bingo.Board, CompletionStage<IOResult>> boardsSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day4/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .map(bs -> bs.utf8String())
                .map(line -> line.trim())
                .filterNot(String::isEmpty)
                .grouped(5)
                .map(lines -> {
                    Bingo.Board board = new Bingo.Board();
                    lines.forEach(line -> board.addRow(line));
                    return board;
                });

//        boardsSource.toMat(Sink.foreach(System.out::println), Keep.right()).run(testSystem);
        List<Bingo.Board> boardList = boardsSource.toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();


        for(Integer number: numbers){
            boardList = boardList.stream().map(board -> board.mark(number)).collect(Collectors.toList());
            Optional<Bingo.Board> winBoard = boardList.stream().filter(b -> b.getWin().isPresent()).findFirst();
            if (winBoard.isPresent()){
                System.out.println(winBoard.get().getWinSumUnmarkedNumber()*number);
                break;
            }
        }


    }

    @Test
    public void testResultExamplePartTwo() throws ExecutionException, InterruptedException {

        final Source<Integer, CompletionStage<IOResult>> headerSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day4/test1_header.txt"))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String())) ;

        List<Integer> numbers = headerSource.toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();

        final Source<Bingo.Board, CompletionStage<IOResult>> boardsSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day4/test1.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .map(bs -> bs.utf8String())
                .map(line -> line.trim())
                .filterNot(String::isEmpty)
                .grouped(5)
                .map(lines -> {
                    Bingo.Board board = new Bingo.Board();
                    lines.forEach(line -> board.addRow(line));
                    return board;
                });

        List<Bingo.Board> boardList = boardsSource.toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();


//        for(Integer number: numbers){
//            boardList = boardList.stream().map(board -> board.mark(number)).collect(Collectors.toList());
//            Optional<Bingo.Board> winBoard = boardList.stream().filter(b -> b.getWin().isPresent()).findFirst();
//            if (winBoard.isPresent()){
//                System.out.println(winBoard.get().getWinSumUnmarkedNumber()*number);
//                boardList.remove(winBoard.get());
//            }
//        }

        for(Integer number: numbers){
            boardList = boardList.stream().map(board -> board.mark(number)).collect(Collectors.toList());
            List<Bingo.Board> winBoards = boardList.stream().filter(b -> b.getWin().isPresent()).collect(Collectors.toList());
            if (winBoards.size()>0){
                for(Bingo.Board winBoard: winBoards) {
                    System.out.println(winBoard.getWinSumUnmarkedNumber() + "*" + number
                            + " = " + winBoard.getWinSumUnmarkedNumber() * number);
                    boardList.remove(winBoard);
                    System.out.println("left bords: " + boardList.size());
                }
            }
        }

    }

    @Test
    public void testResultPartTwo() throws ExecutionException, InterruptedException {

        final Source<Integer, CompletionStage<IOResult>> headerSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day4/test2_header.txt"))
                .via(Framing.delimiter(ByteString.fromString(","), 256, FramingTruncation.ALLOW))
                .filterNot(ByteString::isEmpty)
                .map(bs -> Integer.parseInt(bs.utf8String())) ;

//        headerSource.toMat(Sink.foreach(System.out::println), Keep.right()).run(testSystem);
        List<Integer> numbers = headerSource.toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();


        final Source<Bingo.Board, CompletionStage<IOResult>> boardsSource
                = FileIO.fromPath(
                        Paths.get("/home/mih/projects/advent_of_code_2021/src/main/resources/day4/test2.txt"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                .map(bs -> bs.utf8String())
                .map(line -> line.trim())
                .filterNot(String::isEmpty)
                .grouped(5)
                .map(lines -> {
                    Bingo.Board board = new Bingo.Board();
                    lines.forEach(line -> board.addRow(line));
                    return board;
                });

//        boardsSource.toMat(Sink.foreach(System.out::println), Keep.right()).run(testSystem);
        List<Bingo.Board> boardList = boardsSource.toMat(Sink.seq(), Keep.right()).run(testSystem).toCompletableFuture().get();


        for(Integer number: numbers){
            boardList = boardList.stream().map(board -> board.mark(number)).collect(Collectors.toList());
            List<Bingo.Board> winBoards = boardList.stream().filter(b -> b.getWin().isPresent()).collect(Collectors.toList());
            if (winBoards.size()>0){
                if (winBoards.size() > 1) {
                    System.out.println("win boards: " + winBoards);
//                    throw new IllegalStateException("found more: " + winBoards.size());
                }
                for(Bingo.Board winBoard: winBoards) {
                    System.out.println(winBoard.getWinSumUnmarkedNumber() + "*" + number
                            + " = " + winBoard.getWinSumUnmarkedNumber() * number);
                    boardList.remove(winBoard);
                }
                System.out.println("left boards: " + boardList.size());
            }
        }

//        for(Integer number: numbers){
//            boardList = boardList.stream().map(board -> board.mark(number)).collect(Collectors.toList());
//            Optional<Bingo.Board> winBoard = boardList.stream().filter(b -> b.getWin().isPresent()).findFirst();
//            if (winBoard.isPresent()){
//                System.out.println(winBoard.get().getWinSumUnmarkedNumber() + "*" + number
//                        + " = " + winBoard.get().getWinSumUnmarkedNumber() * number);
//                boardList.remove(winBoard.get());
//                System.out.println("left bords: " + boardList.size());
//            }
//        }


    }
}
