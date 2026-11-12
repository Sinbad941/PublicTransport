import Passager.*;
import Architecture.Coordinate;
import Architecture.Station;
import Architecture.StationImpl;

public class PassengerTest {
    public static void main(String[] args) throws Exception {
        Station s1 = new StationImpl("s1","A", new Coordinate(0,0),10,2);
        Station s2 = new StationImpl("s2","B", new Coordinate(1,1),10,2);
        Passenger p = new Passenger("p1", s1, s2);
        if (!"p1".equals(p.getId())) throw new RuntimeException("id mismatch");
        if (!s1.equals(p.getBoardingStation())) throw new RuntimeException("boarding station mismatch");
        if (!s2.equals(p.getExitStation())) throw new RuntimeException("exit station mismatch");
        if (p.getEtat() != Etat.ATTENTE) throw new RuntimeException("initial etat mismatch");
        p.setEtat(Etat.ABORD);
        if (p.getEtat() != Etat.ABORD) throw new RuntimeException("etat setter failed");
        if (p.hasReachedDestination(s1)) throw new RuntimeException("hasReachedDestination false positive");
        if (!p.hasReachedDestination(s2)) throw new RuntimeException("hasReachedDestination failed");
        System.out.println("PASS: PassengerTest");
    }
}
