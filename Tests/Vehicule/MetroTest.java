import Vehicule.*;
import java.util.ArrayList;
import java.util.List;

import Passager.Passenger;

public class MetroTest {
    public static void main(String[] args) throws Exception {
        // Test Metro creation with minimal passengers list
        List<Passenger> passengers = new ArrayList<>();
        Metro metro = new Metro("METRO1", 80, 60.0, Direction.FORWARD, passengers);

        // Test id
        if (!"METRO1".equals(metro.id())) throw new RuntimeException("id mismatch");

        // Test capacity
        if (metro.capacity() != 80) throw new RuntimeException("capacity mismatch");

        // Test direction
        if (metro.direction() != Direction.FORWARD) throw new RuntimeException("direction mismatch");

        // Test type
        if (metro.type() != VehicleType.METRO) throw new RuntimeException("type should be METRO");

        // Test initial passengers
        if (!metro.passengersOnBoard().isEmpty()) throw new RuntimeException("metro should start empty");

        // Test withDirection returns a Vehicle
        Vehicle metroBackward = metro.withDirection(Direction.BACKWARD);
        if (metroBackward.direction() != Direction.BACKWARD) throw new RuntimeException("withDirection failed");

        // Test occupancy rate
        if (metro.getOccupancyRate() != 0.0) throw new RuntimeException("empty metro should have 0% occupancy");

        System.out.println("PASS: MetroTest");
    }
}
