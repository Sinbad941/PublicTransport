import Architecture.*;
import java.util.ArrayList;
import java.util.List;

public class TxtNetworkLoaderTest {
    public static void main(String[] args) throws Exception {
        // Test 1: Parse a simple network configuration
        List<String> lines = new ArrayList<>();
        lines.add("LIGNE LIGNE_A");
        lines.add("LIGNE LIGNE_B");
        lines.add("");
        lines.add("STATION S1 LIGNE_A");
        lines.add("STATION S2 LIGNE_A");
        lines.add("STATION S3 LIGNE_B");
        lines.add("");
        lines.add("BUS BUS1 LIGNE_A");
        lines.add("METRO METRO1 LIGNE_B");

        ReseauTransportImpl reseau = TxtNetworkLoader.parse(lines);

        // Verify lines were created
        if (reseau.getAllLignes().size() != 2) {
            throw new RuntimeException("Expected 2 lines, got " + reseau.getAllLignes().size());
        }

        // Verify stations were created and each line has stations
        boolean ligne_a_has_stations = reseau.getAllLignes().stream()
            .anyMatch(l -> l.getNom().contains("LIGNE_A") && l.getListeStations().size() >= 2);
        if (!ligne_a_has_stations) throw new RuntimeException("LIGNE_A should have at least 2 stations");

        boolean ligne_b_has_stations = reseau.getAllLignes().stream()
            .anyMatch(l -> l.getNom().contains("LIGNE_B") && l.getListeStations().size() >= 1);
        if (!ligne_b_has_stations) throw new RuntimeException("LIGNE_B should have at least 1 station");

        // Verify vehicles were created - just check that we have the right number
        long busCount = reseau.getAllLignes().stream()
            .flatMap(l -> l.getVehicles().stream())
            .filter(v -> v.type().name().contains("BUS"))
            .count();
        if (busCount < 1) throw new RuntimeException("At least one BUS should be created");

        long metroCount = reseau.getAllLignes().stream()
            .flatMap(l -> l.getVehicles().stream())
            .filter(v -> v.type().name().contains("METRO"))
            .count();
        if (metroCount < 1) throw new RuntimeException("At least one METRO should be created");

        // Test 2: Empty configuration
        List<String> emptyLines = new ArrayList<>();
        ReseauTransportImpl emptyReseau = TxtNetworkLoader.parse(emptyLines);
        if (emptyReseau.getAllLignes().size() != 0) {
            throw new RuntimeException("Empty config should produce empty network");
        }

        // Test 3: Lines with comments (empty lines)
        List<String> commentedLines = new ArrayList<>();
        commentedLines.add("LIGNE L1");
        commentedLines.add("");
        commentedLines.add("  ");
        commentedLines.add("STATION ST1 L1");
        ReseauTransportImpl commented = TxtNetworkLoader.parse(commentedLines);
        if (commented.getAllLignes().size() != 1) {
            throw new RuntimeException("Comments should be ignored");
        }

        System.out.println("PASS: TxtNetworkLoaderTest");
    }
}
