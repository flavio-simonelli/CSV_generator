package it.isw2.flaviosimonelli.model;

import java.util.List;

public class Project {
    private String jiraID;
    private String versionManagerURL;
    private String directory;
    private List<Version> versions;
    private List<Ticket> ticketsFixClosed;
    private String branchName;

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
    public String getBranchName() {
        return branchName;
    }

    public void setJiraID(String jiraID) {
        this.jiraID = jiraID;
    }

    public String getJiraID() {
        return jiraID;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public void setVersionManagerURL(String gitURL) {
        this.versionManagerURL = gitURL;
    }

    public String getVersionManagerURL() {
        return versionManagerURL;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
    public String getDirectory() {
        return directory;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setTicketsFixClosed(List<Ticket> ticketsFixClosed) {
        this.ticketsFixClosed = ticketsFixClosed;
    }

    public List<Ticket> getTicketsFixClosed() {
        return ticketsFixClosed;
    }
}

