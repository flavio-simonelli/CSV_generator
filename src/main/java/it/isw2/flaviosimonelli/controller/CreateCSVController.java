package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Project;
import it.isw2.flaviosimonelli.utils.dao.impl.JiraService;
import it.isw2.flaviosimonelli.utils.exception.SystemException;
import it.isw2.flaviosimonelli.utils.bean.ProjectBean;


public class CreateCSVController {

    public boolean getJIRATickets(ProjectBean projectBean) {
        Project project = new Project();
        project.setJiraID(projectBean.getProjectName());
        JiraService jiraService = new JiraService();
        try {
            jiraService.getFixedBugTickets(project);
        } catch (SystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            jiraService.getVersionsProject(project);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
