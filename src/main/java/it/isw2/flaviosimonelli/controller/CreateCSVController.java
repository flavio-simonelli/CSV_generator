package it.isw2.flaviosimonelli.controller;

import it.isw2.flaviosimonelli.model.Repository;
import it.isw2.flaviosimonelli.utils.Bean.RepositoryBean;
import it.isw2.flaviosimonelli.utils.dao.GitDAO;
import it.isw2.flaviosimonelli.utils.dao.GitDAOFactory;
import it.isw2.flaviosimonelli.utils.exception.GitException;

public class CreateCSVController {
    public void cloneRepository(RepositoryBean repo) {
        GitDAO gitDAO = GitDAOFactory.create();
        Repository repository = new Repository(repo.getUrl(), repo.getLocalpath());
        try {
            gitDAO.cloneRepository(repository);
        } catch (GitException e) {
            throw new RuntimeException(e);
        }
    }
}
