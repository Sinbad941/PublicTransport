package Vehicule;

import Passager.Passenger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TransportsEnCommun implements Vehicle {
    private final String id;
    private final int capacity;
    private final double nominalSpeed;
    private final Direction direction;
    private final List<Passenger> passengersOnBoard;
    
    public TransportsEnCommun(String id, int capacity, double nominalSpeed, Direction direction) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.capacity = capacity;
        this.nominalSpeed = nominalSpeed;
        this.direction = Objects.requireNonNull(direction, "direction cannot be null");
        this.passengersOnBoard = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public String id() {
        return id;
    }
    
    @Override
    public int capacity() {
        return capacity;
    }
    
    @Override
    public List<Passenger> passengersOnBoard() {
        return passengersOnBoard;
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
        return new TransportsEnCommun(id, capacity, nominalSpeed, direction);
    }
    
    public boolean boardPassenger(Passenger passenger) {
        Objects.requireNonNull(passenger, "passenger cannot be null");
        if (isFull()) {
            return false;
        }
        return passengersOnBoard.add(passenger);
    }
    
    public boolean disembarkPassenger(Passenger passenger) {
        Objects.requireNonNull(passenger, "passenger cannot be null");
        return passengersOnBoard.remove(passenger);
    }
    
    public boolean isFull() {
        return passengersOnBoard.size() >= capacity;
    }
    
    public boolean canBoard(Passenger passenger) {
        Objects.requireNonNull(passenger, "passenger cannot be null");
        return !isFull();
    }
    
    @Override
    public String toString() {
        return String.format("TransportsEnCommun(id=%s, type=%s, capacity=%d, onboard=%d, rate=%.1f%%)",
                id, type(), capacity, passengersOnBoard.size(), getOccupancyRate() * 100);
    }
}