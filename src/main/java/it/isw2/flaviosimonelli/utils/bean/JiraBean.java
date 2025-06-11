package it.isw2.flaviosimonelli.utils.bean;

import it.isw2.flaviosimonelli.model.Project.ApproachProportion;
import it.isw2.flaviosimonelli.view.ViewCLI;

/**
 * Bean class representing Jira project configuration.
 * Contains information needed to access and identify a Jira project.
 */
public class JiraBean {
    private final String jiraId;
    private final String approachProportion;


    /**
     * Creates a configuration for a Jira project.
     *
     * @param jiraId             The identifier of the Jira project
     * @param approachProportion
     * @throws IllegalArgumentException if jiraId is null or empty
     */
    public JiraBean(String jiraId, String approachProportion) {
        if (jiraId == null || jiraId.trim().isEmpty()) {
            throw new IllegalArgumentException("Jira project ID cannot be null or empty");
        }
        this.jiraId = jiraId;
        this.approachProportion = approachProportion;
    }

    /**
     * Returns the Jira project identifier.
     *
     * @return The Jira project ID
     */
    public String getJiraId() {
        return jiraId;
    }

    /**
     * Returns the approach proportion for the Jira project.
     *
     * @return The approach proportion
     */
    public ApproachProportion getApproachProportion() {
        if (approachProportion == null || approachProportion.trim().isEmpty()) {
            return ApproachProportion.COMPLETE;
        }
        return ApproachProportion.fromString(approachProportion);
    }
}
