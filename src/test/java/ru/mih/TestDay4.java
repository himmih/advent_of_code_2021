package ru.mih;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import org.junit.AfterClass;
import org.junit.Test;
import ru.mih.day4.Bingo;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

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
}
