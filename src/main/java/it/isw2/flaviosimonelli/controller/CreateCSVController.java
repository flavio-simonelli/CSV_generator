package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.model.Project.ProjectRepository;
import it.isw2.flaviosimonelli.utils.VersionComparator;
import it.isw2.flaviosimonelli.utils.bean.VersionManagerBean;
import it.isw2.flaviosimonelli.utils.dao.impl.GitService;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;
import it.isw2.flaviosimonelli.utils.bean.TicketManagerBean;

import java.util.List;


public class CreateCSVController {

    public boolean createProject(TicketManagerBean ticketManagerBean, VersionManagerBean versionManagerBean) {
        // creazione del progetto model
        ProjectRepository projectRepository = ProjectRepository.getInstance();
        Project project = null;
        if (versionManagerBean.getChoiceCloneOpen() == "open") {
            // creazione del progetto da repository esistente
            project = projectRepository.createProject(ticketManagerBean.getJiraID(), versionManagerBean.getDirectory());
        } else {
            // creazione del progetto da repository da clonare
            project = projectRepository.createProject(ticketManagerBean.getJiraID(), versionManagerBean.getURL(), versionManagerBean.getBranch(), versionManagerBean.getDirectory());
        }
        if (project == null) {
            System.err.println("Errore nella creazione del progetto.");
            return false;
        }
        // esportazione dei ticket in CSV
        CsvExporter.writeTicketsToCsv(project.getTicketsFixClosed(), "tickets.csv");
        // esportazione delle versioni in CSV
        CsvExporter.writeVersionsToCsv(project.getVersions(), "versions.csv");

        return true;
    }

}
