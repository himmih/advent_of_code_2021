package ru.mih.day8;

import javax.swing.text.Segment;
import java.util.*;

public class Clock {

    public static class Row{

        public static int solve(List<String> inSignals, List<String> outSegments ){
            Signal[] signals = new Signal[10];

            Signal intersection2_3_5 = null;
            Signal intersection0_6_9 = null;

            for(String signal: inSignals){
                if (signal.length() == 6) {
                    if (intersection0_6_9 == null) {
                        intersection0_6_9 = new Signal(signal);
                    }else {
                        intersection0_6_9 = intersection0_6_9.intersection(new Signal(signal));
                    }
                }else if (signal.length() == 5) {
                    if (intersection2_3_5 == null) {
                        intersection2_3_5 = new Signal(signal);
                    }else {
                        intersection2_3_5 = intersection2_3_5.intersection(new Signal(signal));
                    }
                }else if (signal.length() == 2) {
                    signals[1] = new Signal(signal);
                }else if (signal.length() == 4) {
                    signals[4] = new Signal(signal);
                }else if (signal.length() == 3) {
                    signals[7] = new Signal(signal);
                }else if (signal.length() == 7) {
                    signals[8] = new Signal(signal);
                }
            }

            Signal s7 = intersection2_3_5.intersection(signals[4]);
            Signal s6 = intersection2_3_5.intersection(signals[7]);
            Signal s2 = intersection0_6_9.intersection(signals[1]);
            Signal s1 = signals[1].minus(s2);
            Signal s5 = signals[4].minus(s1).minus(s2).minus(s7);
            Signal s3 = intersection0_6_9.intersection(signals[8]).minus(s6);
            Signal s4 = signals[8].minus(s1).minus(s2).minus(s3).minus(s5).minus(s6).minus(s7);

            signals[0] = s1.union(s2).union(s3).union(s4).union(s5).union(s6);
            signals[2] = s6.union(s1).union(s7).union(s4).union(s3);
            signals[3] = s6.union(s1).union(s7).union(s2).union(s3);
            signals[5] = s6.union(s5).union(s7).union(s2).union(s3);
            signals[6] = s6.union(s5).union(s7).union(s2).union(s3).union(s4);
            signals[9] = s3.union(s2).union(s7).union(s5).union(s6).union(s1);

//            String wrongSegment: outSegments
            int out = 0;
            for(int j = 0; j < 4; j++){
                Signal wSignal = new Signal(outSegments.get(j));
                for(int i = 0; i< 10; i++){
                    if (wSignal.equals(signals[i])){
                        System.out.print(i);
                       out += i*Math.pow(10, 3-j);
                    }
                }
            }
            System.out.println();

            return out;


        }


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
