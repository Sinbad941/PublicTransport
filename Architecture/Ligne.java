package Architecture;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import Incidents.ActiveIncident;
import Incidents.Incident;
import Vehicule.Vehicle;
import Vehicule.VehicleType;

public interface Ligne{
    String getID();
    String getNom();
    VehicleType getVehicleType();
    List<Station> getListeStations();
    Station getFirstStation();
    Station getLastStation();
    String getStationID(Station station);

    boolean addStation(Station station);
    boolean removeStation(Station station);
    boolean containsStation(Station station);

    Station getNextStation(Station station);
    Station getPreviousStation(Station station);

    boolean addVehicle(Vehicle vehicule);
    boolean removeVehicle(Vehicle vehicule);
    boolean containsVehicle(Vehicle vehicule);
    java.util.List<Vehicle> getVehicles();

    String afficheTrainEnCirculation();
    int nbPassagerEnCirculation();

    Duration travelTime(Station station1, Station station2);
    void advanceVehicles();
    void moveVehicleById(String vehicleId);

    // --- incident management ---
    void reportIncidentToVehicle(String vehicleId, Incident incident);
    void clearVehicleIncident(String vehicleId);
    boolean vehicleHasActiveIncident(String vehicleId);
    Optional<ActiveIncident> getVehicleIncident(String vehicleId);

    void reportIncidentToLine(Incident incident);
    void clearLineIncident();
    boolean lineHasActiveIncident();
    Optional<ActiveIncident> getLineIncident();
}