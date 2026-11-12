package Architecture;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import Incidents.Incident;
import Vehicule.Vehicle;

public class ReseauTransportImpl implements ReseauTransport{
    private static final Duration DEFAULT_TICK = Duration.ofSeconds(1);

    private final CopyOnWriteArrayList<Ligne> Lines;
    private final Set<Vehicle> Vehicules;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;
    // worker threads per vehicle id
    private final Map<String, Thread> vehicleThreads;
    private final Map<String, AtomicBoolean> vehicleThreadFlags;
    // speed control: 1.0 = normal, 0.1 = slow (10x slower), 10.0 = fast (10x faster)
    private double speedFactor = 1.0;
    private boolean verboseLogging = true;
    private volatile long tickMillis;

    public ReseauTransportImpl(){
        this.Lines = new CopyOnWriteArrayList<>();
        this.Vehicules = ConcurrentHashMap.newKeySet();
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.vehicleThreads = new ConcurrentHashMap<>();
        this.vehicleThreadFlags = new ConcurrentHashMap<>();
        this.tickMillis = DEFAULT_TICK.toMillis();
    }

    @Override
    public void start(){
        if (running.compareAndSet(false, true)) {
            paused.set(false);
            // start workers for existing vehicles
            for (Ligne ligne : Lines) {
                for (Vehicle v : ligne.getVehicles()) {
                    startVehicleWorkerIfAbsent(ligne, v);
                }
            }
        }
    }

    @Override
    public void stop(){
        running.set(false);
        paused.set(false);
        // stop and clear all vehicle threads
        for (Thread t : vehicleThreads.values()) {
            if (t != null && t.isAlive()) {
                t.interrupt();
            }
        }
        vehicleThreads.clear();
        vehicleThreadFlags.clear();
    }

    @Override
    public void pause(){
        if (running.get()) {
            paused.set(true);
        }
    }

    @Override
    public void resume(){
        if (running.get()) {
            paused.set(false);
        }
    }

    @Override
    public boolean isRunning(){
        return running.get();
    }

    @Override
    public boolean isPaused(){
        return paused.get();
    }

    @Override
    public Ligne getLigne(String id){
        if (id == null) {
            return null;
        }
        for (Ligne ligne : this.Lines) {
            if (id.equals(ligne.getID())) {
                return ligne;
            }
        }
        return null;
    }

    @Override
    public List<Ligne> getAllLignes(){
        return List.copyOf(this.Lines);
    }

    @Override
    public boolean addLigne(Ligne ligne){
        Objects.requireNonNull(ligne, "Ligne cannot be null");
        return Lines.addIfAbsent(ligne);
    }

    @Override
    public boolean removeLigne(Ligne ligne){
        Objects.requireNonNull(ligne, "Ligne cannot be null");
        return Lines.remove(ligne);
    }

    @Override
    public boolean addVehicleToLine(Ligne ligne, Vehicle vehicule){
        Objects.requireNonNull(ligne, "Ligne cannot be null");
        Objects.requireNonNull(vehicule, "Vehicle cannot be null");
        if (this.Lines.contains(ligne)) {
            int i = this.Lines.indexOf(ligne);
            boolean added = Lines.get(i).addVehicle(vehicule);
            if (added) {
                Vehicules.add(vehicule);
                if (running.get()) {
                    System.out.println("[Reseau] Starting worker for vehicle " + vehicule.id());
                    startVehicleWorkerIfAbsent(ligne, vehicule);
                }
            }
            return added;
        }
        return false;
    }

    @Override
    public boolean removeVehicleFromLine(Ligne ligne, Vehicle vehicule){
        Objects.requireNonNull(ligne, "Ligne cannot be null");
        Objects.requireNonNull(vehicule, "Vehicle cannot be null");
        if (this.Lines.contains(ligne)) {
            int i = this.Lines.indexOf(ligne);
            boolean removed = Lines.get(i).removeVehicle(vehicule);
            if (removed) {
                Vehicules.remove(vehicule);
                // stop worker for this vehicle id
                Thread t = vehicleThreads.remove(vehicule.id());
                if (t != null && t.isAlive()) {
                    System.out.println("[Reseau] Stopping worker for vehicle " + vehicule.id());
                    t.interrupt();
                }
                vehicleThreadFlags.remove(vehicule.id());
            }
            return removed;
        }
        return false;
    }

    @Override
    public void tick(){
        if (!running.get() || paused.get()) {
            return;
        }
        for (Ligne ligne : Lines) {
            ligne.advanceVehicles();
        }
    }

    // start a worker thread for a specific vehicle id if none exists
    private void startVehicleWorkerIfAbsent(Ligne ligne, Vehicle vehicule) {
        String vid = vehicule.id();
        vehicleThreadFlags.putIfAbsent(vid, new AtomicBoolean(true));
        Thread existing = vehicleThreads.get(vid);
        if (existing != null && existing.isAlive()) return;

        Runnable r = () -> {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                if (paused.get()) {
                    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                    continue;
                }
                try {
                    ligne.moveVehicleById(vid);
                        long sleepMs = (long) (getTickMillis() * speedFactor);
                        Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {
                }
            }
        };

        Thread t = new Thread(r, "vehicle-worker-" + vid);
        t.setDaemon(true);
        vehicleThreads.put(vid, t);
        System.out.println("[Reseau] Worker thread created for vehicle " + vid);
        t.start();
    }

    public long getTickMillis() {
        return tickMillis;
    }

    public void setTickMillis(long millis) {
        if (millis <= 0) {
            throw new IllegalArgumentException("tick duration must be positive");
        }
        this.tickMillis = millis;
    }

    public void slowDown10x() {
        long next = Math.min(tickMillis * 10, Duration.ofMinutes(1).toMillis());
        tickMillis = next;
    }

    public void speedUp10x() {
        long next = Math.max(tickMillis / 10, 1);
        tickMillis = next;
    }

    public void setSpeedFactor(double factor) {
        this.speedFactor = Math.max(0.01, factor);
    }

    public double getSpeedFactor() {
        return speedFactor;
    }

    public void setVerboseLogging(boolean verbose) {
        this.verboseLogging = verbose;
        // propagate to all lines
        LigneImpl.VERBOSE_LOGGING = verbose;
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    @Override
    public void progress(Duration duration){
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return;
        }
        long ticks = duration.dividedBy(DEFAULT_TICK);
        for (long i = 0; i < Math.max(1, ticks); i++) {
            tick();
        }
    }

    // --- incident dispatch ---

    @Override
    public boolean reportIncidentToVehicle(String vehicleId, Incident incident) {
        for (Ligne ligne : Lines) {
            boolean found = ligne.getVehicles().stream().anyMatch(v -> vehicleId.equals(v.id()));
            if (found) {
                ligne.reportIncidentToVehicle(vehicleId, incident);
                return true;
            }
        }
        System.out.println("[Reseau] Vehicle not found: " + vehicleId);
        return false;
    }

    @Override
    public boolean reportIncidentToStation(String stationId, Incident incident) {
        for (Ligne ligne : Lines) {
            for (Station station : ligne.getListeStations()) {
                if (stationId.equals(station.getId())) {
                    station.reportIncident(incident);
                    return true;
                }
            }
        }
        System.out.println("[Reseau] Station not found: " + stationId);
        return false;
    }

    @Override
    public boolean reportIncidentToLine(String lineId, Incident incident) {
        Ligne ligne = getLigne(lineId);
        if (ligne != null) {
            ligne.reportIncidentToLine(incident);
            return true;
        }
        System.out.println("[Reseau] Line not found: " + lineId);
        return false;
    }
}