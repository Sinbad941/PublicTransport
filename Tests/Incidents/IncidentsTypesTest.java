import Incidents.*;
public class IncidentsTypesTest {
    public static void main(String[] args) throws Exception {
        // Test that all enum values exist
        if (IncidentsTypes.RETARD == null) throw new RuntimeException("RETARD should exist");
        if (IncidentsTypes.MALAISEPASSAGER == null) throw new RuntimeException("MALAISEPASSAGER should exist");
        if (IncidentsTypes.PERSONNESURVOIE == null) throw new RuntimeException("PERSONNESURVOIE should exist");

        // Test enum values
        IncidentsTypes[] values = IncidentsTypes.values();
        if (values.length != 3) throw new RuntimeException("Should have exactly 3 incident types, got " + values.length);

        // Test valueOf
        IncidentsTypes retard = IncidentsTypes.valueOf("RETARD");
        if (retard != IncidentsTypes.RETARD) throw new RuntimeException("valueOf RETARD failed");

        IncidentsTypes malaisePassager = IncidentsTypes.valueOf("MALAISEPASSAGER");
        if (malaisePassager != IncidentsTypes.MALAISEPASSAGER) throw new RuntimeException("valueOf MALAISEPASSAGER failed");

        IncidentsTypes personneSurVoie = IncidentsTypes.valueOf("PERSONNESURVOIE");
        if (personneSurVoie != IncidentsTypes.PERSONNESURVOIE) throw new RuntimeException("valueOf PERSONNESURVOIE failed");

        // Test toString
        if (!IncidentsTypes.RETARD.toString().equals("RETARD")) throw new RuntimeException("toString failed");
        if (!IncidentsTypes.MALAISEPASSAGER.toString().equals("MALAISEPASSAGER")) throw new RuntimeException("toString failed");
        if (!IncidentsTypes.PERSONNESURVOIE.toString().equals("PERSONNESURVOIE")) throw new RuntimeException("toString failed");

        System.out.println("PASS: IncidentsTypesTest");
    }
}
