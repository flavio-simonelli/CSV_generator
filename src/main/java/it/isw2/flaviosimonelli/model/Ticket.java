package it.isw2.flaviosimonelli.model;

import java.util.List;

/**
 * Classe che rappresenta un ticket di JIRA.
 *
 * Parametri:
 * - key: identificatore univoco del ticket in JIRA (es. PROJECT-123)
 * - id: identificatore numerico del ticket
 * - affectedVersions: lista delle versioni del software interessate dal problema
 * - fixVersion: versione del software in cui il problema è stato risolto
 */
public class Ticket {
    private String key;
    private String id;
    private List<String> affectedVersions;
    private String fixVersion;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAffectedVersions(List<String> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public List<String> getAffectedVersions() {
        return affectedVersions;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }

    public String getFixVersion() {
        return fixVersion;
    }

}