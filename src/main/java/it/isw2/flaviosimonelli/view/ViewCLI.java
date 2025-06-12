package it.isw2.flaviosimonelli.view;

import it.isw2.flaviosimonelli.controller.CreateCSVController;
import it.isw2.flaviosimonelli.utils.bean.JiraBean;
import it.isw2.flaviosimonelli.utils.bean.GitBean;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command Line Interface for the CSV Generator Tool.
 * Handles user interaction and configuration loading for project setup.
 */
public class ViewCLI {
    private static final Logger LOGGER = Logger.getLogger(ViewCLI.class.getName());

    private static final String CONFIG_FILE_PATH = "config/config.properties";

    private final Scanner scanner;
    private final Properties properties;

    /**
     * Initializes a new CLI view with a scanner for user input.
     */
    public ViewCLI() {
        this.scanner = new Scanner(System.in);
        this.properties = new Properties();
    }

    /**
     * Displays the initial screen and starts the application flow.
     */
    public void start() {
        LOGGER.info("Starting CSV Generator Tool");
        System.out.println("=== CSV Generator Tool ===");
        InfoProjectView();
    }

    /**
     * Handles the project configuration workflow.
     * Loads configuration, validates it, and creates the project.
     */
    private void InfoProjectView() {
        LOGGER.info("Processing project configuration");
        ConfigurationData config = null;

        do {
            System.out.println("Per favore assicurati di aver configurato correttamente il file " + CONFIG_FILE_PATH);
            System.out.println("Premi INVIO quando hai completato la modifica del file properties.");
            scanner.nextLine();
            config = loadConfiguration();

            if (config != null) {
                displayConfigurationSummary(config);
            }
        } while (config == null || !confirmConfiguration());

        createProject(config);
    }


    /**
     * Loads and validates the configuration from the properties file.
     *
     * @return A ConfigurationData object if valid, null otherwise
     */
    private ConfigurationData loadConfiguration() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(fis);

            ConfigurationData config = new ConfigurationData();
            config.jiraID = properties.getProperty("jira.id");
            config.conventionReleaseTag = properties.getProperty("tag.release.convention", "{VERSION}");
            config.githubUrl = properties.getProperty("github.url", "");
            config.branch = properties.getProperty("github.branch");
            config.parentDirectory = properties.getProperty("local.directory.parent", "");
            config.directory = properties.getProperty("local.directory.git", "");
            config.approachProportion = ApproachProportion.fromString(properties.getProperty("approach.proportion", "COMPLETE"));

            if (!validateConfiguration(config)) {
                throw new RuntimeException("Invalid configuration");
            }

            return config;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load configuration", e);
            System.out.printf("Errore durante la lettura del file di configurazione: %s" + "%n", e.getMessage());
            System.out.printf("Assicurati che il file %s esista e sia correttamente formattato." + "%n", CONFIG_FILE_PATH);
            return null;
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Configuration validation failed", e);
            System.out.println("Errore nella configurazione: " + e.getMessage());
            return null;
        }
    }

    /**
     * Displays a summary of the loaded configuration.
     *
     * @param config The configuration to display
     */
    private void displayConfigurationSummary(ConfigurationData config) {
        LOGGER.info("Displaying configuration summary, please review and confirm");
        System.out.println("\nConfigurazione caricata:");
        System.out.println("- Progetto JIRA: " + config.jiraID);

        if (config.directory != null && !config.directory.isEmpty()) {
            System.out.println("- Repository Git locale: " + config.directory);
        } else {
            System.out.println("- Repository GitHub: " + config.githubUrl);
            System.out.println("- Parent directory: " + config.parentDirectory);
        }

        System.out.println("- Branch: " + config.branch);
        System.out.println("- Convenzione di rilascio: " + config.conventionReleaseTag);
        System.out.println("- Approach proportion: " + config.approachProportion);
    }

    /**
     * Validates the configuration data to ensure all required fields are present.
     *
     * @param config The configuration to validate
     * @return true if configuration is valid, false otherwise
     */
    private boolean validateConfiguration(ConfigurationData config) {
        if (config.jiraID == null || config.jiraID.isEmpty()) {
            System.out.println("Errore: il file di configurazione non contiene l'ID del progetto JIRA.");
            return false;
        }

        if (config.branch == null || config.branch.isEmpty()) {
            System.out.println("Errore: il campo 'github.branch' non è stato specificato.");
            return false;
        }

        boolean hasLocalRepo = config.directory != null && !config.directory.isEmpty();
        boolean hasRemoteRepo = !config.githubUrl.isEmpty() && !config.parentDirectory.isEmpty();

        if (!hasLocalRepo && !hasRemoteRepo) {
            System.out.println("Errore: il file di configurazione non contiene le informazioni necessarie.");
            System.out.println("Se si vuole clonare un repository compilare i campi 'github.url' e 'local.directory.parent'.");
            System.out.println("Se si vuole aprire un repository esistente compilare il campo 'local.directory.git'.");
            System.out.println("La compilazione di entrambi i campi prediligera la apertura del repository esistente");
            return false;
        }

        return true;
    }

    /**
     * Asks the user to confirm the configuration.
     *
     * @return true if the user confirms, false otherwise
     */
    private boolean confirmConfiguration() {
        System.out.print("La configurazione è corretta? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y");
    }

    /**
     * Creates the project using the provided configuration.
     *
     * @param config The configuration to use for project creation
     */
    private void createProject(ConfigurationData config) {
        JiraBean jiraBean = new JiraBean(config.jiraID, ApproachProportion.toString(config.approachProportion));
        GitBean gitBean = null;
        if (config.directory != null && !config.directory.isEmpty()) {
            gitBean = new GitBean(
                    config.directory,
                    config.branch,
                    config.conventionReleaseTag
            );
        } else {
            gitBean = new GitBean(
                    config.githubUrl,
                    config.parentDirectory,
                    config.branch,
                    config.conventionReleaseTag
            );
        }

        CreateCSVController controller = new CreateCSVController();
        controller.createCSV(jiraBean, gitBean);
    }



    /**
     * Inner class to hold configuration data.
     */
    private static class ConfigurationData {
        String jiraID;
        String conventionReleaseTag;
        String githubUrl;
        String branch;
        String parentDirectory;
        String directory;
        ApproachProportion approachProportion;
    }

    /**
     * Inner ENUM for ApproachPropotion Attribute.
     */
    private enum ApproachProportion {
        COMPLETE,
        CENTRAL_SLIDING_WINDOW;

        public static ApproachProportion fromString(String value) {
            if (value == null) return COMPLETE;
            return switch (value.toUpperCase()) {
                case "CENTRAL_SLIDING_WINDOW" -> CENTRAL_SLIDING_WINDOW;
                default -> COMPLETE;
            };
        }

        public static String toString(ApproachProportion value) {
            if (value == null) return "COMPLETE";
            return switch (value) {
                case CENTRAL_SLIDING_WINDOW -> "CENTRAL_SLIDING_WINDOW";
                default -> "COMPLETE";
            };
        }
    }
}