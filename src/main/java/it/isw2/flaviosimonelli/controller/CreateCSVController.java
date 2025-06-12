package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Project.ProjectFactory;
import it.isw2.flaviosimonelli.model.Project.RepositoryType;
import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.model.method.Method;
import it.isw2.flaviosimonelli.utils.CsvExporter;
import it.isw2.flaviosimonelli.utils.bean.GitBean;
import it.isw2.flaviosimonelli.utils.bean.JiraBean;
import it.isw2.flaviosimonelli.utils.exception.GitException;


import java.util.ArrayList;
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
        ProjectFactory projectFactory = ProjectFactory.getInstance();
        // crea la nuova istanza del progetto scegliendo se clonare un repository remoto o utilizzare uno locale in base all'attributo RepositoryType di GitBean
        if (gitBean.getType() == RepositoryType.LOCAL) {
            LOGGER.info("Using local repository at: " + gitBean.getPath());
            projectFactory.createProject(jiraBean.getJiraId(), jiraBean.getApproachProportion(), gitBean.getBranch(), gitBean.getPath(), gitBean.getReleaseTagFormat());
        } else {
            LOGGER.info("Cloning remote repository from: " + gitBean.getRemoteUrl());
            projectFactory.createProject(jiraBean.getJiraId(), jiraBean.getApproachProportion(), gitBean.getRemoteUrl(), gitBean.getBranch(), gitBean.getPath(), gitBean.getReleaseTagFormat());
        }

            // propotion
            proportion(project);
            // Labeling
            labeling(project);

            // Export data to CSV files
            exportProjectData(project);

            return true;
        } catch (GitException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'operazione Git: " + e.getOperation(), e);
            return false;
        }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante la creazione del progetto o l'elaborazione dei dati", e);
            return false;
        }
    }

    private void labeling(Project project) {
        List<Ticket> tickets = project.getTickets();
        List<Version> versions = project.getVersions();

        for (Ticket ticket : tickets) {
            Version injectedVersion = ticket.getInjectedVersion();
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

    private int getVersionIndex(List<Version> versions, Version target) {
        for (int i = 0; i < versions.size(); i++) {
            if (versions.get(i).getName().equals(target.getName())) {
                return i;
            }
        }
        return -1; // Non trovata
    }

    /**
     * Calcola il valore di proporzione P per il progetto e predice la Injected Version
     * per i ticket che non ne hanno una.
     * Formula: P = (FV - IV) / (FV - OV)
     * Predizione IV = FV - (FV - OV) * P
     * Applica l'approccio complete poichè a noi non interessa un modello che sia realistico (per il nostro scopo), ma piuttosto un modello che sia più accurato possibile.
     *
     * @param project Il progetto per cui calcolare la proporzione
     */
    private void proportion(Project project) {
        List<Ticket> tickets = project.getTickets();
        List<Version> versions = project.getVersions();
        int p = completeApproachPropotion(tickets, versions);
        LOGGER.info("Proporzione P calcolata: " + p);
        for (Ticket ticket : tickets) {
            Version fixVersion = ticket.getFixVersion();
            Version openVersion = ticket.getOpenVersion();
            Version affectedVersion = ticket.getInjectedVersion();

            if (affectedVersion == null && fixVersion != null && openVersion != null) {
                // Calcola la versione iniettata usando la proporzione P
                int fvIndex = versions.indexOf(fixVersion);
                int ovIndex = versions.indexOf(openVersion);

                if (fvIndex >= 0 && ovIndex >= 0 && fvIndex > ovIndex) {
                    // Calcola l'indice della versione iniettata
                    int ivIndex = (int) Math.ceil(fvIndex - (fvIndex - ovIndex) * ((double) p));
                    if (ivIndex < 0) ivIndex = 0; // Assicurati che l'indice non sia negativo
                    if (ivIndex >= versions.size()) ivIndex = versions.size() - 1; // Assicurati che l'indice non superi la dimensione della lista

                    Version injectedVersion = versions.get(ivIndex);
                    ticket.setInjectedVersion(injectedVersion);
                    LOGGER.info("Ticket " + ticket.getId() + " ha una versione iniettata predetta: " + injectedVersion.getName());
                } else {
                    LOGGER.warning("Non è possibile calcolare la versione iniettata per il ticket " + ticket.getId());
                }
            }
        }


    }

    private int completeApproachPropotion(List<Ticket> tickets, List<Version> versions) {
        List<Double> pValues = new ArrayList<>();
        // Calcola i valori di P per ogni ticket
        for (Ticket ticket : tickets) {
            Version fixVersion = ticket.getFixVersion();
            Version openVersion = ticket.getOpenVersion();
            Version affectedVersion = ticket.getInjectedVersion();

            if (fixVersion == null || openVersion == null || affectedVersion == null) {
                continue;
            }

            int fvIndex = versions.indexOf(fixVersion);
            int ovIndex = versions.indexOf(openVersion);
            int ivIndex = versions.indexOf(affectedVersion);

            if (fvIndex == ovIndex) continue; // evita divisione per zero

            double p = (double) (fvIndex - ivIndex) / (fvIndex - ovIndex);
            pValues.add(p);
        }

        // Calcola la mediana dei valori di P
        if (pValues.isEmpty()) return 0;
        pValues.sort(Double::compareTo);
        int size = pValues.size();
        double median;
        if (size % 2 == 1) {
            median = pValues.get(size / 2);
        } else {
            median = (pValues.get(size / 2 - 1) + pValues.get(size / 2)) / 2.0;
        }
        return (int) Math.ceil(median);
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
