package it.isw2.flaviosimonelli.model.Project;

import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.model.Version;

import java.util.List;

public class Project {
    private String name;
    private String conventionReleaseTag;
    private String jiraID;
    private String gitURL;
    private String gitBranch;
    private String gitDirectory;
    private List<Ticket> tickets;
    private List<Version> versions;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getConventionReleaseTag() {
        return conventionReleaseTag;
    }
    public void setConventionReleaseTag(String conventionReleaseTag) {
        this.conventionReleaseTag = conventionReleaseTag;
    }

    public String getJiraID() {
        return jiraID;
    }
    public void setJiraID(String jiraID) {
        this.jiraID = jiraID;
    }

    public String getGitURL() {
        return gitURL;
    }
    public void setGitURL(String gitURL) {
        this.gitURL = gitURL;
    }

    public String getGitBranch() {
        return gitBranch;
    }
    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public String getGitDirectory() {
        return gitDirectory;
    }
    public void setGitDirectory(String gitDirectory) {
        this.gitDirectory = gitDirectory;
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

}

