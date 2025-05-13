package it.isw2.flaviosimonelli.model;

import java.util.List;

public class Project {
    private String jiraID;
    private List<Version> versions;
    private List<Ticket> ticketsFixClosed;

    public void setJiraID(String jiraID) {
        this.jiraID = jiraID;
    }

    public String getJiraID() {
        return jiraID;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
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

