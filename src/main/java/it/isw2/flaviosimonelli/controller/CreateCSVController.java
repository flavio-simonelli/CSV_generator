package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project;
import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.utils.VersionComparator;
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
            versions.sort(new VersionComparator());
            project.setVersions(versions);

            List<Ticket> tickets = jiraService.getFixedBugTickets(project);
            project.setTicketsFixClosed(tickets);

            // Stampa le versioni in CSV
            String csvVersionFilePath = "versions.csv";
            CsvExporter.writeVersionsToCsv(versions, csvVersionFilePath);

            // Stampa i ticket in CSV
            String csvTicketFilePath = "tickets.csv";
            CsvExporter.writeTicketsToCsv(tickets, csvTicketFilePath);


            // Applica il filtro
            return true;
        } catch (SystemException e) {
            e.printStackTrace();
            return false;
        }
    }

}
