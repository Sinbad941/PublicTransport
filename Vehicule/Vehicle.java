package Vehicule;

import Passager.Passenger;
import java.util.List;

public sealed interface Vehicle permits TransportsEnCommun, Bus, Metro, Tram {
    String id();
    VehicleType type();
    int capacity();
    List<Passenger> passengersOnBoard();
    Direction direction();
    Vehicle withDirection(Direction direction);

    default double getOccupancyRate() {
        return (double) passengersOnBoard().size() / capacity();
    }
}
