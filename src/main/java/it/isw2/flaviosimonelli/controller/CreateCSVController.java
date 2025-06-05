package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Project.ProjectFactory;
import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.utils.VersionComparator;
import it.isw2.flaviosimonelli.utils.bean.GitBean;
import it.isw2.flaviosimonelli.utils.bean.JiraBean;
import it.isw2.flaviosimonelli.utils.dao.impl.GitService;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller responsible for project data extraction and CSV creation.
 * This is a stateless controller that manages the workflow of creating
 * a project, gathering data from Jira and Git, and exporting to CSV files.
 */
public class CreateCSVController {

    private static final Logger LOGGER = Logger.getLogger(CreateCSVController.class.getName());
    private static final String RESULT_PATH = "result/";

    /**
     * Creates a project and processes its data from start to finish.
     * This method is the main entry point for the controller.
     * Orchestrates the process of collecting all necessary project data.
     *
     * @param jiraBean Configuration for Jira access
     * @param gitBean Configuration for Git access
     * @return true if project creation and data processing succeeded
     */
    public boolean createCSV(JiraBean jiraBean, GitBean gitBean) {
        try {
            // Create project through factory
            ProjectFactory projectFactory = ProjectFactory.getInstance();
            Project project = projectFactory.CreateProject(
                                gitBean.isLocalRepository(),
                                jiraBean.getJiraId(),
                                gitBean.getRemoteUrl(),
                                gitBean.getBranch(),
                                gitBean.getPath(),
                                gitBean.getReleaseTagFormat());

            initializeVersions(project);
            initializeTickets(project);
            initializeMethods(project);

            // Export data to CSV files
            exportProjectData(project);

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during project processing", e);
            return false;
        }
    }

    /**
     * Retrieves and filters versions from Jira that also exist as Git tags.
     *
     * @param project The project to process
     * @throws SystemException If an error occurs during version retrieval
     */
    private void initializeVersions(Project project) throws SystemException {
        JiraService jiraService = new JiraService();
        List<Version> versions = jiraService.getVersions(project);
        List<Version> validVersions = filterValidVersions(project, versions);
        project.setVersions(validVersions);
    }

    /**
     * Filters versions to keep only those that have corresponding Git tags.
     *
     * @param project The project being processed
     * @param versions List of versions to filter
     * @return List of valid versions with commit hashes
     */
    private List<Version> filterValidVersions(Project project, List<Version> versions) {
        GitService gitService = new GitService();
        List<Version> validVersions = new ArrayList<>();

        for (Version version : versions) {
            String hashCommit = gitService.getCommitByVersion(project, version);

            if (hashCommit == null) {
                LOGGER.info("Version " + version.getName() + " not found in Git repository. Removing from list.");
            } else {
                version.setHashCommit(hashCommit);
                validVersions.add(version);
            }
        }

        return validVersions;
    }

    /**
     * Retrieves all fixed bug tickets from Jira for the project.
     *
     * @param project The project to process
     * @throws SystemException If an error occurs during ticket retrieval
     */
    private void initializeTickets(Project project) throws Exception {
        JiraService jiraService = new JiraService();
        project.setTickets(jiraService.getFixedBugTickets(project));
        filterTickets(project);
    }

    /**
     * Retrieves method information for each version of the project from Git.
     *
     * @param project The project to process
     * @throws Exception If an error occurs during method extraction
     */
    private void initializeMethods(Project project) throws Exception {
        GitService gitService = new GitService();
        for (Version version : project.getVersions()) {
            version.setMethods(gitService.getMethodsInVersion(project, version));
        }
    }

    private void filterTickets(Project project) throws Exception {
        GitService gitService = new GitService();
        VersionComparator comparator = new VersionComparator();

        Iterator<Ticket> it = project.getTickets().iterator();
        while (it.hasNext()) {
            Ticket ticket = it.next();

            String fixCommitHash = gitService.findFixCommitForTicket(project, ticket.getId());
            if (fixCommitHash == null) {
                LOGGER.info("Ticket " + ticket.getId() + " has no fix commit, removing it.");
                it.remove();  // Rimozione sicura usando Iterator
                continue;
            }
            ticket.setFixVersion(gitService.getVersionForCommit(project, fixCommitHash));
            ticket.setNameMethodsBuggy(gitService.getModifiedMethodSignaturesfromCommit(project, fixCommitHash));

            if (ticket.getFixVersion() == null) {
                LOGGER.info("Ticket " + ticket.getId() + " is corrupted, removing it.");
                it.remove();  // Rimozione sicura usando Iterator
            } else {

                // Check if affected version is valid
                if (ticket.getAffectedVersion() != null && comparator.compare(ticket.getAffectedVersion().getName(), ticket.getFixVersion().getName()) > 0) {
                    ticket.setAffectedVersion(null);
                }
            }
        }
    }




    /**
     * Exports all project data to CSV files.
     *
     * @param project The project containing data to export
     */
    private void exportProjectData(Project project) {
        String projectName = project.getName();

        CsvExporter.writeTicketsToCsv(project.getTickets(),
                RESULT_PATH + "tickets_" + projectName + ".csv");

        CsvExporter.writeVersionsToCsv(project.getVersions(),
                RESULT_PATH + "version_" + projectName + ".csv");

        CsvExporter.writeMethodsToCsv(project,
                RESULT_PATH + "methods_" + projectName + ".csv");
    }
}
