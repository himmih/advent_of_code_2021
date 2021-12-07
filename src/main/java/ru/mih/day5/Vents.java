package ru.mih.day5;

import akka.japi.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Vents {

    public static class Point{
        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "[" + x + "," + y + "]";
        }
    }

    public static class Vent{
        public final Set<Point> points;

        public Vent(int x1, int y1, int x2, int y2) {

            LinkedHashSet<Point> tPoints = new LinkedHashSet<>();

            if (x1 == x2 || y1 == y2){
                int x11, x22;
                if ((x1<x2)){ x11 = x1; x22 = x2; }
                else { x11 = x2; x22 = x1; }

                int y11, y22;
                if ((y1<y2)){ y11 = y1; y22 = y2; }
                else { y11 = y2; y22 = y1; }

                for(int x = x11; x <= x22; x++){
                    for(int y = y11; y <= y22; y++ ){
                       tPoints.add(new Point(x, y));
                    }
                }
            }else {
                int xstep = (x1<x2)?1:-1;
                int ystep = (y1<y2)?1:-1;
                int x = x1; int y = y1;
                int j = 0;
                do{
                   tPoints.add(new Point(x, y));
//                   System.out.println(new Point(x,y));
//                    if (++j == 10) throw new IllegalStateException("Exit");
                   x += xstep;
                   y += ystep;
                }while ( (x <= x2 && xstep == 1) || (x >= x2 && xstep == -1));
            }

            points = Collections.unmodifiableSet(tPoints);
        }

        public Vent(String[] xy1, String[] xy2) {
            this(Integer.parseInt(xy1[0]), Integer.parseInt(xy1[1]), Integer.parseInt(xy2[0]), Integer.parseInt(xy2[1]));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vent vent = (Vent) o;
            return points.equals(vent.points);
        }

        @Override
        public int hashCode() {
            return Objects.hash(points);
        }

        @Override
        public String toString() {
            return points.stream().map(p -> p.toString()).collect(Collectors.joining());
        }

        public Set<Point> intersects(Vent v2) {
            return points.stream()
                    .filter(v2.points::contains)
//                    .map(p -> new Point(p.x, p.y))
                    .collect(Collectors.toSet());
        }

        public List<Point> intersects(Collection<Vent> vents) {
            return vents.stream()
                    .flatMap(v -> intersects(v).stream())
                    .collect(Collectors.toList());
        }


        public boolean isHorizontalOrVertical(){
           return points.size() > 0;
        }
    }

}
