package it.isw2.flaviosimonelli.model.Project;

import it.isw2.flaviosimonelli.utils.dao.impl.GitService;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectFactory {
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

    // Method to create a new project with the given parameters
    public Project CreateProject(Boolean localRepository, String jiraID, String gitURL, String gitBranch, String gitDirectory, String releaseTagFormat) {
        GitService gitService = new GitService();
        JiraService jiraService = new JiraService();
        if (localRepository == false) {
            project = new Project(extractGitHubProjectName(gitURL), jiraID, gitURL, gitBranch, gitDirectory + "/" + extractGitHubProjectName(gitURL), releaseTagFormat);
            // Clone the Git repository
            gitService.cloneRepository(project);
        } else {
            project = new Project(extractDirectoryName(gitDirectory), jiraID, gitURL, gitBranch, gitDirectory, releaseTagFormat);
            // Open the local Git repository
            gitService.openRepository(project);
        }

        return project;
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
