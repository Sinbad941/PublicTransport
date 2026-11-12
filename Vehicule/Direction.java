package Vehicule;

public enum Direction {
    FORWARD,
    BACKWARD;

    public Direction opposite() {
        return this == FORWARD ? BACKWARD : FORWARD;
    }
}
