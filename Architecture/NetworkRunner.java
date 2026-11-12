package Architecture;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import Incidents.Incident;
import Incidents.IncidentsTypes;
import Passager.Passenger;
import Vehicule.Bus;
import Vehicule.Direction;
import Vehicule.Metro;
import Vehicule.Tram;
import Vehicule.Vehicle;
import Vehicule.VehicleType;

public class NetworkRunner {
    public static void main(String[] args) throws Exception {
        ReseauTransportImpl reseau;
        boolean startEmpty = args.length > 0 && "--empty".equalsIgnoreCase(args[0]);
        boolean startFromFile = args.length > 1 && "--file".equalsIgnoreCase(args[0]);

        if (startFromFile) {
            String filePath = args[1];
            reseau = TxtNetworkLoader.load(filePath);
            System.out.println("Network loaded from file: " + filePath + " (lines=" + reseau.getAllLignes().size() + ")");
        } else {
            reseau = new ReseauTransportImpl();
            if (!startEmpty) {
                initializeDefaultScenario(reseau);
            } else {
                System.out.println("Empty simulation mode enabled (--empty). No lines/stations/vehicles loaded.");
            }
        }

        printHelp();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            System.out.print("cmd> ");
            while ((line = in.readLine()) != null) {
                String input = line.trim();
                if (input.isEmpty()) {
                    System.out.print("cmd> ");
                    continue;
                }
                String[] parts = input.split("\\s+");
                String cmd = parts[0].toLowerCase();
                switch (cmd) {
                    case "start":
                        reseau.start();
                        System.out.println("Network started.");
                        break;
                    case "pause":
                        reseau.pause();
                        System.out.println("Network paused.");
                        break;
                    case "resume":
                        reseau.resume();
                        System.out.println("Network resumed.");
                        break;
                    case "tick":
                        reseau.tick();
                        ConsoleRenderer.render(reseau);
                        break;
                    case "render":
                    case "status":
                        ConsoleRenderer.render(reseau);
                        System.out.println("Running=" + reseau.isRunning() + " Paused=" + reseau.isPaused());
                        break;
                    case "stop":
                        reseau.stop();
                        System.out.println("Network stopped.");
                        break;
                    case "slow":
                        reseau.setSpeedFactor(0.1);
                        reseau.setVerboseLogging(true);
                        System.out.println("Speed: SLOW (10x slower) - verbose logging ON");
                        break;
                    case "fast":
                        reseau.setSpeedFactor(10.0);
                        reseau.setVerboseLogging(true);
                        System.out.println("Speed: FAST (10x faster) - verbose logging ON");
                        break;
                    case "normal":
                        reseau.setSpeedFactor(1.0);
                        reseau.setVerboseLogging(true);
                        System.out.println("Speed: NORMAL (1x) - verbose logging ON");
                        break;
                    case "addline":
                        if (parts.length < 4) {
                            System.out.println("Usage: addline <lineId> <lineName> <BUS|METRO|TRAMWAY>");
                            break;
                        }
                        try {
                            String lineId = parts[1];
                            String lineName = parts[2].replace('_', ' ');
                            VehicleType type = parseVehicleType(parts[3]);
                            if (reseau.getLigne(lineId) != null) {
                                System.out.println("Line already exists: " + lineId);
                                break;
                            }
                            LigneImpl newLine = new LigneImpl(lineId, lineName, type);
                            boolean added = reseau.addLigne(newLine);
                            System.out.println(added
                                ? "Line added: " + lineId + " (" + lineName + ", " + type + ")"
                                : "Failed to add line: " + lineId);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid line type. Use BUS, METRO, or TRAMWAY.");
                        }
                        break;
                    case "addstation":
                        if (parts.length < 6) {
                            System.out.println("Usage: addstation <lineId> <stationId> <stationName> <x> <y> [maxWaiting=10] [maxVehicles=2]");
                            break;
                        }
                        try {
                            String lineId = parts[1];
                            Ligne targetLine = reseau.getLigne(lineId);
                            if (targetLine == null) {
                                System.out.println("Line not found: " + lineId);
                                break;
                            }
                            String stationId = parts[2];
                            String stationName = parts[3].replace('_', ' ');
                            int x = Integer.parseInt(parts[4]);
                            int y = Integer.parseInt(parts[5]);
                            int maxWaiting = parts.length >= 7 ? Integer.parseInt(parts[6]) : 10;
                            int maxVehicles = parts.length >= 8 ? Integer.parseInt(parts[7]) : 2;

                            StationImpl station = new StationImpl(stationId, stationName, new Coordinate(x, y), maxWaiting, maxVehicles);
                            boolean added = targetLine.addStation(station);
                            System.out.println(added
                                ? "Station added to " + lineId + ": " + stationId + " (" + stationName + ")"
                                : "Failed to add station: " + stationId);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number format. x/y/maxWaiting/maxVehicles must be integers.");
                        }
                        break;
                    case "addvehicle":
                        if (parts.length < 5) {
                            System.out.println("Usage: addvehicle <lineId> <BUS|METRO|TRAMWAY> <vehicleId> <capacity> [speed=30.0]");
                            break;
                        }
                        try {
                            String lineId = parts[1];
                            Ligne targetLine = reseau.getLigne(lineId);
                            if (targetLine == null) {
                                System.out.println("Line not found: " + lineId);
                                break;
                            }

                            VehicleType vType = parseVehicleType(parts[2]);
                            String vehicleId = parts[3];
                            int capacity = Integer.parseInt(parts[4]);
                            double speed = parts.length >= 6 ? Double.parseDouble(parts[5]) : 30.0;

                            Vehicle vehicle = createVehicle(vType, vehicleId, capacity, speed);
                            boolean added = reseau.addVehicleToLine(targetLine, vehicle);
                            System.out.println(added
                                ? "Vehicle added: " + vehicleId + " on line " + lineId
                                : "Failed to add vehicle: " + vehicleId);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number format. capacity must be int and speed must be decimal.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid vehicle type. Use BUS, METRO, or TRAMWAY.");
                        }
                        break;
                    case "empty":
                        reseau.stop();
                        reseau = new ReseauTransportImpl();
                        System.out.println("Simulation reset to EMPTY network.");
                        break;
                    case "incident-vehicle":
                        // Usage: incident-vehicle <vehicleId> <RETARD|MALAISEPASSAGER|PERSONNESURVOIE> <seconds>
                        if (parts.length < 4) {
                            System.out.println("Usage: incident-vehicle <vehicleId> <RETARD|MALAISEPASSAGER|PERSONNESURVOIE> <seconds>");
                            break;
                        }
                        try {
                            String vid = parts[1];
                            IncidentsTypes iType = IncidentsTypes.valueOf(parts[2].toUpperCase());
                            long secs = Long.parseLong(parts[3]);
                            boolean ok = reseau.reportIncidentToVehicle(vid, new Incident(iType, Duration.ofSeconds(secs)));
                            if (!ok) System.out.println("Vehicle not found: " + vid);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid type. Use: RETARD, MALAISEPASSAGER, PERSONNESURVOIE");
                        }
                        break;
                    case "incident-station":
                        // Usage: incident-station <stationId> <RETARD|MALAISEPASSAGER|PERSONNESURVOIE> <seconds>
                        if (parts.length < 4) {
                            System.out.println("Usage: incident-station <stationId> <RETARD|MALAISEPASSAGER|PERSONNESURVOIE> <seconds>");
                            break;
                        }
                        try {
                            String sid = parts[1];
                            IncidentsTypes iType = IncidentsTypes.valueOf(parts[2].toUpperCase());
                            long secs = Long.parseLong(parts[3]);
                            boolean ok = reseau.reportIncidentToStation(sid, new Incident(iType, Duration.ofSeconds(secs)));
                            if (!ok) System.out.println("Station not found: " + sid);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid type. Use: RETARD, MALAISEPASSAGER, PERSONNESURVOIE");
                        }
                        break;
                    case "incident-line":
                        // Usage: incident-line <lineId> <RETARD|MALAISEPASSAGER|PERSONNESURVOIE> <seconds>
                        if (parts.length < 4) {
                            System.out.println("Usage: incident-line <lineId> <RETARD|MALAISEPASSAGER|PERSONNESURVOIE> <seconds>");
                            break;
                        }
                        try {
                            String lid = parts[1];
                            IncidentsTypes iType = IncidentsTypes.valueOf(parts[2].toUpperCase());
                            long secs = Long.parseLong(parts[3]);
                            boolean ok = reseau.reportIncidentToLine(lid, new Incident(iType, Duration.ofSeconds(secs)));
                            if (!ok) System.out.println("Line not found: " + lid);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid type. Use: RETARD, MALAISEPASSAGER, PERSONNESURVOIE");
                        }
                        break;
                    case "loadfile":
                        String filePath = extractCommandArgument(input);
                        if (filePath == null || filePath.isBlank()) {
                            System.out.println("Usage: loadfile <path-to-txt>");
                            break;
                        }
                        try {
                            reseau.stop();
                            reseau = TxtNetworkLoader.load(filePath);
                            System.out.println("Network loaded from file: " + filePath + " (lines=" + reseau.getAllLignes().size() + ")");
                        } catch (Exception e) {
                            System.out.println("Failed to load file: " + e.getMessage());
                        }
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        reseau.stop();
                        System.out.println("Exiting.");
                        return;
                    default:
                        if (!cmd.isEmpty()) System.out.println("Unknown command: " + cmd);
                }
                System.out.print("cmd> ");
            }
        }
    }

    private static void initializeDefaultScenario(ReseauTransportImpl reseau) {
        LigneImpl ligne = new LigneImpl("L1", "Ligne 1", VehicleType.BUS);
        StationImpl s1 = new StationImpl("s1", "S1", new Coordinate(0, 0), 10, 2);
        StationImpl s2 = new StationImpl("s2", "S2", new Coordinate(1, 0), 10, 2);
        StationImpl s3 = new StationImpl("s3", "S3", new Coordinate(2, 0), 10, 2);

        ligne.addStation(s1);
        ligne.addStation(s2);
        ligne.addStation(s3);

        reseau.addLigne(ligne);

        List<Passenger> empty = new ArrayList<>();
        Bus b1 = new Bus("B1", 10, 30.0, Direction.FORWARD, empty);
        Bus b2 = new Bus("B2", 10, 30.0, Direction.FORWARD, empty);

        reseau.addVehicleToLine(ligne, b1);
        reseau.addVehicleToLine(ligne, b2);

        Passenger p = new Passenger("P1", s2, s3);
        s2.addWaitingPassenger(p);
    }

    private static VehicleType parseVehicleType(String raw) {
        String v = raw.toUpperCase();
        if ("TRAM".equals(v)) return VehicleType.TRAMWAY;
        return VehicleType.valueOf(v);
    }

    private static String extractCommandArgument(String input) {
        int firstSpace = input.indexOf(' ');
        if (firstSpace < 0) {
            return "";
        }
        return input.substring(firstSpace + 1).trim();
    }

    private static Vehicle createVehicle(VehicleType type, String id, int capacity, double speed) {
        List<Passenger> empty = new ArrayList<>();
        return switch (type) {
            case BUS -> new Bus(id, capacity, speed, Direction.FORWARD, empty);
            case METRO -> new Metro(id, capacity, speed, Direction.FORWARD, empty);
            case TRAMWAY -> new Tram(id, capacity, speed, Direction.FORWARD, empty);
        };
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  start   - start the network (starts vehicle workers)");
        System.out.println("  pause   - pause the network workers");
        System.out.println("  resume  - resume the network workers");
        System.out.println("  tick    - advance one tick (single-step)");
        System.out.println("  status  - render the network and show running state");
        System.out.println("  render  - render the network (alias for status)");
        System.out.println("  slow    - slow down 10x (workers sleep longer, logs on)");
        System.out.println("  fast    - speed up 10x (workers sleep less, logs on)");
        System.out.println("  normal  - reset to normal speed (1x, logs on)");
        System.out.println("  addline <id> <name> <type>               - add a line (type: BUS/METRO/TRAMWAY)");
        System.out.println("  addstation <lineId> <id> <name> <x> <y> [maxWaiting] [maxVehicles] - add a station");
        System.out.println("  addvehicle <lineId> <type> <id> <capacity> [speed] - add a vehicle to a line");
        System.out.println("  loadfile <path> - load/reset network from a txt scenario file");
        System.out.println("  empty   - reset to an empty simulation (no preloaded elements)");
        System.out.println("  incident-vehicle <id> <type> <sec> - block vehicle for <sec> seconds");
        System.out.println("  incident-station <id> <type> <sec> - block station for <sec> seconds");
        System.out.println("  incident-line    <id> <type> <sec> - block entire line for <sec> seconds");
        System.out.println("    incident types: RETARD, MALAISEPASSAGER, PERSONNESURVOIE");
        System.out.println("  stop    - stop the network");
        System.out.println("  help    - show this help");
        System.out.println("  exit    - stop and exit the runner");
        System.out.println("Startup option: --empty to launch directly with an empty simulation");
        System.out.println("Startup option: --file <path> to load a txt scenario at launch");
    }
}
