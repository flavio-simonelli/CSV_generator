package it.isw2.flaviosimonelli.model.Project;

import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.utils.VersionComparator;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;
import org.eclipse.jgit.revwalk.DepthWalk;

import java.util.List;

/**
 * ProjectRepository è una classe che si ispira al pattern Repository ma implementa una singola istanza di progetto. è possibile in futuro implementare un dizionario per la ricerca di progetti tramite ID
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
    public Project createProject(String JiraID, String versionManagerURL, String branchName, String directory) {
        if (project != null){
            return project;
        } else {
            project = new Project();
            project.setJiraID(JiraID);
            project.setVersionManagerURL(versionManagerURL);
            project.setBranchName(branchName);
            project.setDirectory(directory);
            // Chiamata al dato per riceve informazioni sul progetto
            project.setVersions(getVersions());
            project.setTicketsFixClosed(getTickets());

        }
        return project;
    }

    // Il metodo di creazione del progetto tramite apertura del repository Git (se utilizzassimo più progetti dovremmo sostituire nell if una ricerca tramite ID)
    public Project createProject(String JiraID, String directory) {
        if (project != null){
            return project;
        } else {
            project = new Project();
            project.setDirectory(directory);
            project.setJiraID(JiraID);
            // Chiamata al dato per riceve informazioni sul progetto
            project.setVersions(getVersions());
            project.setTicketsFixClosed(getTickets());


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
        List<Version> versions = null;
        try {
            versions = jiraService.getVersionProject(project);
            versions.sort(new VersionComparator());
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return versions;
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

    // Metodo per estrarre i commit in Git
    private List<DepthWalk.Commit> getCommits() {

    }
}
