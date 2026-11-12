import Vehicule.*;
public class DirectionTest {
    public static void main(String[] args) throws Exception {
        // Test that all Direction values exist
        Direction[] values = Direction.values();
        if (values.length == 0) throw new RuntimeException("Direction should have at least one value");

        // Test valueOf for each direction
        for (Direction d : values) {
            Direction valueOf = Direction.valueOf(d.name());
            if (valueOf != d) throw new RuntimeException("valueOf " + d.name() + " failed");
        }

        // Test toString
        for (Direction d : values) {
            String str = d.toString();
            if (str == null || str.isEmpty()) throw new RuntimeException("toString returned empty for " + d.name());
        }

        System.out.println("PASS: DirectionTest");
    }
}
