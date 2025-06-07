package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Project.ProjectFactory;
import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.model.method.Method;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.utils.VersionComparator;
import it.isw2.flaviosimonelli.utils.bean.GitBean;
import it.isw2.flaviosimonelli.utils.bean.JiraBean;
import it.isw2.flaviosimonelli.utils.dao.impl.GitService;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
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
            // Labeling
            labeling(project);

            // Export data to CSV files
            exportProjectData(project);

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during project processing", e);
            return false;
        }
    }

    private void labeling(Project project) {
        List<Ticket> tickets = project.getTickets();
        List<Version> versions = project.getVersions();

        for (Ticket ticket : tickets) {
            Version injectedVersion = ticket.getAffectedVersion();
            Version fixVersion = ticket.getFixVersion();
            List<String> buggyMethodSignatures = ticket.getNameMethodsBuggy();

            if (injectedVersion == null || fixVersion == null || buggyMethodSignatures == null || buggyMethodSignatures.isEmpty()) {
                LOGGER.warning("Ticket " + ticket.getId() + " manca di informazioni necessarie per il labeling");
                continue; // Salta ticket senza informazioni necessarie
            }

            // Ottieni gli indici delle versioni affected e fix
            int injectedVersionIndex = getVersionIndex(versions, injectedVersion);
            int fixVersionIndex = getVersionIndex(versions, fixVersion);

            if (injectedVersionIndex < 0 || fixVersionIndex < 0 || injectedVersionIndex > fixVersionIndex) {
                LOGGER.warning("Ticket " + ticket.getId() + " ha versioni non valide o in ordine errato");
                continue; // Salta se le versioni non sono valide o in ordine errato
            }

            // Imposta il flag buggy per i metodi interessati in tutte le versioni da injected a fix (incluse)
            for (int versionIndex = injectedVersionIndex; versionIndex < fixVersionIndex; versionIndex++) {
                Version version = versions.get(versionIndex);
                if (version.getMethods() == null) continue;

                for (String methodSignature : buggyMethodSignatures) {
                    // Trova e marca il metodo come buggy
                    for (Method method : version.getMethods()) {
                        if (method.getSignature().equals(methodSignature)) {
                            method.setBuggy(true);
                            LOGGER.fine("Metodo " + methodSignature + " marcato come buggy nella versione " + version.getName());
                        }
                    }
                }
            }
        }

        LOGGER.info("Labeling completato per tutti i metodi buggy");
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
     * Retrieves all fixed bug tickets from Jira for the project.
     *
     * @param project The project to process
     * @throws SystemException If an error occurs during ticket retrieval
     */
    private void initializeTickets(Project project) throws Exception {
        JiraService jiraService = new JiraService();
        project.setTickets(jiraService.getFixedBugTickets(project));
        setOpeningVersionForTickets(project);
        filterTickets(project);
        proportion(project);
    }

    private void setOpeningVersionForTickets(Project project) {
        GitService gitService = new GitService();
        for (Ticket ticket : project.getTickets()) {
            ZonedDateTime openDate = ticket.getOpenDate();
            if (openDate != null) {
                // Find the first version that was released before the ticket's open date
                for (Version version : project.getVersions()) {
                    if (version.getReleaseDate() != null && version.getReleaseDate().before(Date.from(openDate.toInstant()))) {
                        ticket.setOpenVersion(version);
                        break; // Found the opening version, no need to continue
                    }
                }
            } else {
                LOGGER.warning("Ticket " + ticket.getId() + " has no open date, cannot determine opening version.");
            }
        }
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
            ticket.setCommitHash(fixCommitHash);
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
     * Calcola il valore di proporzione P per il progetto e predice la Injected Version
     * per i ticket che non ne hanno una.
     * Formula: P = (FV - IV) / (FV - OV)
     * Predizione IV = FV - (FV - OV) * P
     *
     * @param project Il progetto per cui calcolare la proporzione
     */
    private void proportion(Project project) {
        List<Ticket> tickets = project.getTickets();
        VersionComparator comparator = new VersionComparator();
        List<Double> pValues = new ArrayList<>();

        // Calcola i valori P per i ticket con IV (affected version) conosciuta
        for (Ticket ticket : tickets) {
            Version iv = ticket.getAffectedVersion();  // Injected Version
            Version fv = ticket.getFixVersion();       // Fix Version
            Version ov = ticket.getOpenVersion();      // Opening Version

            if (iv != null && fv != null && ov != null) {
                int fvIndex = getVersionIndex(project.getVersions(), fv);
                int ivIndex = getVersionIndex(project.getVersions(), iv);
                int ovIndex = getVersionIndex(project.getVersions(), ov);

                if (fvIndex >= 0 && ivIndex >= 0 && ovIndex >= 0 &&
                        fvIndex > ivIndex && fvIndex > ovIndex && ivIndex <= ovIndex) {
                    double p = (double)(fvIndex - ivIndex) / (fvIndex - ovIndex);
                    pValues.add(p);
                }
            }
        }

        // Calcola il valore P medio
        double avgP;
        if (!pValues.isEmpty()) {
            double sum = 0.0;
            for (Double p : pValues) {
                sum += p;
            }
            avgP = sum / pValues.size();
        } else {
            // Se non ci sono ticket validi, usa 0.5 come valore predefinito
            avgP = 0.5;
            LOGGER.warning("Nessun ticket valido trovato per calcolare P. Utilizzo valore predefinito: " + avgP);
        }

        // Predici la Injected Version per i ticket senza IV
        for (Ticket ticket : tickets) {
            if (ticket.getAffectedVersion() == null && ticket.getFixVersion() != null) {
                Version fv = ticket.getFixVersion();
                Version ov = ticket.getOpenVersion();

                if (ov != null) {
                    int fvIndex = getVersionIndex(project.getVersions(), fv);
                    int ovIndex = getVersionIndex(project.getVersions(), ov);

                    if (fvIndex >= 0 && ovIndex >= 0 && fvIndex > ovIndex) {
                        // Calcola l'indice della Injected Version prevista
                        int predictedIvIndex = (int) Math.round(fvIndex - (fvIndex - ovIndex) * avgP);

                        // Assicurati che l'indice sia valido
                        if (predictedIvIndex >= 0 && predictedIvIndex < project.getVersions().size()) {
                            Version predictedIv = project.getVersions().get(predictedIvIndex);
                            ticket.setAffectedVersion(predictedIv);
                            LOGGER.info("Versione di introduzione predetta per ticket " + ticket.getId() + ": " + predictedIv.getName());
                        }
                    }
                }
            }
        }

        LOGGER.info("Valore P medio calcolato: " + avgP);
    }


    /**
     * Trova l'indice di una versione nell'elenco delle versioni del progetto.
     *
     * @param versions Elenco delle versioni
     * @param version La versione di cui trovare l'indice
     * @return L'indice della versione o -1 se non trovata
     */

    private int getVersionIndex(List<Version> versions, Version version) {
        for (int i = 0; i < versions.size(); i++) {
            if (versions.get(i).getName().equals(version.getName())) {
                return i;
            }
        }
        return -1;
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
