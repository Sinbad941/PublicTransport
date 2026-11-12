import Architecture.*;
import Passager.Passenger;
import Vehicule.Bus;
import Vehicule.Direction;
import Vehicule.Vehicle;
import java.util.ArrayList;

public class LigneImplDirectionTest {
    public static void main(String[] args) {
        testVehicleMovesForwardThenReverses();
        testPassengerBoardsAndDescendsAfterDirectionChange();
        System.out.println("PASS: LigneImplDirectionTest");
    }

    private static void testVehicleMovesForwardThenReverses() {
        LigneImpl ligne = new LigneImpl("L1", "Ligne 1", Vehicule.VehicleType.BUS);
        StationImpl s1 = new StationImpl("s1", "S1", new Coordinate(0, 0), 10, 2);
        StationImpl s2 = new StationImpl("s2", "S2", new Coordinate(1, 0), 10, 2);
        StationImpl s3 = new StationImpl("s3", "S3", new Coordinate(2, 0), 10, 2);

        assertTrue(ligne.addStation(s1), "Could not add s1");
        assertTrue(ligne.addStation(s2), "Could not add s2");
        assertTrue(ligne.addStation(s3), "Could not add s3");

        Bus bus = new Bus("bus1", 20, 40.0, Direction.FORWARD, new ArrayList<>());
        assertTrue(ligne.addVehicle(bus), "Could not add vehicle to line");
        assertTrue(s1.containsVehicle(bus), "Vehicle should start at first station");

        ligne.advanceVehicles();
        assertFalse(s1.containsVehicle(bus), "Vehicle should have departed s1 after first tick");
        assertTrue(s2.containsVehicle(bus), "Vehicle should have arrived at s2 after first tick");
        assertEquals(Direction.FORWARD, bus.direction(), "Bus direction should remain FORWARD after first move");

        ligne.advanceVehicles();
        assertFalse(s2.containsVehicle(bus), "Vehicle should have departed s2 after second tick");
        assertTrue(s3.containsVehicle(bus), "Vehicle should have arrived at s3 after second tick");

        ligne.advanceVehicles();
        Vehicle currentBus = ligne.getVehicles().get(0);
        assertFalse(s3.containsVehicle(currentBus), "Vehicle should have departed s3 after third tick");
        assertTrue(s2.containsVehicle(currentBus), "Vehicle should have returned to s2 after reversing at s3");
        assertEquals(Direction.BACKWARD, currentBus.direction(), "Bus direction should be BACKWARD after reversal at terminus");
    }

    private static void testPassengerBoardsAndDescendsAfterDirectionChange() {
        LigneImpl ligne = new LigneImpl("L2", "Ligne 2", Vehicule.VehicleType.BUS);
        StationImpl s1 = new StationImpl("s1", "S1", new Coordinate(0, 0), 10, 2);
        StationImpl s2 = new StationImpl("s2", "S2", new Coordinate(1, 0), 10, 2);
        StationImpl s3 = new StationImpl("s3", "S3", new Coordinate(2, 0), 10, 2);

        ligne.addStation(s1);
        ligne.addStation(s2);
        ligne.addStation(s3);

        Bus bus = new Bus("bus2", 20, 40.0, Direction.FORWARD, new ArrayList<>());
        ligne.addVehicle(bus);

        Passenger passenger = new Passenger("p1", s2, s3);
        assertTrue(s2.addWaitingPassenger(passenger), "Could not add waiting passenger to s2");

        ligne.advanceVehicles();
        assertTrue(s2.containsVehicle(bus), "Vehicle should be at s2 after first tick");
        assertEquals(0, s2.getWaitingCount(), "Passenger should have boarded at s2");
        assertEquals(Passager.Etat.ABORD, passenger.getEtat(), "Passenger should be aboard after boarding");

        ligne.advanceVehicles();
        assertTrue(s3.containsVehicle(bus), "Vehicle should be at s3 after second tick");
        assertEquals(Passager.Etat.DESCENTE, passenger.getEtat(), "Passenger should have descended at s3");
        assertEquals(0, bus.passengersOnBoard().size(), "Bus should have no passengers on board after disembarkation");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }
}
