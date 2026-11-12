package Architecture;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import Incidents.ActiveIncident;
import Incidents.Incident;
import Passager.Passenger;
import Vehicule.Direction;
import Vehicule.Vehicle;
import Vehicule.VehicleType;

public class LigneImpl implements Ligne{
    private final String id;
    private final String nom;
    private final VehicleType typeVehicule;
    private final LinkedList<Station> Stations;
    private Station firstStation, lastStation;
    private final Set<Vehicle> vehiculePresents;
    // package-level flag to control logging verbosity (set by ReseauTransportImpl)
    public static volatile boolean VERBOSE_LOGGING = true;

    // incident tracking: per-vehicle and line-level
    private final Map<String, ActiveIncident> vehicleIncidents = new ConcurrentHashMap<>();
    private final AtomicReference<ActiveIncident> lineIncident = new AtomicReference<>(null);

    public LigneImpl(String id, String nom, VehicleType typevehicule){
        this.id = id;
        this.nom = nom;
        this.typeVehicule = typevehicule;
        this.Stations = new LinkedList<>();
        this.vehiculePresents = ConcurrentHashMap.newKeySet();
        this.firstStation = null;
        this.lastStation = null;
    }

    @Override
    public String getID(){
        return this.id;
    }
    @Override
    public String getNom(){
        return this.nom;
    }
    @Override
    public VehicleType getVehicleType(){
        return this.typeVehicule;
    }
    @Override
    public LinkedList<Station> getListeStations(){
        return this.Stations;
    }
    @Override
    public Station getFirstStation(){
        return this.firstStation;
    }
    @Override
    public Station getLastStation(){
        return this.lastStation;
    }
    @Override
    public String getStationID(Station station){
        if(Stations.contains(station)){
            return station.getId();
        }
        return "Cette station n'appartient pas a la ligne";
    }

    @Override
    public boolean addStation(Station station){
        boolean check =  Stations.add(station);
        if(check){
            this.lastStation = station;
            if(this.firstStation == null){
                this.firstStation = station;
            }
        }
        return check;
    }
    @Override
    public boolean removeStation(Station station){
        if(this.Stations.isEmpty()){
            return false;
        }
        if(Stations.contains(station)){
            Stations.remove(station);
            if(this.Stations.isEmpty()){
                    this.lastStation = null;
                    this.firstStation = null;
                    return true;
            }
            if(station == this.lastStation){
                this.lastStation = Stations.getLast();
                return true;
            }
            else if(station == this.firstStation){
                Stations.remove(station);
                this.firstStation = Stations.getFirst();
                return true;
            }else{
                return Stations.remove(station);
            }
            
        }
        return false;
    }
    @Override
    public boolean containsStation(Station station){
        return Stations.contains(station);
    }

    @Override
    public Station getNextStation(Station station){
        if(Stations.contains(station)){
            if(station == this.lastStation){
                return null;
            }
            return Stations.get(Stations.indexOf(station)+1);
        }
        return null;
    }
    @Override
    public Station getPreviousStation(Station station){
        if(Stations.contains(station)){
            if(station == this.firstStation){
                return null;
            }
            return Stations.get(Stations.indexOf(station)-1);
        }
        return null;
    }
    

    @Override
    public boolean addVehicle(Vehicle vehicule){
        if(!vehiculePresents.contains(vehicule)){
            boolean added = vehiculePresents.add(vehicule);
            if (added && firstStation != null) {
                firstStation.updateVehicleStatus(vehicule, true);
            }
            return added;
        }
        return false; 
    }
    @Override
    public boolean removeVehicle(Vehicle vehicule){
        if(vehiculePresents.contains(vehicule)){
            boolean removed = vehiculePresents.remove(vehicule);
            if (removed) {
                for (Station station : Stations) {
                    if (station.containsVehicle(vehicule)) {
                        station.updateVehicleStatus(vehicule, false);
                    }
                }
            }
            return removed;
        }
        return false;
    }
    @Override
    public boolean containsVehicle(Vehicle vehicule){
        return vehiculePresents.contains(vehicule);
    }
    @Override
    public java.util.List<Vehicle> getVehicles() {
        return List.copyOf(vehiculePresents);
    }
    @Override
    public String afficheTrainEnCirculation(){
        return this.vehiculePresents.toString();
    }
    // Compatibility method: some versions expect this name
    public String listeTrainEnCirculation(){
        return this.vehiculePresents.toString();
    }
    @Override
    public int nbPassagerEnCirculation(){
        int total = 0;

        // Count passengers currently on vehicles assigned to this line
        for (Vehicle v : vehiculePresents) {
            try {
                List<?> onboard = v.passengersOnBoard();
                if (onboard != null) {
                    total += onboard.size();
                }
            } catch (Exception ignored) {
                // defensive: if implementation varies, ignore and continue
            }
        }

        // Also include passengers currently waiting at each station on the line
        for (Station s : Stations) {
            try {
                total += s.getWaitingCount();
            } catch (Exception ignored) {
            }
        }

        return total;
    }

