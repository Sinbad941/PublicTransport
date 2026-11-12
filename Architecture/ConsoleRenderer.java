package Architecture;

import Vehicule.Direction;
import Vehicule.Vehicle;

public final class ConsoleRenderer {
    private ConsoleRenderer() {}

    public static void render(ReseauTransport reseau) {
        System.out.println("\n=== Network Snapshot ===");
        for (Ligne ligne : reseau.getAllLignes()) {
            System.out.println("Line " + ligne.getID() + " - " + ligne.getNom());
            StringBuilder sb = new StringBuilder();
            for (Station station : ligne.getListeStations()) {
                // station label
                sb.append("[").append(station.getId()).append("]");
                // vehicles at this station
                String vehiclesAt = "";
                for (Vehicle v : ligne.getVehicles()) {
                    try {
                        if (station.containsVehicle(v)) {
                            Direction d = v.direction();
                            String arrow = d == Direction.FORWARD ? "->" : "<-";
                            if (!vehiclesAt.isEmpty()) vehiclesAt += ",";
                            vehiclesAt += v.id() + arrow;
                        }
                    } catch (Exception ignored) {}
                }
                if (!vehiclesAt.isEmpty()) {
                    sb.append(" ").append(vehiclesAt).append(" ");
                } else {
                    sb.append("    ");
                }
                // station separator
                sb.append(" -- ");
            }
            // trim trailing separator
            String lineStr = sb.toString();
            if (lineStr.endsWith(" -- ")) lineStr = lineStr.substring(0, lineStr.length()-4);
            System.out.println(lineStr);
        }
        System.out.println("========================\n");
    }
}
