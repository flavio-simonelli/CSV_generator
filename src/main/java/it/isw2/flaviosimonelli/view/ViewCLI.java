package it.isw2.flaviosimonelli.view;

import it.isw2.flaviosimonelli.controller.CreateCSVController;
import it.isw2.flaviosimonelli.utils.bean.TicketManagerBean;
import it.isw2.flaviosimonelli.utils.bean.VersionManagerBean;

import java.util.Scanner;
import javax.swing.*;
import java.io.File;

public class ViewCLI {
    private Scanner scanner;

    public ViewCLI() {
        this.scanner = new Scanner(System.in);
    }

    // schermata iniziale di avvio tool
    public void start() {
        System.out.println("=== CSV Generator Tool ===");
        getInfoProject();
    }

    // Schermata che prende le informazioni utili per prendere le informazioni del progetto da Git e JIRA
    private void getInfoProject() {
        System.out.println("=== Inizializzazione Progetto ===");

        // Richiesta informazioni per interazione con JIRA
        String jiraID = getInput("Inserisci ID del progetto JIRA: ");
        while (jiraID == null || jiraID.trim().isEmpty()) {
            System.out.println("ID progetto non valido. Riprova.");
            jiraID = getInput("Inserisci ID del progetto JIRA: ");
        }
        TicketManagerBean ticketManagerBean = new TicketManagerBean(jiraID);

        // Richiesta informazioni per interazione con Git
        String choice = getInput("Vuoi clonare un progetto da GitHub o aprirne uno già esistente? (clone/open): ").trim().toLowerCase();
        while (!choice.equals("clone") && !choice.equals("open")) {
            System.out.println("Scelta non valida. Riprova.");
            choice = getInput("Vuoi clonare un progetto da GitHub o aprirne uno già esistente? (clone/open): ").trim().toLowerCase();
        }
        VersionManagerBean versionManagerBean = null;
        if (choice.equals("clone")) {
            // Informazioni per il clone e del progetto
            String url = getInput("Inserisci l'URL del repository GitHub: ");
            while (url == null || url.trim().isEmpty()) {
                System.out.println("URL non valido. Riprova.");
                url = getInput("Inserisci l'URL del repository GitHub: ");
            }
            String branch = getInput("Inserisci il branch desiderato: ");
            while (branch == null || branch.trim().isEmpty()) {
                System.out.println("Branch non valido. Riprova.");
                branch = getInput("Inserisci il branch desiderato: ");
            }
            String parentDirectory = scegliCartellaConPopup("Seleziona la parent directory per la clonazione");
            if (parentDirectory == null) {
                parentDirectory = scegliCartellaConPopup("Inserisci il parent directory per la clonazione");
            }
            versionManagerBean = new VersionManagerBean(url, branch, parentDirectory, choice);
        } else {
            // Informazioni per l'apertura del progetto esistente
            String directory = scegliCartellaConPopup("Seleziona la cartella del progetto esistente");
            if (directory == null) {
                directory = scegliCartellaConPopup("Inserisci la cartella del progetto esistente");
            }
            versionManagerBean = new VersionManagerBean(directory, choice);
        }
        // Chiamata del controller
        CreateCSVController controller = new CreateCSVController();
        controller.createProject(ticketManagerBean, versionManagerBean);
    }

    private String getInput(String messaggio) {
        System.out.print(messaggio);
        return scanner.nextLine();
    }

    //interfaccia per selezionare la cartella grafica
    public String scegliCartellaConPopup(String messaggio) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(messaggio);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            return selectedDir.getAbsolutePath();
        } else {
            System.out.println("Nessuna cartella selezionata.");
            return null;
        }
    }

}