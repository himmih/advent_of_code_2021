package ru.mih.day8;

import javax.swing.text.Segment;
import java.util.*;

public class Clock {

    public static class Row{



    }



    public static class Signal{

        public final Set<Character> words;

        public Signal(String signal){
            words = new HashSet<>();
            for(char ch: signal.toCharArray()){
                words.add(Character.valueOf(ch));
            }
        }

        public Signal(Set<Character> characters) {
            words = new HashSet<>();
            for(Character ch: characters){
                words.add(Character.valueOf(ch.charValue()));
            }
        }

        public int size() {
            return words.size();
        }

        @Override
        public String toString() {
            return String.valueOf(words);
        }

        public Signal union(Signal a) {
            Signal result = new Signal(this.words);
            result.words.addAll(a.words);
            return result;
        }

        public Signal minus(Signal a) {
            Signal result = new Signal(this.words);
            result.words.removeAll(a.words);
            return result;
        }

        public Signal intersection(Signal a) {
            Signal result = new Signal(this.words);
            result.words.retainAll(a.words);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Signal signal = (Signal) o;
            return words.equals(signal.words);
        }

        @Override
        public int hashCode() {
            return Objects.hash(words);
        }
    }


}
