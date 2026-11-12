import Architecture.*;
public class CoordinateTest {
    public static void main(String[] args) throws Exception {
        Coordinate c1 = new Coordinate(1.0, 2.0);
        Coordinate c2 = new Coordinate(1.0, 2.0);
        Coordinate c3 = new Coordinate(2.0, 3.0);
        if (c1.x() != 1.0 || c1.y() != 2.0) throw new RuntimeException("coords values");
        if (!c1.equals(c2)) throw new RuntimeException("equals failed");
        if (c1.equals(c3)) throw new RuntimeException("equals false positive");
        System.out.println("PASS: CoordinateTest");
    }
}
