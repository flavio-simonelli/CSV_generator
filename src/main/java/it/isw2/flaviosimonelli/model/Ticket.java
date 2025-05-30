package it.isw2.flaviosimonelli.model;

import java.time.LocalDateTime;

/**
 * Represents a ticket/issue from a tracking system like Jira.
 */
public class Ticket {
    private Version fixVersion;             // Version in which the issue is fixed
    private Version affectedVersion;        // First version in which the issue is present
    private LocalDateTime createDate;       // When the ticket was created
    private LocalDateTime resolvedDate;     // When the ticket was resolved

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

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }
}