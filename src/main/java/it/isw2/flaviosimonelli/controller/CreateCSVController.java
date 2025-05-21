package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project;
import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.utils.VersionComparator;
import it.isw2.flaviosimonelli.utils.bean.VersionManagerBean;
import it.isw2.flaviosimonelli.utils.dao.impl.GitService;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;
import it.isw2.flaviosimonelli.utils.bean.TicketManagerBean;

import java.util.List;


public class CreateCSVController {

    public boolean getTickets(TicketManagerBean ticketManagerBean) {
        Project project = new Project();
        project.setJiraID(ticketManagerBean.getJiraID());
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
    
    public boolean getVersions(VersionManagerBean versionManagerBean) {
        // Clonazione o apertura del repository Git
        if (versionManagerBean.getChoiceCloneOpen() == 1) {
            // Clonazione del repository
            String url = versionManagerBean.getURL();
            String projectName = url.substring(url.lastIndexOf('/') + 1).replace(".git", "");;
            String projectDirectory = versionManagerBean.getDirectory() + "/" + projectName;
            // Creazione progetto model
            Project project = new Project();
            project.setVersionManagerURL(url);
            project.setDirectory(projectDirectory);
            project.setBranchName(versionManagerBean.getBranch());
            // Clonazione del repository
            GitService gitService = new GitService();
            gitService.cloneRepository(project);
        } else {
            // Apertura del repository esistente
            String projectDirectory = versionManagerBean.getDirectory();
            Project project = new Project();
            project.setDirectory(projectDirectory);
            GitService gitService = new GitService();
            gitService.openRepository(project);
        }

        return true;
    }

}
