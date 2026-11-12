import Incidents.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class ActiveIncidentTest {
    public static void main(String[] args) throws Exception {
        // Test ActiveIncident creation
        Incident incident = new Incident(IncidentsTypes.RETARD, Duration.ofSeconds(2));
        ActiveIncident active = ActiveIncident.of(incident);

        // Test that it's initially active
        if (!active.isActive()) throw new RuntimeException("ActiveIncident should be active initially");
        if (active.isExpired()) throw new RuntimeException("ActiveIncident should not be expired initially");

        // Test incident is stored
        if (active.incident() != incident) throw new RuntimeException("incident mismatch");
        if (active.incident().type() != IncidentsTypes.RETARD) throw new RuntimeException("incident type mismatch");

        // Test expiry time is in the future
        LocalDateTime now = LocalDateTime.now();
        if (!active.expiresAt().isAfter(now)) throw new RuntimeException("expiresAt should be in the future");

        // Test toString
        String toString = active.toString();
        if (!toString.contains("RETARD")) throw new RuntimeException("toString should contain incident type");

        // Test manual creation with past expiry
        LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);
        ActiveIncident expired = new ActiveIncident(incident, pastTime);
        if (expired.isActive()) throw new RuntimeException("expired incident should not be active");
        if (!expired.isExpired()) throw new RuntimeException("incident should be expired");

        // Test manual creation with future expiry
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(100);
        ActiveIncident notExpired = new ActiveIncident(incident, futureTime);
        if (!notExpired.isActive()) throw new RuntimeException("future incident should be active");
        if (notExpired.isExpired()) throw new RuntimeException("future incident should not be expired");

        System.out.println("PASS: ActiveIncidentTest");
    }
}
