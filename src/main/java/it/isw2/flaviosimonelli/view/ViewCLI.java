package it.isw2.flaviosimonelli.view;

import it.isw2.flaviosimonelli.controller.CreateCSVController;
import it.isw2.flaviosimonelli.utils.bean.JiraBean;
import it.isw2.flaviosimonelli.utils.bean.GitBean;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class ViewCLI {
    private final Scanner scanner;
    private static final String CONFIG_FILE_PATH = "config/config.properties";

    public ViewCLI() {
        this.scanner = new Scanner(System.in);
    }

    // schermata iniziale di avvio tool
    public void start() {
        System.out.println("=== CSV Generator Tool ===");

        getInfoProject(); // schermata che richiede le informazioni del progetto

    }

    // metodo che gestisce le informazioni del progetto
    private void getInfoProject() {
        Properties properties = new Properties();
        String jiraID = null;
        String conventionReleaseTag = null;
        String githubUrl = null;
        String branch = null;
        String parentDirectory = null;
        String directory = null;

        do {
            System.out.println("Per favore assicurati di aver configurato correttamente il file " + CONFIG_FILE_PATH);
            System.out.println("Premi INVIO quando hai completato la modifica del file properties.");
            scanner.nextLine();

            try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
                properties.load(fis);

                jiraID = properties.getProperty("jira.id");
                conventionReleaseTag = properties.getProperty("tag.release.convention", "{VERSION}");
                githubUrl = properties.getProperty("github.url", "");
                branch = properties.getProperty("github.branch");
                parentDirectory = properties.getProperty("local.directory.parent", "");
                directory = properties.getProperty("local.directory.git", "");

                if ((githubUrl == null || parentDirectory == null) && (directory == null)) {
                    System.out.println("Errore: il file di configurazione non contiene le informazioni necessarie.");
                    System.out.println("Se si vuole clonare un repository compilare i campi 'github.url' e 'local.directory.parent'.");
                    System.out.println("Se si vuole aprire un repository esistente compilare il campo 'local.directory.git'.");
                    System.out.println("La compilazione di entrambi i campi prediligera la apertura del repository esistente");
                    return;
                }

                if (jiraID == null) {
                    System.out.println("Errore: il file di configurazione non contiene l'ID del progetto JIRA.");
                    return;
                }

                if (branch == null) {
                    System.out.println("Attenzione: il campo 'github.branch' non è stato specificato.");
                    return;
                }

                System.out.println("\nConfigurazione caricata:");
                System.out.println("- Progetto JIRA: " + jiraID);
                if ( directory != null) {
                    System.out.println("- Repository Git locale: " + directory);
                } else {
                    System.out.println("- Repository GitHub: " + githubUrl);
                    System.out.println("- Parent directory: " + directory);
                }
                System.out.println("- Branch: " + branch);
                System.out.println("- Convenzione di rilascio: " + conventionReleaseTag);


            } catch (IOException e) {
                System.out.println("Errore durante la lettura del file di configurazione: " + e.getMessage());
                System.out.println("Assicurati che il file " + CONFIG_FILE_PATH + " esista e sia correttamente formattato.");
            }
        } while (!askConfigConfirmation());

        // Creazione dei bean per gestire i ticket e le versioni
        JiraBean jiraBean = new JiraBean(jiraID);
        GitBean gitBean;
        if( directory != null){
            gitBean = new GitBean(directory, branch, conventionReleaseTag);
        } else {
            gitBean = new GitBean(githubUrl, parentDirectory, branch, conventionReleaseTag);
        }
        // Creazione del controller per la generazione dei CSV
        CreateCSVController createCSVController = new CreateCSVController();
        // Creazione del progetto e generazione dei CSV
        createCSVController.createProject(jiraBean, gitBean);
    }

    private boolean askConfigConfirmation() {
        System.out.print("La configurazione è corretta? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y");
    }
}