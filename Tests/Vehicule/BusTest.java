import Vehicule.*;
import java.util.ArrayList;
import java.util.List;

import Passager.Passenger;

public class BusTest {
    public static void main(String[] args) throws Exception {
        // Test Bus creation with minimal passengers list
        List<Passenger> passengers = new ArrayList<>();
        Bus bus = new Bus("BUS1", 50, 40.0, Direction.FORWARD, passengers);

        // Test id
        if (!"BUS1".equals(bus.id())) throw new RuntimeException("id mismatch");

        // Test capacity
        if (bus.capacity() != 50) throw new RuntimeException("capacity mismatch");

        // Test direction
        if (bus.direction() != Direction.FORWARD) throw new RuntimeException("direction mismatch");

        // Test type
        if (bus.type() != VehicleType.BUS) throw new RuntimeException("type should be BUS");

        // Test initial passengers
        if (!bus.passengersOnBoard().isEmpty()) throw new RuntimeException("bus should start empty");

        // Test withDirection returns a Vehicle
        Vehicle busBackward = bus.withDirection(Direction.BACKWARD);
        if (busBackward.direction() != Direction.BACKWARD) throw new RuntimeException("withDirection failed");

        // Test occupancy rate
        if (bus.getOccupancyRate() != 0.0) throw new RuntimeException("empty bus should have 0% occupancy");

        System.out.println("PASS: BusTest");
    }
}
