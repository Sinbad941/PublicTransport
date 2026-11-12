import Vehicule.*;
import Architecture.Coordinate;
import Architecture.Station;
import Architecture.StationImpl;
import Passager.Passenger;
import java.util.ArrayList;
import java.util.List;

public class VehicleTest {
    public static void main(String[] args) throws Exception {
        Station s1 = new StationImpl("s1","A", new Coordinate(0,0),10,2);
        Station s2 = new StationImpl("s2","B", new Coordinate(1,1),10,2);
        Passenger p1 = new Passenger("p1", s1, s2);
        Passenger p2 = new Passenger("p2", s1, s2);
        List<Passenger> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);
        Bus bus = new Bus("bus1", 30, 40.0, list);
        if (!"bus1".equals(bus.id())) throw new RuntimeException("bus id");
        if (bus.capacity() != 30) throw new RuntimeException("capacity");
        if (bus.passengersOnBoard().size() != 2) throw new RuntimeException("passenger count");
        double rate = bus.getOccupancyRate();
        if (rate <= 0.0) throw new RuntimeException("occupancy rate should be >0");
        System.out.println("PASS: VehicleTest");
    }
}
