package Vehicule;

import Passager.Passenger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public record Bus(
    String id,
    int capacity,
    double nominalSpeed,
    Direction direction,
    List<Passenger> passengersOnBoard
) implements Vehicle {
    public Bus {
        Objects.requireNonNull(direction, "direction cannot be null");
        passengersOnBoard = new CopyOnWriteArrayList<>(passengersOnBoard);
    }

    public Bus(String id, int capacity, double nominalSpeed, List<Passenger> passengersOnBoard) {
        this(id, capacity, nominalSpeed, Direction.FORWARD, passengersOnBoard);
    }
    
    @Override
    public VehicleType type() {
        return VehicleType.BUS;
    }

    @Override
    public Direction direction() {
        return direction;
    }

    @Override
    public Vehicle withDirection(Direction direction) {
        return new Bus(id, capacity, nominalSpeed, direction, passengersOnBoard);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bus other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