    @Override
    public void advanceVehicles() {
        if (Stations.isEmpty() || vehiculePresents.isEmpty()) {
            return;
        }

        for (Vehicle vehicle : List.copyOf(vehiculePresents)) {
            moveVehicleInternal(vehicle.id());
        }
    }

    @Override
    public void moveVehicleById(String vehicleId) {
        if (vehicleId == null) return;
        moveVehicleInternal(vehicleId);
    }

    // internal helper that moves a vehicle identified by id once along the line
    private void moveVehicleInternal(String vehicleId) {
        if (Stations.isEmpty() || vehiculePresents.isEmpty()) return;

        // --- LINE INCIDENT: block all vehicles on this line ---
        ActiveIncident li = lineIncident.get();
        if (li != null) {
            if (li.isActive()) {
                if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] LINE INCIDENT " + li.incident().type() + " - all vehicles blocked (remaining: " + java.time.Duration.between(LocalDateTime.now(), li.expiresAt()).toSeconds() + "s)");
                return;
            } else {
                lineIncident.compareAndSet(li, null);
                System.out.println("[Ligne:" + id + "] Line incident " + li.incident().type() + " expired, resuming.");
            }
        }

        Vehicle currentVehicle = null;
        for (Vehicle v : vehiculePresents) {
            if (v != null && vehicleId.equals(v.id())) {
                currentVehicle = v;
                break;
            }
        }

        // If vehicle not yet registered on any station, place it at first station
        Station currentStation = null;
        if (currentVehicle != null) {
            for (Station station : Stations) {
                if (station.containsVehicle(currentVehicle)) {
                    currentStation = station;
                    break;
                }
            }
        }

        if (currentVehicle == null) {
            for (Vehicle v : vehiculePresents) {
                if (v != null && vehicleId.equals(v.id())) {
                    currentVehicle = v;
                    break;
                }
            }
        }

        if (currentVehicle == null) {
            return;
        }

