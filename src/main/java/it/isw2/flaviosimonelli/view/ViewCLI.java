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

    public void start() {
        System.out.println("=== CSV Generator Tool ===");
        getGitProject();
        getNameJiraProject();
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

    /**
     * Chiede all'utente se vuole clonare un progetto da GitHub o aprirne uno già esistente.
     * Se l'utente sceglie di clonare, chiede l'URL del repository e la directory di destinazione.
     * Se l'utente sceglie di aprire, chiede la directory locale del progetto.
     */
    public void getGitProject() {
        System.out.println("Vuoi clonare un progetto da GitHub o aprirne uno già esistente?");
        System.out.print("Digita 'c' per clonare o 'a' per aprire: ");
        String scelta = scanner.nextLine().trim().toLowerCase();

        if ("a".equals(scelta)) {
            String directory = scegliCartellaConPopup("Seleziona la cartella del progetto esistente");
            if (directory == null) {
                System.out.println("Operazione annullata.");
                return;
            }
            System.out.println("Progetto aperto dalla directory: " + directory);
            VersionManagerBean versionManagerBean = new VersionManagerBean(directory);
            CreateCSVController controller = new CreateCSVController();
            if (controller.getVersions(versionManagerBean)) {
                System.out.println("Progetto aperto con successo.");
            } else {
                System.out.println("Errore durante l'apertura del progetto.");
            }
        } else if ("c".equals(scelta)) {
            System.out.print("Inserisci l'URL del repository GitHub: ");
            String url = scanner.nextLine().trim();
            System.out.print("Inserisci il branch desiderato: ");
            String branch = scanner.nextLine().trim();
            String directory = scegliCartellaConPopup("Seleziona la cartella di destinazione per la clonazione");
            if (url.isEmpty() || directory == null) {
                System.out.println("URL o directory non validi. Operazione annullata.");
                return;
            }
            System.out.println("Clonazione del repository in corso...");
            VersionManagerBean versionManagerBean = new VersionManagerBean(url, directory, branch);
            CreateCSVController controller = new CreateCSVController();
            if (controller.getVersions(versionManagerBean)) {
                System.out.println("Repository clonato con successo in: " + directory);
            } else {
                System.out.println("Errore durante la clonazione del repository.");
            }
        } else {
            System.out.println("Scelta non valida. Operazione annullata.");
        }
    }

    public void getNameJiraProject() {
        // Richiedi il nome del progetto JIRA
        System.out.print("Inserisci il nome del progetto JIRA da analizzare: ");
        String projectName = scanner.nextLine();
        
        // Validazione input
        if (projectName == null || projectName.trim().isEmpty()) {
            System.out.println("Nome progetto non valido. Operazione annullata.");
            return;
        }
        
        System.out.println("Analisi del progetto JIRA: " + projectName);
        TicketManagerBean project = new TicketManagerBean();
        project.setJiraID(projectName);

        CreateCSVController controller = new CreateCSVController();
        boolean result = controller.getTickets(project);
        
    }

}