package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project;
import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;
import it.isw2.flaviosimonelli.utils.bean.ProjectBean;

import java.util.ArrayList;
import java.util.List;


public class CreateCSVController {

    public boolean getJIRATickets(ProjectBean projectBean) {
        Project project = new Project();
        project.setJiraID(projectBean.getProjectName());
        JiraService jiraService = new JiraService();

        try {
            // Recupera i dati e salvali nel progetto
            List<Version> versions = jiraService.getVersionProject(project);
            project.setVersions(versions);

            List<Ticket> tickets = jiraService.getFixedBugTickets(project);
            project.setTicketsFixClosed(tickets);

            // Applica il filtro
            return filterTickets(project);
        } catch (SystemException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Filtra i ticket secondo i seguenti criteri:
     * 1. Rimuove le versioni in fixed version e affected version non release
     * 2. Rimuove i ticket con fixed version non presente
     * 3. Rimuove i ticket con più di una fixed version
     * 4. Rimuove i ticket con affected version >= fixed version
     *
     * @param project Il progetto con i ticket da filtrare
     * @return True se il filtraggio è stato completato con successo
     */
    public boolean filterTickets(Project project) {
        // Usa i dati già recuperati dal metodo getJIRATickets
        List<Version> releases = project.getVersions();
        List<Ticket> tickets = project.getTicketsFixClosed();

        if (tickets == null || tickets.isEmpty() || releases == null || releases.isEmpty()) {
            System.out.println("Nessun ticket o release trovati.");
            return false;
        }

        System.out.println("Ticket prima del filtraggio: " + tickets.size());

        List<Ticket> filteredTickets = new ArrayList<>();

        for (Ticket ticket : tickets) {
            // Copia le fixVersions per evitare ConcurrentModificationException
            List<String> fixVersions = new ArrayList<>(ticket.getFixVersions());
            List<String> validFixVersions = new ArrayList<>();

            for (String fixVersion : fixVersions) {
                if (releases.contains(fixVersion)) {
                    validFixVersions.add(fixVersion);
                }
            }

            // Aggiorna le fixVersions del ticket
            ticket.setFixVersions(validFixVersions);

            // Controlla se la fixVersion è presente dopo il filtraggio
            if (validFixVersions.isEmpty()) {
                continue;
            }

            // Controlla se ci sono più di una fixVersion
            if (validFixVersions.size() > 1) {
                continue;
            }

            // Controlla se almeno una affected version è >= alla fixVersion
            boolean skipTicket = false;
            for (String affectedVersion : ticket.getAffectedVersions()) {
                if (releases.contains(affectedVersion) &&
                        releases.indexOf(affectedVersion) >= releases.indexOf(validFixVersions.get(0))) {
                    skipTicket = true;
                    break;
                }
            }

            if (!skipTicket) {
                filteredTickets.add(ticket);
            }
        }

        // Aggiorna i ticket nel progetto
        project.setTicketsFixClosed(filteredTickets);
        System.out.println("Ticket dopo il filtraggio: " + filteredTickets.size());

        return true;
    }

}
