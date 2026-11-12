import Architecture.*;
import Passager.Passenger;
import Vehicule.Bus;
import Vehicule.VehicleType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LigneImplUnitTest {
    public static void main(String[] args) throws Exception {
        LigneImpl ligne = new LigneImpl("L1","Ligne 1", VehicleType.BUS);
        StationImpl s1 = new StationImpl("s1","S1", new Coordinate(0,0), 10, 2);
        StationImpl s2 = new StationImpl("s2","S2", new Coordinate(1,0), 10, 2);
        StationImpl s3 = new StationImpl("s3","S3", new Coordinate(2,0), 10, 2);
        if (!ligne.addStation(s1)) throw new RuntimeException("add s1");
        if (!ligne.addStation(s2)) throw new RuntimeException("add s2");
        if (!ligne.addStation(s3)) throw new RuntimeException("add s3");
        if (!ligne.containsStation(s2)) throw new RuntimeException("contains s2");
        if (ligne.getNextStation(s3) != null) throw new RuntimeException("s3 next should be null");
        if (!ligne.getNextStation(s2).equals(s3)) throw new RuntimeException("next station mismatch");
        if (!ligne.getPreviousStation(s2).equals(s1)) throw new RuntimeException("previous station mismatch");
        Duration d = ligne.travelTime(s1, s3);
        if (!d.equals(Duration.ofMinutes(4))) throw new RuntimeException("travel time mismatch expected 4 minutes");

        // test passengers in circulation: add waiting passengers and a vehicle with 1 passenger
        Passenger p = new Passenger("p1", s1, s3);
        s1.addWaitingPassenger(p);
        List<Passenger> list = new ArrayList<>();
        list.add(new Passenger("p2", s1, s2));
        Bus bus = new Bus("bus1", 20, 40.0, list);
        ligne.addVehicle(bus);
        // ligne.nbPassagerEnCirculation should count waiting + onboard
        int total = ligne.nbPassagerEnCirculation();
        if (total < 2) throw new RuntimeException("nbPassagerEnCirculation too small: " + total);

        System.out.println("PASS: LigneImplUnitTest");
    }
}
