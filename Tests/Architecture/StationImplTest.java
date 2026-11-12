import Architecture.*;
import Passager.Passenger;
import Vehicule.Bus;
import java.time.Duration;
import java.util.ArrayList;

public class StationImplTest {

    public static void main(String[] args) {
        testAddAndRemovePassenger();
        testCapacityLimit();
        testVehicleStatus();
        testAverageWaitingTimeNonNegative();
        System.out.println("All StationImpl tests passed.");
    }

    private static void testAddAndRemovePassenger() {
        StationImpl station = new StationImpl("S1", "Station 1", new Coordinate(0, 0), 5, 2);
        Passenger p1 = new Passenger("P1", station, station);

        boolean added = station.addWaitingPassenger(p1);
        int waitingAfterAdd = station.getWaitingCount();
        System.out.println("Add passenger: " + added + ", waitingCount=" + waitingAfterAdd);
        assertTrue(added, "Passenger should be added");
        assertEquals(1, waitingAfterAdd, "Waiting count should be 1 after add");

        boolean removed = station.removeWaitingPassenger(p1);
        int waitingAfterRemove = station.getWaitingCount();
        System.out.println("Remove passenger: " + removed + ", waitingCount=" + waitingAfterRemove);
        assertTrue(removed, "Passenger should be removed");
        assertEquals(0, waitingAfterRemove, "Waiting count should be 0 after remove");
    }

    private static void testCapacityLimit() {
        StationImpl station = new StationImpl("S2", "Station 2", new Coordinate(1, 1), 1, 1);
        Passenger p1 = new Passenger("P1", station, station);
        Passenger p2 = new Passenger("P2", station, station);

        boolean firstAdded = station.addWaitingPassenger(p1);
        boolean secondAdded = station.addWaitingPassenger(p2);
        int waitingCount = station.getWaitingCount();
        System.out.println("Capacity test: firstAdded=" + firstAdded + ", secondAdded=" + secondAdded + ", waitingCount=" + waitingCount);
        assertTrue(firstAdded, "First passenger should be accepted");
        assertFalse(secondAdded, "Second passenger should be rejected when full");
        assertEquals(1, waitingCount, "Waiting count should remain at capacity");
    }

    private static void testVehicleStatus() {
        StationImpl station = new StationImpl("S3", "Station 3", new Coordinate(2, 2), 5, 1);
        Bus vehicle = new Bus("B1", 10, 30.0, new ArrayList<>());

        station.updateVehicleStatus(vehicle, true);
        boolean afterArrival = station.canAccommodateVehicle();
        System.out.println("Vehicle arrival: canAccommodateVehicle=" + afterArrival);
        assertFalse(afterArrival, "Station should not accommodate another vehicle when full");

        station.updateVehicleStatus(vehicle, false);
        boolean afterDeparture = station.canAccommodateVehicle();
        System.out.println("Vehicle departure: canAccommodateVehicle=" + afterDeparture);
        assertTrue(afterDeparture, "Station should accommodate a vehicle after departure");
    }

    private static void testAverageWaitingTimeNonNegative() {
        StationImpl station = new StationImpl("S4", "Station 4", new Coordinate(3, 3), 5, 2);
        Passenger p1 = new Passenger("P1", station, station);

        station.addWaitingPassenger(p1);
        Duration average = station.getAverageWaitingTime();
        System.out.println("Average waiting time: " + average.toMillis() + " ms");

        assertTrue(average != null, "Average waiting time should not be null");
        assertTrue(!average.isNegative(), "Average waiting time should be non-negative");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }
}
