import Passager.*;
public class EtatTest {
    public static void main(String[] args) throws Exception {
        // Test that all enum values exist
        if (Etat.ABORD == null) throw new RuntimeException("ABORD should exist");
        if (Etat.MONTEE == null) throw new RuntimeException("MONTEE should exist");
        if (Etat.DESCENTE == null) throw new RuntimeException("DESCENTE should exist");
        if (Etat.ATTENTE == null) throw new RuntimeException("ATTENTE should exist");

        // Test enum values
        Etat[] values = Etat.values();
        if (values.length != 4) throw new RuntimeException("Should have exactly 4 Etat values, got " + values.length);

        // Test valueOf
        Etat abord = Etat.valueOf("ABORD");
        if (abord != Etat.ABORD) throw new RuntimeException("valueOf ABORD failed");

        Etat montee = Etat.valueOf("MONTEE");
        if (montee != Etat.MONTEE) throw new RuntimeException("valueOf MONTEE failed");

        Etat descente = Etat.valueOf("DESCENTE");
        if (descente != Etat.DESCENTE) throw new RuntimeException("valueOf DESCENTE failed");

        Etat attente = Etat.valueOf("ATTENTE");
        if (attente != Etat.ATTENTE) throw new RuntimeException("valueOf ATTENTE failed");

        // Test toString
        if (!Etat.ABORD.toString().equals("ABORD")) throw new RuntimeException("toString ABORD failed");
        if (!Etat.MONTEE.toString().equals("MONTEE")) throw new RuntimeException("toString MONTEE failed");
        if (!Etat.DESCENTE.toString().equals("DESCENTE")) throw new RuntimeException("toString DESCENTE failed");
        if (!Etat.ATTENTE.toString().equals("ATTENTE")) throw new RuntimeException("toString ATTENTE failed");

        System.out.println("PASS: EtatTest");
    }
}
