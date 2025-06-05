package it.isw2.flaviosimonelli.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a ticket/issue from a tracking system like Jira.
 */
public class Ticket {
    private String id;
    private Version fixVersion;             // Version in which the issue is fixed
    private Version affectedVersion;        // First version in which the issue is present
    private LocalDateTime resolvedDate;     // When the ticket was resolved
    private List<String> nameMethodsBuggy; // List of methods that are buggy

    public Version getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(Version fixVersion) {
        this.fixVersion = fixVersion;
    }

    public Version getAffectedVersion() {
        return affectedVersion;
    }

    public void setAffectedVersion(Version affectedVersion) {
        this.affectedVersion = affectedVersion;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
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
}