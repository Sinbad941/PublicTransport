import Architecture.*;
import java.util.ArrayList;
import java.util.List;

import Passager.Passenger;
import Vehicule.Bus;
import Vehicule.Direction;
import Vehicule.VehicleType;

public class ReseauTransportImplTest {
    public static void main(String[] args) throws Exception {
        ReseauTransportImpl reseau = new ReseauTransportImpl();

        // Test initial state
        if (reseau.getAllLignes().size() != 0) {
            throw new RuntimeException("Initial reseau should have no lines");
        }

        // Test adding a line - create with correct signature
        LigneImpl ligne1 = new LigneImpl("L1", "Line 1", VehicleType.BUS);
        reseau.addLigne(ligne1);
        if (reseau.getAllLignes().size() != 1) {
            throw new RuntimeException("Should have 1 line after adding");
        }

        // Test adding multiple lines
        LigneImpl ligne2 = new LigneImpl("L2", "Line 2", VehicleType.METRO);
        reseau.addLigne(ligne2);
        if (reseau.getAllLignes().size() != 2) {
            throw new RuntimeException("Should have 2 lines, got " + reseau.getAllLignes().size());
        }

        // Test adding a station to a line
        StationImpl station = new StationImpl("S1", "StationA", new Coordinate(0, 0), 100, 5);
        Ligne ligne = reseau.getAllLignes().get(0);
        boolean stationAdded = ligne.addStation(station);
        if (!stationAdded) {
            throw new RuntimeException("Station should be added to line");
        }

        // Verify station is in line
        boolean hasStation = ligne.getListeStations().contains(station);
        if (!hasStation) {
            throw new RuntimeException("Station should be in line after adding");
        }

        // Test adding a vehicle to a line
        List<Passenger> passengers = new ArrayList<>();
        Bus bus = new Bus("BUS1", 50, 40.0, Direction.FORWARD, passengers);
        boolean vehicleAdded = reseau.addVehicleToLine(ligne1, bus);
        if (!vehicleAdded) {
            throw new RuntimeException("Vehicle should be added to line");
        }

        // Verify vehicle is in network
        boolean vehicleInNetwork = reseau.getAllLignes().stream()
            .anyMatch(l -> l.getVehicles().contains(bus));
        if (!vehicleInNetwork) {
            throw new RuntimeException("Vehicle should be in network");
        }

        // Test isRunning/isPaused initial state
        if (reseau.isRunning()) throw new RuntimeException("Reseau should not be running initially");
        if (reseau.isPaused()) throw new RuntimeException("Reseau should not be paused initially");

        System.out.println("PASS: ReseauTransportImplTest");
    }
}
