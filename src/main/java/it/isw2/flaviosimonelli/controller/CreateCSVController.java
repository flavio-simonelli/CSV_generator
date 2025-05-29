package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.model.Project.ProjectRepository;
import it.isw2.flaviosimonelli.utils.bean.GitBean;
import it.isw2.flaviosimonelli.utils.bean.JiraBean;
import it.isw2.flaviosimonelli.utils.dao.impl.GitService;

import java.io.IOException;


public class CreateCSVController {

    public boolean createProject(JiraBean jiraBean, GitBean gitBean) {
        // creazione del progetto model
        ProjectRepository projectRepository = ProjectRepository.getInstance();
        // decidiamo se clonare il repository o aprirlo
        if (gitBean.getDirectory() != null){
            // apertura del repository nella directory locale
            projectRepository.openProject(jiraBean.getJiraID(), gitBean.getDirectory(), gitBean.getConventionReleaseTag());
        } else {
            // clonazione del repository nella directory locale
            projectRepository.cloneProject(jiraBean.getJiraID(), gitBean.getURL(), gitBean.getBranch(), gitBean.getParentDirectory(), gitBean.getConventionReleaseTag());
        }
        printCSV();
        return true;
    }

    public boolean printCSV() {
        ProjectRepository projectRepository = ProjectRepository.getInstance();
        Project project = projectRepository.getProject();
        // esportazione dei ticket in CSV
        CsvExporter.writeTicketsToCsv(project.getTickets(), "result/tickets_"+project.getName()+".csv");
        // esportazione delle versioni in CSV
        CsvExporter.writeVersionsToCsv(project.getVersions(), "result/version_"+project.getName()+".csv");
        // esportazione dei metodi in CSV
        try {
            CsvExporter.writeMethodsToCsv(project, "result/methods_"+project.getName()+".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}
