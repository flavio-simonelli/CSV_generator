package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.model.Project.ProjectRepository;
import it.isw2.flaviosimonelli.utils.bean.GitBean;
import it.isw2.flaviosimonelli.utils.bean.JiraBean;


public class CreateCSVController {

    public boolean createProject(JiraBean jiraBean, GitBean gitBean) {
        // creazione del progetto model
        ProjectRepository projectRepository = ProjectRepository.getInstance();
        // decidiamo se clonare il repository o aprirlo
        if (gitBean.getLocalPath() != null && !gitBean.getLocalPath().isEmpty()) {
            // apertura del repository nella directory locale
            projectRepository.openProject(jiraBean.getJiraId(), gitBean.getLocalPath(), gitBean.getReleaseTagFormat());
        } else {
            // clonazione del repository nella directory locale
            projectRepository.cloneProject(jiraBean.getJiraId(), gitBean.getRemoteUrl(), gitBean.getBranch(), gitBean.getParentDirectory(), gitBean.getReleaseTagFormat());
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
        CsvExporter.writeMethodsToCsv(project, "result/methods_"+project.getName()+".csv");

        return true;
    }

}
