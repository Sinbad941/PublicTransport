import Incidents.*;
import java.time.Duration;

public class IncidentTest {
    public static void main(String[] args) throws Exception {
        // Test Incident creation with RETARD
        Incident incident1 = new Incident(IncidentsTypes.RETARD, Duration.ofSeconds(30));
        if (incident1.type() != IncidentsTypes.RETARD) throw new RuntimeException("type mismatch for RETARD");
        if (!incident1.dureeDecalage().equals(Duration.ofSeconds(30))) throw new RuntimeException("duration mismatch");

        // Test Incident creation with MALAISEPASSAGER
        Incident incident2 = new Incident(IncidentsTypes.MALAISEPASSAGER, Duration.ofMinutes(5));
        if (incident2.type() != IncidentsTypes.MALAISEPASSAGER) throw new RuntimeException("type mismatch for MALAISEPASSAGER");
        if (!incident2.dureeDecalage().equals(Duration.ofMinutes(5))) throw new RuntimeException("duration mismatch");

        // Test Incident creation with PERSONNESURVOIE
        Incident incident3 = new Incident(IncidentsTypes.PERSONNESURVOIE, Duration.ofSeconds(60));
        if (incident3.type() != IncidentsTypes.PERSONNESURVOIE) throw new RuntimeException("type mismatch for PERSONNESURVOIE");
        if (!incident3.dureeDecalage().equals(Duration.ofSeconds(60))) throw new RuntimeException("duration mismatch");

        // Test toString
        String toString = incident1.toString();
        if (!toString.contains("Incident") && !toString.contains("RETARD")) {
            throw new RuntimeException("toString doesn't contain expected content");
        }

        System.out.println("PASS: IncidentTest");
    }
}
