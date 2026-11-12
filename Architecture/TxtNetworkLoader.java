package Architecture;

import Passager.Passenger;
import Vehicule.Bus;
import Vehicule.Direction;
import Vehicule.Metro;
import Vehicule.Tram;
import Vehicule.Vehicle;
import Vehicule.VehicleType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TxtNetworkLoader {
    private static final int DEFAULT_WAITING_CAPACITY = 100;
    private static final int DEFAULT_STATION_VEHICLE_CAPACITY = 10;

    private TxtNetworkLoader() {}

    public static ReseauTransportImpl load(String filePath) throws IOException {
        return load(Path.of(filePath));
    }

    public static ReseauTransportImpl load(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        return parse(lines);
    }

    public static ReseauTransportImpl parse(List<String> lines) {
        ReseauTransportImpl reseau = new ReseauTransportImpl();

        Map<String, LigneImpl> lignesById = new LinkedHashMap<>();
        Map<String, Integer> stationCountByLine = new HashMap<>();
        Map<String, Integer> lineIndexById = new HashMap<>();
        Set<String> usedVehicleIds = new HashSet<>();
        Set<String> usedStationIds = new HashSet<>();

        int lineNumber = 0;
        for (String raw : lines) {
            lineNumber++;
            if (raw == null) {
                continue;
            }

            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] parts = trimmed.split("\\s+");
            String keyword = parts[0].toUpperCase(Locale.ROOT);

            switch (keyword) {
                case "LIGNE" -> parseLineDeclaration(trimmed, parts, lineNumber, reseau, lignesById, lineIndexById);
                case "STATION" -> parseStationDeclaration(trimmed, parts, lineNumber, lignesById, stationCountByLine, lineIndexById, usedStationIds);
                case "BUS", "METRO", "TRAM", "TRAMWAY" -> parseVehicleDeclaration(trimmed, parts, lineNumber, reseau, lignesById, usedVehicleIds);
                default -> throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: mot-clé inconnu '" + parts[0] + "'");
            }
        }

        return reseau;
    }

    private static void parseLineDeclaration(
        String trimmed,
        String[] parts,
        int lineNumber,
        ReseauTransportImpl reseau,
        Map<String, LigneImpl> lignesById,
        Map<String, Integer> lineIndexById
    ) {
        if (parts.length < 2) {
            throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: format attendu LIGNE <id>");
        }

        String lineId = joinTokens(parts, 1, parts.length).toUpperCase(Locale.ROOT);
        if (lignesById.containsKey(lineId)) {
            throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: ligne déjà définie '" + lineId + "'");
        }

        LigneImpl ligne = new LigneImpl(lineId, lineId, VehicleType.BUS);
        reseau.addLigne(ligne);
        lignesById.put(lineId, ligne);
        lineIndexById.put(lineId, lineIndexById.size());
    }

    private static void parseStationDeclaration(
        String trimmed,
        String[] parts,
        int lineNumber,
        Map<String, LigneImpl> lignesById,
        Map<String, Integer> stationCountByLine,
        Map<String, Integer> lineIndexById,
        Set<String> usedStationIds
    ) {
        if (parts.length < 3) {
            throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: format attendu STATION <nom> <ligne>");
        }

        String lineId = parts[parts.length - 1].toUpperCase(Locale.ROOT);
        LigneImpl ligne = lignesById.get(lineId);
        if (ligne == null) {
            throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: ligne inconnue '" + lineId + "'");
        }

        String stationName = joinTokens(parts, 1, parts.length - 1);
        int stationIndex = stationCountByLine.getOrDefault(lineId, 0);
        int y = lineIndexById.getOrDefault(lineId, 0);

        String baseId = (lineId + "_" + stationName)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
        if (baseId.isEmpty()) {
            baseId = "station";
        }
        String stationId = uniqueId(baseId, usedStationIds);

        StationImpl station = new StationImpl(
            stationId,
            stationName,
            new Coordinate(stationIndex, y),
            DEFAULT_WAITING_CAPACITY,
            DEFAULT_STATION_VEHICLE_CAPACITY
        );

        boolean added = ligne.addStation(station);
        if (!added) {
            throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: station non ajoutée '" + stationName + "'");
        }
        stationCountByLine.put(lineId, stationIndex + 1);
    }

    private static void parseVehicleDeclaration(
        String trimmed,
        String[] parts,
        int lineNumber,
        ReseauTransportImpl reseau,
        Map<String, LigneImpl> lignesById,
        Set<String> usedVehicleIds
    ) {
        if (parts.length < 3) {
            throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: format attendu <BUS|METRO|TRAMWAY> <id> <ligne>");
        }

        String typeToken = parts[0].toUpperCase(Locale.ROOT);
        String lineId = parts[parts.length - 1].toUpperCase(Locale.ROOT);
        LigneImpl ligne = lignesById.get(lineId);
        if (ligne == null) {
            throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: ligne inconnue '" + lineId + "'");
        }

        String rawVehicleId = joinTokens(parts, 1, parts.length - 1);
        String vehicleIdBase = (typeToken + "_" + rawVehicleId)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
        if (vehicleIdBase.isEmpty()) {
            vehicleIdBase = "vehicle";
        }
        String vehicleId = uniqueId(vehicleIdBase, usedVehicleIds);

        Vehicle vehicle = switch (typeToken) {
            case "BUS" -> new Bus(vehicleId, 40, 30.0, Direction.FORWARD, new ArrayList<Passenger>());
            case "METRO" -> new Metro(vehicleId, 300, 45.0, Direction.FORWARD, new ArrayList<Passenger>());
            case "TRAM", "TRAMWAY" -> new Tram(vehicleId, 120, 35.0, Direction.FORWARD, new ArrayList<Passenger>());
            default -> throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: type véhicule inconnu '" + typeToken + "'");
        };

        boolean added = reseau.addVehicleToLine(ligne, vehicle);
        if (!added) {
            throw new IllegalArgumentException("Ligne " + lineNumber + " invalide: véhicule non ajouté '" + vehicleId + "'");
        }
    }

    private static String joinTokens(String[] parts, int fromInclusive, int toExclusive) {
        StringBuilder sb = new StringBuilder();
        for (int i = fromInclusive; i < toExclusive; i++) {
            if (i > fromInclusive) {
                sb.append(' ');
            }
            sb.append(parts[i]);
        }
        return sb.toString().trim();
    }

    private static String uniqueId(String base, Set<String> usedIds) {
        String candidate = base;
        int idx = 2;
        while (usedIds.contains(candidate)) {
            candidate = base + "_" + idx;
            idx++;
        }
        usedIds.add(candidate);
        return candidate;
    }
}