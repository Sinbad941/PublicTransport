package Passager;

import Architecture.Station;
import java.time.LocalDateTime;

public class Passenger {
    private final String id;
    private final Station boardingStation;
    private final Station exitStation;
    private Etat etat;
    private LocalDateTime waitingSince;
    private LocalDateTime boardingTime;
    private LocalDateTime arrivalTime;

    public Passenger(String id, Station boardingStation, Station exitStation) {
        this.id = id;
        this.boardingStation = boardingStation;
        this.exitStation = exitStation;
        this.etat = Etat.ATTENTE;
        this.waitingSince = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public Station getBoardingStation() {
        return boardingStation;
    }

    public Station getExitStation() {
        return exitStation;
    }

    public Etat getEtat() {
        return etat;
    }

    public void setEtat(Etat etat) {
        this.etat = etat;
    }

    public LocalDateTime getWaitingSince() {
        return waitingSince;
    }

    public LocalDateTime getBoardingTime() {
        return boardingTime;
    }

    public void setBoardingTime(LocalDateTime boardingTime) {
        this.boardingTime = boardingTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public boolean hasReachedDestination(Station station) {
        return exitStation.equals(station);
    }
}