        // --- VEHICLE INCIDENT: block this specific vehicle ---
        ActiveIncident vi = vehicleIncidents.get(vehicleId);
        if (vi != null) {
            if (vi.isActive()) {
                if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] VEHICLE INCIDENT " + vi.incident().type() + " on " + vehicleId + " - blocked at station (remaining: " + java.time.Duration.between(LocalDateTime.now(), vi.expiresAt()).toSeconds() + "s)");
                // still allow deboarding passengers even during incident
                if (currentStation != null) boardAndDeboard(currentVehicle, currentStation);
                return;
            } else {
                vehicleIncidents.remove(vehicleId, vi);
                System.out.println("[Ligne:" + id + "] Vehicle incident " + vi.incident().type() + " on " + vehicleId + " expired, resuming.");
            }
        }

        if (currentStation == null) {
            currentStation = getFirstStation();
            if (currentStation != null) {
                currentStation.updateVehicleStatus(currentVehicle, true);
            }
        }

        // --- STATION INCIDENT: block vehicles departing from this station ---
        if (currentStation != null && currentStation.hasActiveIncident()) {
            Optional<ActiveIncident> si = currentStation.getCurrentIncident();
            if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] STATION INCIDENT " +
                si.map(a -> a.incident().type().toString()).orElse("?") + " at " + currentStation.getId() +
                " - vehicle " + vehicleId + " blocked (remaining: " +
                si.map(a -> java.time.Duration.between(LocalDateTime.now(), a.expiresAt()).toSeconds()).orElse(0L) + "s)");
            // allow deboarding but no departure
            boardAndDeboard(currentVehicle, currentStation);
            return;
        }

        // determine current direction
        Direction direction = currentVehicle.direction();

        // allow passengers to board/deboard at current station before moving
        if (currentStation != null) {
            if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] Vehicle " + currentVehicle.id() + " at station " + currentStation.getId() + " (dir=" + direction + ") - servicing");
            boardAndDeboard(currentVehicle, currentStation);
        }
        Station nextStation = direction == Direction.FORWARD ? getNextStation(currentStation) : getPreviousStation(currentStation);

        if (nextStation == null) {
            direction = direction.opposite();
            Vehicle updated = currentVehicle.withDirection(direction);
            vehiculePresents.remove(currentVehicle);
            vehiculePresents.add(updated);
            currentVehicle = updated;
            if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] Vehicle " + currentVehicle.id() + " reversed direction to " + direction);
            nextStation = direction == Direction.FORWARD ? getNextStation(currentStation) : getPreviousStation(currentStation);
        }

        if (currentStation != null && nextStation != null && !currentStation.equals(nextStation)) {
            if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] Vehicle " + currentVehicle.id() + " departing " + currentStation.getId() + " -> " + nextStation.getId() + " (dir=" + direction + ")");
            currentStation.updateVehicleStatus(currentVehicle, false);
        }
        if (nextStation != null) {
            nextStation.updateVehicleStatus(currentVehicle, true);
            if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] Vehicle " + currentVehicle.id() + " arrived at " + nextStation.getId());
            boardAndDeboard(currentVehicle, nextStation);
        }
    }

    private void boardAndDeboard(Vehicle vehicle, Station station) {
        for (Passenger passenger : List.copyOf(station.getWaitingPassengers(station))) {
            if (vehicle.passengersOnBoard().size() >= vehicle.capacity()) {
                break;
            }
            if (passenger.hasReachedDestination(station)) {
                continue;
            }
            if (station.removeWaitingPassenger(passenger)) {
                vehicle.passengersOnBoard().add(passenger);
                passenger.setEtat(Passager.Etat.ABORD);
                passenger.setBoardingTime(LocalDateTime.now());
                if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] Passenger " + passenger.getId() + " boarded vehicle " + vehicle.id() + " at " + station.getId() + " (onboard=" + vehicle.passengersOnBoard().size() + ")");
            }
        }

        for (Passenger passenger : List.copyOf(vehicle.passengersOnBoard())) {
            if (passenger.hasReachedDestination(station)) {
                vehicle.passengersOnBoard().remove(passenger);
                passenger.setEtat(Passager.Etat.DESCENTE);
                passenger.setArrivalTime(LocalDateTime.now());
                if (VERBOSE_LOGGING) System.out.println("[Ligne:" + id + "] Passenger " + passenger.getId() + " disembarked vehicle " + vehicle.id() + " at " + station.getId() + " (onboard=" + vehicle.passengersOnBoard().size() + ")");
            }
        }
    }

    @Override
    public Duration travelTime(Station station1, Station station2){
        if (station1 == null || station2 == null) return Duration.ZERO;

        int idx1 = Stations.indexOf(station1);
        int idx2 = Stations.indexOf(station2);
        if (idx1 == -1 || idx2 == -1) return Duration.ZERO;

        int segments = Math.abs(idx1 - idx2);
        if (segments == 0) return Duration.ZERO;

        long minutesPerSegment = 2L;
        return Duration.ofMinutes(minutesPerSegment * segments);
    }

    // --- incident management implementation ---

    @Override
    public void reportIncidentToVehicle(String vehicleId, Incident incident) {
        ActiveIncident ai = ActiveIncident.of(incident);
        vehicleIncidents.put(vehicleId, ai);
        System.out.println("[Ligne:" + id + "] Vehicle incident reported on " + vehicleId + ": " + ai);
    }

    @Override
    public void clearVehicleIncident(String vehicleId) {
        ActiveIncident removed = vehicleIncidents.remove(vehicleId);
        if (removed != null) System.out.println("[Ligne:" + id + "] Vehicle incident cleared on " + vehicleId);
    }

    @Override
    public boolean vehicleHasActiveIncident(String vehicleId) {
        ActiveIncident ai = vehicleIncidents.get(vehicleId);
        if (ai == null) return false;
        if (ai.isExpired()) { vehicleIncidents.remove(vehicleId, ai); return false; }
        return true;
    }

    @Override
    public Optional<ActiveIncident> getVehicleIncident(String vehicleId) {
        ActiveIncident ai = vehicleIncidents.get(vehicleId);
        if (ai == null || ai.isExpired()) return Optional.empty();
        return Optional.of(ai);
    }

    @Override
    public void reportIncidentToLine(Incident incident) {
        ActiveIncident ai = ActiveIncident.of(incident);
        lineIncident.set(ai);
        System.out.println("[Ligne:" + id + "] Line incident reported: " + ai);
    }

    @Override
    public void clearLineIncident() {
        ActiveIncident removed = lineIncident.getAndSet(null);
        if (removed != null) System.out.println("[Ligne:" + id + "] Line incident cleared: " + removed.incident().type());
    }

    @Override
    public boolean lineHasActiveIncident() {
        ActiveIncident ai = lineIncident.get();
        if (ai == null) return false;
        if (ai.isExpired()) { lineIncident.compareAndSet(ai, null); return false; }
        return true;
    }

    @Override
    public Optional<ActiveIncident> getLineIncident() {
        ActiveIncident ai = lineIncident.get();
        if (ai == null || ai.isExpired()) return Optional.empty();
        return Optional.of(ai);
    }
}