package ru.mih.day4;

import akka.japi.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
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
    }

    public static class Board implements Cloneable{
       private final List<List<NumberOnBoard>> rc;

        public Board(List<List<NumberOnBoard>> rows) {
           rc = rows;
        }

        public Board() { //crate empty board
            rc = new ArrayList<>();
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




    }

}
