package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Project.ProjectFactory;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.utils.bean.GitBean;
import it.isw2.flaviosimonelli.utils.bean.JiraBean;
import it.isw2.flaviosimonelli.utils.dao.impl.GitService;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;

import java.util.ArrayList;
import java.util.List;


public class CreateCSVController {

    public boolean createProject(JiraBean jiraBean, GitBean gitBean) {
        ProjectFactory projectFactory = ProjectFactory.getInstance();
        // Creazione del progetto tramite il factory
        Project project = projectFactory.CreateProject(gitBean.isLocalRepository(), jiraBean.getJiraId(), gitBean.getRemoteUrl(), gitBean.getBranch(), gitBean.getPath(), gitBean.getReleaseTagFormat());
        // initialization versions and tickets
        initializeVersions(project);
        initializeTickets(project);
        initializeMethods(project);
        printCSV();
        return true;
    }

    private void initializeVersions(Project project) {
        // contatta il servizio Jira per ottenere le versioni del progetto
        JiraService jiraService = new JiraService();
        GitService gitService = new GitService();
        List<Version> versions;
        try {
            versions = jiraService.getVersions(project);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
        // Filtra le versioni per rimuovere quelle che non sono rilasciate come tag in git
        List<Version> validVersions = new ArrayList<>();
        for (Version version : versions) {
            String hashCommit = gitService.getCommitByVersion(project, version);
            if (hashCommit == null) {
                // Se il tag non esiste, rimuove la versione dalla lista
                System.out.println("Versione " + version.getName() + " non trovata nel repository Git. Rimuovendo dalla lista.");
            } else {
                // Imposta l'hash del commit per la versione
                version.setHashCommit(hashCommit);
                validVersions.add(version);
            }
        }
        project.setVersions(validVersions);
    }

    private void initializeTickets(Project project) {
        // contatta il servizio Jira per ottenere i ticket del progetto
        JiraService jiraService = new JiraService();
        try {
            project.setTickets(jiraService.getFixedBugTickets(project));
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeMethods(Project project) {
        // contatta il servizio Git per ottenere i metodi del progetto
        GitService gitService = new GitService();
        for (Version version : project.getVersions()) {
            try {
                version.setMethods(gitService.getMethodsInVersion(project, version));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean printCSV() {
        ProjectFactory projectFactory = ProjectFactory.getInstance();
        Project project = projectFactory.getProject();
        // esportazione dei ticket in CSV
        CsvExporter.writeTicketsToCsv(project.getTickets(), "result/tickets_"+project.getName()+".csv");
        // esportazione delle versioni in CSV
        CsvExporter.writeVersionsToCsv(project.getVersions(), "result/version_"+project.getName()+".csv");
        // esportazione dei metodi in CSV
        CsvExporter.writeMethodsToCsv(project, "result/methods_"+project.getName()+".csv");

        return true;
    }

}
