package it.isw2.flaviosimonelli.model.Project;

import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.VersionComparator;
import it.isw2.flaviosimonelli.utils.dao.impl.GitService;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * ProjectRepository è una classe che si ispira al pattern Repository ma implementa una singola istanza di progetto. È possibile in futuro implementare un dizionario per la ricerca di progetti tramite ID
 */

public class ProjectRepository {
    private static ProjectRepository instance; // Singleton instance of ProjectFactory
    private Project project; // Singleton instance of Project

    // Costruttore privato per evitare istanziazione esterna
    private ProjectRepository() {
    }

    // Metodo per ottenere l'istanza singleton della factory
    public static ProjectRepository getInstance() {
        if (instance == null) {
            instance = new ProjectRepository();
        }
        return instance;
    }


    // Il metodo di creazione del progetto tramite clone del repository Git (se utilizzassimo più progetti dovremmo sostituire nell if una ricerca tramite ID)
    public Project cloneProject(String JiraID, String gitHubURL, String branch, String parentDirectory, String conventionReleaseTag) {
        project = new Project();
        String projectName = extractGitHubProjectName(gitHubURL);
        if (projectName == null) {
            System.err.println("Errore: URL GitHub non valido o non riconosciuto.");
            // TODO: lancia una eccezione
            return null;
        } else {
            project.setName(projectName);
        }
        project.setJiraID(JiraID);
        project.setGitURL(gitHubURL);
        project.setGitBranch(branch);
        project.setGitDirectory(parentDirectory+ "/" + projectName);
        project.setConventionReleaseTag(conventionReleaseTag);
        // Clone del repository Git
        GitService gitService = new GitService();
        gitService.cloneRepository(project);
        // Chiamata al dato per riceve informazioni sul progetto
        project.setVersions(getVersions());
        project.setTickets(getTickets());

        return project;
    }

    // Il metodo di creazione del progetto tramite apertura del repository Git (se utilizzassimo più progetti dovremmo sostituire nell if una ricerca tramite ID)
    public Project openProject(String JiraID, String directory, String conventionReleaseTag) {
        project = new Project();
        project.setName(extractDirectoryName(directory));
        project.setGitDirectory(directory);
        project.setJiraID(JiraID);
        project.setConventionReleaseTag(conventionReleaseTag);
        // Apertura del repostory Git
        GitService gitService = new GitService();
        gitService.openRepository(project);
        // Chiamata al dato per riceve informazioni sul progetto
        project.setVersions(getVersions());
        project.setTickets(getTickets());
        // ricava i metodi
        for (Version version : project.getVersions()) {
            try {
                version.setMethods(gitService.getMethodsInCommit(project.getGitDirectory(), version.getHashCommit(), version.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return project;
    }

    // Metodo per ottenere l'istanza del progetto tramite open
    public Project getProject() {
        return project;
    }

    // Metodo per eliminare il progetto
    public void deleteProject() {
        if (project != null) {
            project = null;
        }
    }

    // Metodo per estrarre le versioni in JIra
    private List<Version> getVersions() {
        JiraService jiraService = new JiraService();
        GitService gitService = new GitService();
        List<Version> versions = null;
        List<Version> validVersions = new ArrayList<Version>();
        try {
            versions = jiraService.getVersionProject(project);
            for (Version version : versions) {
                // Estrazione del commit corrispondente alla versione
                String hashCommit = gitService.getCommitByVersion(project.getGitDirectory(), project.getConventionReleaseTag(), version.getName());
                if (hashCommit == null) {
                    // Se il tag non esiste, rimuove la versione dalla lista
                    System.out.println("Versione " + version.getName() + " non trovata nel repository Git. Rimuovendo dalla lista.");
                } else {
                    // Imposta l'hash del commit per la versione
                    version.setHashCommit(hashCommit);
                    validVersions.add(version);
                }
            }
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return validVersions;
    }

    // Metodo per estrarre i ticket in Jira
    private List<Ticket> getTickets() {
        JiraService jiraService = new JiraService();
        List<Ticket> tickets = null;
        try {
            tickets = jiraService.getFixedBugTickets(project);
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    // Metodo per estrarre il nome del progetto da url github
    private static String extractGitHubProjectName(String url) {
        if (url == null) return null;
        // Rimuove .git finale se presente
        url = url.replaceAll("\\.git$", "");

        // Gestisce URL HTTPS, es: https://github.com/user/repo
        if (url.startsWith("https://github.com/") || url.startsWith("http://github.com/")) {
            String[] parts = url.split("/");
            if (parts.length >= 5) {
                return parts[4];
            }
        }

        // Gestisce URL SSH, es: git@github.com:user/repo.git
        if (url.startsWith("git@github.com:")) {
            String[] parts = url.split(":")[1].split("/");
            if (parts.length == 2) {
                return parts[1];
            }
        }
        return null; // URL non valido o non riconosciuto
    }

    // Metodo per ottenere il nome del progetto dal path
    private static String extractDirectoryName(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) return null;

        Path path = Paths.get(pathStr);
        Path fileName = path.getFileName();

        return fileName != null ? fileName.toString() : null;
    }


}
