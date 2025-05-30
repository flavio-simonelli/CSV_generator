package it.isw2.flaviosimonelli.utils.bean;

/**
 * Bean class representing Jira project configuration.
 * Contains information needed to access and identify a Jira project.
 */
public class JiraBean {
    private final String jiraId;

    /**
     * Creates a configuration for a Jira project.
     *
     * @param jiraId The identifier of the Jira project
     * @throws IllegalArgumentException if jiraId is null or empty
     */
    public JiraBean(String jiraId) {
        if (jiraId == null || jiraId.trim().isEmpty()) {
            throw new IllegalArgumentException("Jira project ID cannot be null or empty");
        }
        this.jiraId = jiraId;
    }

    /**
     * Returns the Jira project identifier.
     *
     * @return The Jira project ID
     */
    public String getJiraId() {
        return jiraId;
    }
}
