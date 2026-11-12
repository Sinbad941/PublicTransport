import Vehicule.*;
import java.util.ArrayList;
import java.util.List;

import Passager.Passenger;

public class TramTest {
    public static void main(String[] args) throws Exception {
        // Test Tram creation with minimal passengers list
        List<Passenger> passengers = new ArrayList<>();
        Tram tram = new Tram("TRAM1", 60, 45.0, Direction.FORWARD, passengers);

        // Test id
        if (!"TRAM1".equals(tram.id())) throw new RuntimeException("id mismatch");

        // Test capacity
        if (tram.capacity() != 60) throw new RuntimeException("capacity mismatch");

        // Test direction
        if (tram.direction() != Direction.FORWARD) throw new RuntimeException("direction mismatch");

        // Test type
        if (tram.type() != VehicleType.TRAMWAY) throw new RuntimeException("type should be TRAMWAY");

        // Test initial passengers
        if (!tram.passengersOnBoard().isEmpty()) throw new RuntimeException("tram should start empty");

        // Test withDirection returns a Vehicle
        Vehicle tramBackward = tram.withDirection(Direction.BACKWARD);
        if (tramBackward.direction() != Direction.BACKWARD) throw new RuntimeException("withDirection failed");

        // Test occupancy rate
        if (tram.getOccupancyRate() != 0.0) throw new RuntimeException("empty tram should have 0% occupancy");

        System.out.println("PASS: TramTest");
    }
}
