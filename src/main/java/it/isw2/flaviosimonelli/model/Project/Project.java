package it.isw2.flaviosimonelli.model.Project;

import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;

import java.util.List;

public class Project {
    private final String name; // name of the project, usually the name of the Git repository
    private final String jiraID; // ID of the project in Jira, used to retrieve tickets
    private final ApproachProportion approachProportion; // Approach for the proportion applied to the ticket in a project
    private final String gitBranch; // Branch of the Git repository to work with
    private final String gitDirectory; // Directory where the Git repository is located or will be cloned
    private final String releaseTagFormat; // Convention for release tags, e.g. "v{VERSION}" or "release-{VERSION}"
    private List<Ticket> tickets; // List of tickets associated with the project, retrieved from Jira
    private List<Version> versions; // List of versions associated with the project

    // Constructor for creating a new project with all necessary parameters (without tickets and versions)
    public Project(String name, String jiraID, ApproachProportion approachProportion, String gitBranch, String gitDirectory, String releaseTagFormat) {
        this.name = name;
        this.jiraID = jiraID;
        this.approachProportion = approachProportion;
        this.gitBranch = gitBranch;
        this.gitDirectory = gitDirectory;
        this.releaseTagFormat = releaseTagFormat;
    }

    // Getters and Setters for all fields
    public String getName() {
        return name;
    }

    public String getJiraID() {
        return jiraID;
    }

    public ApproachProportion getApproachProportion() { return approachProportion; }

    public String getGitBranch() {
        return gitBranch;
    }

    public String getGitDirectory() {
        return gitDirectory;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<Version> getVersions() {
        return versions;
    }
    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }
    public Version getVersionFromName(String name) {
        for (Version version : versions) {
            if (version.getName().equals(name)) {
                return version;
            }
        }
        return null;
    }

    public String getReleaseTagFormat() { return releaseTagFormat; }
}

