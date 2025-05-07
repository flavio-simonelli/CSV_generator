package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;
import it.isw2.flaviosimonelli.utils.bean.ProjectBean;


public class CreateCSVController {

    public boolean getJIRATickets(ProjectBean project) {
        JiraService jiraService = new JiraService();
        try {
            jiraService.getFixedBugTickets(project.getProjectName());
        } catch (SystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
