import Vehicule.*;
public class VehicleTypeTest {
    public static void main(String[] args) throws Exception {
        // Test that all VehicleType values exist
        VehicleType[] values = VehicleType.values();
        if (values.length == 0) throw new RuntimeException("VehicleType should have at least one value");

        // Test valueOf for each type
        for (VehicleType v : values) {
            VehicleType valueOf = VehicleType.valueOf(v.name());
            if (valueOf != v) throw new RuntimeException("valueOf " + v.name() + " failed");
        }

        // Test toString
        for (VehicleType v : values) {
            String str = v.toString();
            if (str == null || str.isEmpty()) throw new RuntimeException("toString returned empty for " + v.name());
        }

        System.out.println("PASS: VehicleTypeTest");
    }
}
