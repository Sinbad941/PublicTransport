package Architecture;

import java.time.Duration;
import java.util.Optional;
import java.util.Queue;

import Incidents.ActiveIncident;
import Incidents.Incident;
import Passager.Passenger;
import Vehicule.Vehicle;

/**
 * Représente une station/arrêt du réseau de transports.
 */
public interface Station {
    String getId();
    String getNom();
    Coordinate getPosition();
    int getMaxWaitingCapacity();

    Queue<Passenger> getWaitingPassengers(Station destination);
    boolean addWaitingPassenger(Passenger passenger);
    boolean removeWaitingPassenger(Passenger passenger);
    int getWaitingCount();

    boolean canAccommodateVehicle();
    void updateVehicleStatus(Vehicle vehicle, boolean present);
    boolean containsVehicle(Vehicle vehicle);

    Duration getAverageWaitingTime();
    int getPassengersServedToday();

    // --- incident management ---
    void reportIncident(Incident incident);
    void clearIncident();
    boolean hasActiveIncident();
    Optional<ActiveIncident> getCurrentIncident();
}


