package it.isw2.flaviosimonelli.view;

import it.isw2.flaviosimonelli.controller.CreateCSVController;
import it.isw2.flaviosimonelli.utils.Bean.RepositoryBean;

import java.util.Scanner;

public class ViewCLI {
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        System.out.println("==================================");
        System.out.println("  Benvenuto in  Project Analyzer  ");
        System.out.println("==================================");
        mainMenu();
    }

    // Mostra il menu principale e raccoglie l'input dell'utente
    public void mainMenu() {
        while (true) {
            System.out.println("1. Clonare un nuovo repository");
            System.out.println("2. Selezionare una cartella di un repository esistente");
            System.out.println("3. Esci");
            System.out.print("\nScegli un'opzione (1-3): ");

            int scelta;
            try {
                scelta = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) { // devo gestire poi l'eccezione nella maniera corretta
                System.out.println("Input non valido. Inserisci un numero tra 1 e 3.\n");
                continue;
            }

            switch (scelta) {
                case 1:
                    System.out.println("Clonare un nuovo repository\n");
                    RepositoryBean repository = cloneRepository();
                    new CreateCSVController().cloneRepository(repository);
                    
                    break;
                case 2:
                    System.out.println("Selezionare una repository esistente\n");
                    RepositoryBean repository2 = getRepositoryPath();
                    new CreateCSVController();
                    break;
                case 3:
                    System.out.println("Uscita in corso...");
                    return;
                default:
                    System.out.println("Opzione non valida. Riprova.\n");
                    break;
            }
        }
    }

    // Chiede l'URL e cartella del repository da clonare
    public RepositoryBean cloneRepository() {
        System.out.print("Inserisci l'URL del repository da clonare: ");
        String url = scanner.nextLine();
        System.out.println("Inserisci la cartella locale in cui salvare i file del repostiory: ");
        String path = scanner.nextLine();
        RepositoryBean bean = new RepositoryBean();
        bean.setUrl(url);
        bean.setLocalpath(path);
        return bean;
    }

    // Chiede il percorso della cartella del repository esistente
    public RepositoryBean getRepositoryPath() {
        System.out.print("Inserisci il percorso della cartella locale del repository: ");
        RepositoryBean bean = new RepositoryBean();
        String path = scanner.nextLine();
        bean.setLocalpath(path);
        return bean;
    }

    // Mostra un messaggio di errore
    public void showError(String message) {
        System.out.println("Errore: " + message);
    }

    // Mostra un messaggio di successo
    public void showSuccess(String message) {
        System.out.println(message);
    }

    // Attendere che l'utente premi Enter
    public void waitForUser() {
        System.out.println("\nPremi ENTER per tornare al menu...");
        scanner.nextLine();
    }
}
