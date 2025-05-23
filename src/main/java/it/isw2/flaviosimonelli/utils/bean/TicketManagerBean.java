package it.isw2.flaviosimonelli.utils.bean;

public class TicketManagerBean {
    private String JiraID;

    public TicketManagerBean(String JiraID) {
        this.JiraID = JiraID;
    }

    public String getJiraID() {
        return JiraID;
    }

}
