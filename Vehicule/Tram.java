package Vehicule;

import Passager.Passenger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public record Tram(
    String id,
    int capacity,
    double nominalSpeed,
    Direction direction,
    List<Passenger> passengersOnBoard
) implements Vehicle {
    public Tram {
        Objects.requireNonNull(direction, "direction cannot be null");
        passengersOnBoard = new CopyOnWriteArrayList<>(passengersOnBoard);
    }
    
    @Override
    public VehicleType type() {
        return VehicleType.TRAMWAY;
    }

    @Override
    public Direction direction() {
        return direction;
    }

    @Override
    public Vehicle withDirection(Direction direction) {
        return new Tram(id, capacity, nominalSpeed, direction, passengersOnBoard);
    }
}
