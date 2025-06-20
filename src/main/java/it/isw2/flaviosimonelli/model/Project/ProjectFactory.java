package it.isw2.flaviosimonelli.model.Project;

import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.Comparator.VersionComparator;
import it.isw2.flaviosimonelli.utils.dao.GitService;
import it.isw2.flaviosimonelli.utils.dao.JiraService;
import it.isw2.flaviosimonelli.utils.exception.GitException;
import it.isw2.flaviosimonelli.utils.exception.JiraException;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectFactory {
    private static final Logger LOGGER = Logger.getLogger(ProjectFactory.class.getName());

    private Project project;
    private static ProjectFactory instance; // Singleton instance of ProjectFactory

    // Private constructor to prevent instantiation
    private ProjectFactory() {
    }

    // Method to get the singleton instance of the factory
    public static ProjectFactory getInstance() {
        if (instance == null) {
            instance = new ProjectFactory();
        }
        return instance;
    }

    // Method to create a new istance of Project and clone the git repository
    public void createProject(String jiraID, ApproachProportion approachProportion, String gitURL, String gitBranch, String parentDirectory, String releaseTagFormat) throws GitException, JiraException {
        GitService gitService = new GitService();
        // Crea il path in cui clonare il repository Git
        String gitDirectory = parentDirectory + "/" + extractGitHubProjectName(gitURL);
        // Clone del repository Git
        gitService.cloneRepository(gitURL, gitDirectory, gitBranch); // prova a vedere se il repository esiste veramente
        // Crea entità Project
        Project project = new Project(extractGitHubProjectName(gitURL), jiraID, approachProportion, gitBranch, gitDirectory, releaseTagFormat);
        // Inizializza le versioni del progetto prendendoli da Jira
        getVersion(project);
        // Inizializza i ticket del progetto prendendoli da Jira
        getTicket(project);

        // imposta il progetto corrente
        this.project = project;
    }

    // Metodo che crea un nuovo progetto a partire da un repository Git locale
    public void createProject(String JiraID, ApproachProportion approachProportion, String gitBranch, String gitDirectory, String releaseTagFormat) throws GitException, JiraException {
        GitService gitService = new GitService();
        // Open the local Git repository
        gitService.openRepository(gitDirectory, gitBranch); // prova a vedere se il repository esiste veramente
        // Crea un entità project aprendo un repository Git locale
        Project project = new Project(extractDirectoryName(gitDirectory), JiraID, approachProportion, gitBranch, gitDirectory, releaseTagFormat);
        // Inizializza le versioni del progetto prendendoli da Jira
        getVersion(project);
        // Inizializza i ticket del progetto prendendoli da Jira
        getTicket(project);
        // imposta il progetto corrente
        this.project = project;
    }


    // Get the current project
    public Project getProject() {
        return project;
    }

    // Delete the current project
    public void deleteProject() {
        if (project != null) {
            project = null;
        }
    }

    /**
     * Retrieves and filters versions from Jira that also exist as Git tags.
     *
     * @param project The project to process
     *
     */
    private void getVersion(Project project) throws JiraException, GitException {
        JiraService jiraService = new JiraService();
        // Recupera le versioni del progetto da Jira
        List<Version> versions = jiraService.getVersions(project);
        // Filtra le versioni per mantenere solo quelle che hanno un commit hash associato
        versions = filterVersion(project, versions);
        // ordina le versioni in ordine cronologico
        versions.sort(new VersionComparator());
        // per ogni versione, prendi tutti i metodi presenti in quella versione
        getMethodsforVersion(project, versions);
        // setta le versioni nel progetto
        project.setVersions(versions);
    }

    /**
     * Filters versions to keep only those that have corresponding Git tags.
     *
     * @param project The project being processed
     * @param versions List of versions to filter
     * @return List of valid versions with commit hashes
     */
    private List<Version> filterVersion(Project project, List<Version> versions) throws GitException {
        GitService gitService = new GitService();
        // La lista che conterrà le versioni valide
        List<Version> validVersions = new ArrayList<>();

        for (Version version : versions) {
            // recupera l'hash del commit associato alla versione
            String hashCommit = gitService.getCommitByVersion(project, version);

            if (hashCommit == null) {
                // Se l'hash del commit è null, significa che non esiste un tag Git per questa versione la versione non verra' considerata valida
                LOGGER.warning("Version '" + version.getName() + "' not found in Git repository for project '" + project.getName() + "'. Removing from list.");
            } else {
                // Se l'hash del commit è valido, aggiungi la versione alla lista delle versioni valide
                version.setHashCommit(hashCommit);
                // Imposta la data di rilascio della versione utilizzando la data del commit
                Date commitDate = gitService.getCommitDate(project, hashCommit);
                if (commitDate != null) {
                    version.setReleaseDate(commitDate);
                }
                validVersions.add(version);
            }
        }

        return validVersions;
    }

    /**
     * Retrieves method information for each version of the project from Git.
     *
     * @param project The project to process
     * @throws Exception If an error occurs during method extraction
     */
    private void getMethodsforVersion(Project project, List<Version> versions) throws GitException {
        GitService gitService = new GitService();
        for (Version version : versions) {
            version.setMethods(gitService.getMethodsInVersion(project, version));
        }
    }

    /**
     * Retrieves valid fixed bug tickets from Jira for the project.
     *
     * @param project The project to process
     *
     */
    private void getTicket(Project project) throws JiraException {
        JiraService jiraService = new JiraService();
        // lista che conterrà i ticket bug risolti
        List<Ticket> tickets = null;
        // preleva da jira i ticket bug risolti per il progetto
        tickets = jiraService.getFixedBugTickets(project);
        // set the fix version for each ticket
        setFixVersionForTickets(project, tickets);

        // set opening version for each ticket
        setOpeningVersionForTickets(project, tickets);
        // filter the tickets to remove those are invalid
        filterTickets(tickets);
        // set the tickets in the project
        project.setTickets(tickets);
    }

    /**
     * Sets the fix version for each ticket based on the project's git repository.
     *
     * @param project The project
     * @param tickets The list of tickets to process
     */
    private void setFixVersionForTickets(Project project, List<Ticket> tickets) throws GitException {
        GitService gitService = new GitService();
        for (Ticket ticket : tickets) {
            String fixCommitHash = gitService.findFixCommitForTicket(project, ticket.getId());
            if (fixCommitHash != null) {
                ticket.setCommitHash(fixCommitHash);
                ticket.setFixVersion(gitService.getVersionForCommit(project, fixCommitHash));
                ticket.setNameMethodsBuggy(gitService.getModifiedMethodSignaturesfromCommit(project, fixCommitHash));
            } else {
                ticket.setFixVersion(null);
                ticket.setCommitHash(null);
                ticket.setNameMethodsBuggy(null);
            }
        }
    }

    /**
     * Sets the opening version for each ticket based on the project's versions.
     *
     * @param project The project
     * @param tickets The list of tickets to process
     */
    private void setOpeningVersionForTickets(Project project, List<Ticket> tickets) {
        for (Ticket ticket : tickets) {
            ZonedDateTime openDate = ticket.getOpenDate();
            if (openDate != null) {
                Version openingVersion = null;
                for (Version version : project.getVersions()) {
                    if (version.getReleaseDate() != null &&
                            !version.getReleaseDate().before(Date.from(openDate.toInstant()))) {
                        openingVersion = version;
                        break;
                    }
                }
                if (openingVersion != null) {
                    ticket.setOpenVersion(openingVersion);
                } else {
                    LOGGER.warning("Nessuna versione trovata con release >= open date per il ticket " + ticket.getId());
                }
            } else {
                LOGGER.warning("Ticket " + ticket.getId() + " non ha open date, impossibile determinare opening version.");
            }
        }
    }


    /**
     * filters the tickets to remove
     *
     * @param tickets the tickets to filter
     */
    private void filterTickets(List<Ticket> tickets) {
        Iterator<Ticket> iterator = tickets.iterator();
        VersionComparator versionComparator = new VersionComparator();
        while (iterator.hasNext()) {
            Ticket ticket = iterator.next();
            boolean remove = false;
            String reason = "";

            if (ticket.getFixVersion() == null) {
                remove = true;
                reason = "fixVersion mancante";
            } else if (ticket.getOpenVersion() == null) {
                remove = true;
                reason = "openVersion mancante";
            } else if (ticket.getInjectedVersion() != null && versionComparator.compare(ticket.getInjectedVersion(), ticket.getFixVersion()) >= 0) {
                remove = true;
                reason = "injectedVersion >= fixVersion";
            }

            if (remove) {
                LOGGER.warning("Rimosso ticket " + ticket.getId() + ": " + reason);
                iterator.remove();
            }
        }
    }



    // Method to extract the project name from a GitHub URL
    private static String extractGitHubProjectName(String url) {
        // if the URL is null or empty, return null
        if (url == null || url.isEmpty()) return null;
        // remove trailing .git if present
        url = url.replaceAll("\\.git$", "");

        // URL HTTP/HTTPS
        if (url.startsWith("https://github.com/") || url.startsWith("http://github.com/")) {
            String[] parts = url.split("/");
            if (parts.length >= 5) {
                return parts[4];
            }
        }

        // URL SSH, es: git@github.com:user/repo.git
        if (url.startsWith("git@github.com:")) {
            String[] parts = url.split(":")[1].split("/");
            if (parts.length == 2) {
                return parts[1];
            }
        }

        return null;
    }

    // Method to extract the directory name from a given path
    private static String extractDirectoryName(String pathStr) {
        // if the path is null or empty, return null
        if (pathStr == null || pathStr.isEmpty()) return null;
        // create a Path object from the string
        Path path = Paths.get(pathStr);
        // get the file name from the path
        Path fileName = path.getFileName();

        return fileName != null ? fileName.toString() : null;
    }
}
