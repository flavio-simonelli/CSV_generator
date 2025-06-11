package it.isw2.flaviosimonelli.model;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Represents a ticket/issue from a tracking system like Jira.
 */
public class Ticket {
    private String id;
    private String commitHash; // commit fix for the issue
    private Version fixVersion;             // Version in which the issue is fixed
    private Version injectedVersion;        // First version in which the issue is present
    private ZonedDateTime openDate;     // When the ticket was resolved
    private Version openVersion; // Version in which the ticket was opened
    private List<String> nameMethodsBuggy; // List of methods that are buggy

    public Version getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(Version fixVersion) {
        this.fixVersion = fixVersion;
    }

    public Version getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Version injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public ZonedDateTime getOpenDate() {
        return openDate;
    }
    public void setOpenDate(ZonedDateTime openDate) {
        this.openDate = openDate;
    }

    public Version getOpenVersion() {
        return openVersion;
    }
    public void setOpenVersion(Version openVersion) {
        this.openVersion = openVersion;
    }

    public List<String> getNameMethodsBuggy() {
        return nameMethodsBuggy;
    }

    public void setNameMethodsBuggy(List<String> nameMethodsBuggy) {
        this.nameMethodsBuggy = nameMethodsBuggy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }
}