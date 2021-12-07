package ru.mih.day6;

import java.util.ArrayList;
import java.util.List;

public class School {

    public static class Fish{
        int day;

        public Fish(int i) {
            day = i;
        }

        public List<Fish> nextDay(){
               List<Fish> out = new ArrayList<>();
               if (day == 0){
                  out.add(new Fish(6));
                  out.add(new Fish(8));
               }else {
                   out.add(new Fish(day-1));
               }
               return out;
        }

        @Override
        public String toString(){
            return String.valueOf(day);
        }

    }

}
