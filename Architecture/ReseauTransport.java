package Architecture;

import java.time.Duration;
import java.util.List;

import Incidents.Incident;
import Vehicule.Vehicle;

public interface ReseauTransport{
    void start();
    void stop();
    void pause();
    void resume();

    boolean isRunning();
    boolean isPaused();

    Ligne getLigne(String id);
    List<Ligne> getAllLignes();

    boolean addLigne(Ligne ligne);
    boolean removeLigne(Ligne ligne);

    boolean addVehicleToLine(Ligne ligne, Vehicle vehicule);
    boolean removeVehicleFromLine(Ligne ligne, Vehicle vehicule);

    void tick();
    void progress(Duration duration);

    // --- incident dispatch ---
    boolean reportIncidentToVehicle(String vehicleId, Incident incident);
    boolean reportIncidentToStation(String stationId, Incident incident);
    boolean reportIncidentToLine(String lineId, Incident incident);
}