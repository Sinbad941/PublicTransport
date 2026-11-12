package Architecture;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import Incidents.ActiveIncident;
import Incidents.Incident;
import Passager.Passenger;
import Vehicule.Vehicle;

public class StationImpl implements Station{

    private final String id;
    private final String nom;
    private final Coordinate position;
    private final int MaxWaitingCapacity;
    private final int MaxVehicleCapacity;
    private final ConcurrentLinkedQueue<Passenger> passengersWaiting;
    private final AtomicInteger sizeQueue;
    private final Set<Vehicle> vehiculePresents;
    private int nbVehicules;
    private static int TotalPassagers = 0;
    private final AtomicReference<ActiveIncident> activeIncident = new AtomicReference<>(null);

    public StationImpl(String id, String nom, Coordinate position, int max1, int max2){
        this.id = id;
        this.nom = nom;
        this.position = position;
        this.MaxWaitingCapacity = max1;
        this.MaxVehicleCapacity =  max2;
        passengersWaiting = new ConcurrentLinkedQueue<>();
        sizeQueue = new AtomicInteger();
        vehiculePresents = ConcurrentHashMap.newKeySet();
        nbVehicules = 0;
    }

    @Override
    public String getId(){
        return this.id;
    }

    @Override
    public String getNom(){
        return this.nom;
    }

    @Override
    public Coordinate getPosition(){
        return this.position;
    }

    @Override
    public int getMaxWaitingCapacity(){
        return this.MaxWaitingCapacity;
    }

    @Override
    public ConcurrentLinkedQueue<Passenger> getWaitingPassengers(Station destination){
        return passengersWaiting;
    }

    @Override
    public boolean addWaitingPassenger(Passenger passenger) {
        while (true) {
            int current = sizeQueue.get();
            if (current >= MaxWaitingCapacity) {
                return false;
            }
            if (sizeQueue.compareAndSet(current, current + 1)) {
                boolean added = passengersWaiting.add(passenger);
                if (added) {
                    TotalPassagers++;
                    return true;
                }
                sizeQueue.decrementAndGet();
                return false;
            }
        }
    }

    @Override
    public boolean removeWaitingPassenger(Passenger passenger){
        if(passengersWaiting.remove(passenger)){
            sizeQueue.getAndDecrement();
            return true;
        }
        return false;
    }

    @Override
    public int getWaitingCount(){
        return sizeQueue.get();
    }

    @Override
    public boolean canAccommodateVehicle(){
        return nbVehicules < MaxVehicleCapacity;
    }

    @Override
    public void updateVehicleStatus(Vehicle vehicle, boolean present){
        if(present){
            if(!vehiculePresents.contains(vehicle)){
                vehiculePresents.add(vehicle);
                nbVehicules++;
            }
        }else{
            if(vehiculePresents.remove(vehicle)){
                nbVehicules--;
            }
        }
    }

    @Override
    public boolean containsVehicle(Vehicle vehicle) {
        return vehiculePresents.contains(vehicle);
    }

    @Override
    public Duration getAverageWaitingTime() {
        LocalDateTime now = LocalDateTime.now();
        long totalMillis = 0;
        int count = 0;

        for (Passenger passenger : passengersWaiting) {
            totalMillis += Duration.between(passenger.getWaitingSince(), now).toMillis();
            count++;
        }

        if (count == 0) {
            return Duration.ZERO;
        }

        return Duration.ofMillis(totalMillis / count);
    }

    @Override
    public int getPassengersServedToday(){
        return TotalPassagers;
    }

    // --- incident management ---

    @Override
    public void reportIncident(Incident incident) {
        ActiveIncident ai = ActiveIncident.of(incident);
        activeIncident.set(ai);
        System.out.println("[Station:" + id + "] Incident reported: " + ai);
    }

    @Override
    public void clearIncident() {
        ActiveIncident ai = activeIncident.getAndSet(null);
        if (ai != null) {
            System.out.println("[Station:" + id + "] Incident cleared: " + ai.incident().type());
        }
    }

    @Override
    public boolean hasActiveIncident() {
        ActiveIncident ai = activeIncident.get();
        if (ai == null) return false;
        if (ai.isExpired()) {
            activeIncident.compareAndSet(ai, null);
            return false;
        }
        return true;
    }

    @Override
    public Optional<ActiveIncident> getCurrentIncident() {
        ActiveIncident ai = activeIncident.get();
        if (ai == null || ai.isExpired()) return Optional.empty();
        return Optional.of(ai);
    }
}