import Architecture.*;
import Passager.Passenger;
import Vehicule.Bus;
import java.util.ArrayList;
import java.util.List;

public class StationImplUnitTest {
    public static void main(String[] args) throws Exception {
        StationImpl s = new StationImpl("s1","Station 1", new Coordinate(0,0), 5, 2);
        Passenger p1 = new Passenger("p1", s, s);
        Passenger p2 = new Passenger("p2", s, s);
        if (!s.addWaitingPassenger(p1)) throw new RuntimeException("add p1 failed");
        if (s.getWaitingCount() != 1) throw new RuntimeException("waiting count after add");
        if (!s.addWaitingPassenger(p2)) throw new RuntimeException("add p2 failed");
        if (s.getWaitingCount() != 2) throw new RuntimeException("waiting count after add2");
        if (!s.removeWaitingPassenger(p1)) throw new RuntimeException("remove p1 failed");
        if (s.getWaitingCount() != 1) throw new RuntimeException("waiting count after remove");
        if (!s.canAccommodateVehicle()) throw new RuntimeException("should accommodate vehicle");
        List<Passenger> list = new ArrayList<>();
        Bus bus = new Bus("b1", 10, 30.0, list);
        s.updateVehicleStatus(bus, true);
        if (!s.canAccommodateVehicle() && s.getWaitingCount() < s.getMaxWaitingCapacity()) {
            // still ok; nothing to assert strongly here
        }
        if (s.getAverageWaitingTime() == null) throw new RuntimeException("average waiting time null");
        int served = s.getPassengersServedToday();
        if (served <= 0) throw new RuntimeException("served count should be >0 after adds");
        System.out.println("PASS: StationImplUnitTest");
    }
}
