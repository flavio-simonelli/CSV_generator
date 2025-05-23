import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import it.isw2.flaviosimonelli.controller.CreateCSVController;
import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Ticket;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CreateCSVControllerTest {

    @Test
    public void testFilterTickets() {
        // Prepara il test
        CreateCSVController controller = new CreateCSVController();
        Project project = new Project();

        // Prepara le release valide
        List<String> releases = Arrays.asList("1.0", "2.0", "3.0", "4.0", "5.0");
        project.setVersions(releases);

        // Crea ticket di test
        List<Ticket> tickets = new ArrayList<>();

        // Caso 1: Ticket con fixVersion invalida (non in releases)
        Ticket ticket1 = new Ticket();
        ticket1.setKey("TICKET-1");
        ticket1.setFixVersions(Arrays.asList("6.0")); // Non in releases
        ticket1.setAffectedVersions(Arrays.asList("1.0"));
        tickets.add(ticket1);

        // Caso 2: Ticket con più di una fixVersion
        Ticket ticket2 = new Ticket();
        ticket2.setKey("TICKET-2");
        ticket2.setFixVersions(Arrays.asList("2.0", "3.0")); // Multiple fixVersions
        ticket2.setAffectedVersions(Arrays.asList("1.0"));
        tickets.add(ticket2);

        // Caso 3: Ticket con affected version >= fixed version
        Ticket ticket3 = new Ticket();
        ticket3.setKey("TICKET-3");
        ticket3.setFixVersions(Arrays.asList("3.0"));
        ticket3.setAffectedVersions(Arrays.asList("4.0")); // > fixed version
        tickets.add(ticket3);

        // Caso 4: Ticket che dovrebbe passare il filtro
        Ticket ticket4 = new Ticket();
        ticket4.setKey("TICKET-4");
        ticket4.setFixVersions(Arrays.asList("3.0"));
        ticket4.setAffectedVersions(Arrays.asList("1.0", "2.0")); // < fixed version
        tickets.add(ticket4);

        // Imposta i ticket nel progetto
        project.setTicketsFixClosed(tickets);

        // Esegui il filtro
        boolean result = controller.filterTickets(project);

        // Verifica i risultati
        assertTrue(result);
        assertEquals(1, project.getTicketsFixClosed().size());
        assertEquals("TICKET-4", project.getTicketsFixClosed().get(0).getKey());
    }
}