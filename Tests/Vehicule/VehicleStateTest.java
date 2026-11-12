import Vehicule.*;
public class VehicleStateTest {
    public static void main(String[] args) throws Exception {
        // Test that all enum values exist
        if (VehicleState.ENDEPLACEMENT == null) throw new RuntimeException("ENDEPLACEMENT should exist");
        if (VehicleState.ENSTATION == null) throw new RuntimeException("ENSTATION should exist");
        if (VehicleState.NONOPERATIONEL == null) throw new RuntimeException("NONOPERATIONEL should exist");

        // Test enum values
        VehicleState[] values = VehicleState.values();
        if (values.length != 3) throw new RuntimeException("Should have exactly 3 VehicleState values, got " + values.length);

        // Test valueOf
        VehicleState enDeplacement = VehicleState.valueOf("ENDEPLACEMENT");
        if (enDeplacement != VehicleState.ENDEPLACEMENT) throw new RuntimeException("valueOf ENDEPLACEMENT failed");

        VehicleState enStation = VehicleState.valueOf("ENSTATION");
        if (enStation != VehicleState.ENSTATION) throw new RuntimeException("valueOf ENSTATION failed");

        VehicleState nonOp = VehicleState.valueOf("NONOPERATIONEL");
        if (nonOp != VehicleState.NONOPERATIONEL) throw new RuntimeException("valueOf NONOPERATIONEL failed");

        // Test toString
        if (!VehicleState.ENDEPLACEMENT.toString().equals("ENDEPLACEMENT")) throw new RuntimeException("toString ENDEPLACEMENT failed");
        if (!VehicleState.ENSTATION.toString().equals("ENSTATION")) throw new RuntimeException("toString ENSTATION failed");
        if (!VehicleState.NONOPERATIONEL.toString().equals("NONOPERATIONEL")) throw new RuntimeException("toString NONOPERATIONEL failed");

        System.out.println("PASS: VehicleStateTest");
    }
}
