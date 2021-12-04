package ru.mih.day4;

import akka.japi.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Bingo {

    public static class NumberOnBoard implements Cloneable{
       public final int number;
       public final boolean marked;

        public NumberOnBoard(int number, boolean marked) {
            this.number = number;
            this.marked = marked;
        }
        public NumberOnBoard(NumberOnBoard old) {
            this.number = old.number;
            this.marked = old.marked;
        }

        protected NumberOnBoard copy(){
            return new NumberOnBoard(number, marked);
        }

        @Override
        public String toString() {
            return String.valueOf(number);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NumberOnBoard that = (NumberOnBoard) o;
            return number == that.number;
        }

        @Override
        public int hashCode() {
            return Objects.hash(number);
        }
    }

    public static class Board implements Cloneable{
       private final List<List<NumberOnBoard>> rc;

        public Board(List<List<NumberOnBoard>> rows) {
           rc = rows;
        }

        public Board() { //crate empty board
            rc = new ArrayList<>();
        }

        @Override
        public String toString() {
            return rc.stream().map(row -> String.valueOf(row)).collect(Collectors.joining());
        }

        public void addRow(String row){
           List<NumberOnBoard> newRow = new ArrayList<>();
           StringTokenizer tokenizer = new StringTokenizer(row);
           while(tokenizer.hasMoreTokens()){
              newRow.add(new NumberOnBoard(Integer.parseInt(tokenizer.nextToken()), false));
           }
           if (rc.size() > 1){
               if (rc.get(rc.size()-1).size() != rc.get(rc.size()-2).size()){
                   throw new IllegalStateException("Wrong numbers in row: " + (rc.size()-1));
               }
           }
           rc.add(newRow);
       }

        public Board copy(){
           List<List<NumberOnBoard>> rows = new ArrayList<>();
           for(List<NumberOnBoard> row: rc){
               rows.add(row.stream().map(n -> n.copy()).collect(Collectors.toList()));
           }
           return new Board(rows);
        }

        public Board mark(int i){
            List<List<NumberOnBoard>> rows = new ArrayList<>();
            for(List<NumberOnBoard> row: rc){
                rows.add(row.stream().map(n -> (n.number == i)?
                        new NumberOnBoard(n.number, true):
                        new NumberOnBoard(n.number, n.marked)).collect(Collectors.toList()));
            }
            return new Board(rows);
        }

        public List<NumberOnBoard> getRow(int i) {
            return rc.get(i);
        }

        public List<NumberOnBoard> getColumn(int i) {
            return rc.stream().map(row -> row.get(i)).collect(Collectors.toList());
        }


        public Optional<Pair<String, Integer>> getWin(){

            for(int i = 0; i < rc.size(); i++){
                Optional<NumberOnBoard> noMarked = getRow(i).stream().filter(x -> !x.marked).findAny();
                if (noMarked.isEmpty()) return Optional.of(new Pair<>("r", i));
            }

            int columnSize = rc.get(0).size(); //getting columns size by first row

            for(int i = 0; i < columnSize; i++){
                Optional<NumberOnBoard> noMarked = getColumn(i).stream().filter(x -> !x.marked).findAny();
                if (noMarked.isEmpty()) return Optional.of(new Pair<>("c", i));
            }

            return Optional.empty();
        }

        public Integer getWinSumUnmarkedNumber(){
           Optional<Integer> o = rc.stream().map(r -> r.stream().filter(a -> !a.marked).map(a -> a.number).reduce((a, b)->a+b))
                   .filter(Optional::isPresent).map(x -> x.get())
                   .reduce((x, y) -> x + y);
           return o.isPresent()?o.get():0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Board board = (Board) o;
            return rc.equals(board.rc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rc);
        }
    }

}
