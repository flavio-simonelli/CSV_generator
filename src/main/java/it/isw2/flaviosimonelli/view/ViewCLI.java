package it.isw2.flaviosimonelli.view;

import it.isw2.flaviosimonelli.controller.CreateCSVController;
import it.isw2.flaviosimonelli.utils.Bean.ProjectBean;

import java.util.Scanner;

public class ViewCLI {
    private Scanner scanner;

    public ViewCLI() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== CSV Generator Tool ===");
        getNameJiraProject();
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
        ProjectBean project = new ProjectBean();
        project.setProjectName(projectName);

        CreateCSVController controller = new CreateCSVController();
        boolean result = controller.getJIRATickets(project);
        
    }

}