package Incidents;

import java.time.LocalDateTime;

/**
 * Wraps an Incident with its expiry time.
 * An ActiveIncident is expired when LocalDateTime.now() is after expiresAt.
 */
public record ActiveIncident(Incident incident, LocalDateTime expiresAt) {

    public static ActiveIncident of(Incident incident) {
        LocalDateTime expiry = LocalDateTime.now().plus(incident.dureeDecalage());
        return new ActiveIncident(incident, expiry);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return !isExpired();
    }

    @Override
    public String toString() {
        return "[" + incident.type() + " until " + expiresAt + "]";
    }
}
