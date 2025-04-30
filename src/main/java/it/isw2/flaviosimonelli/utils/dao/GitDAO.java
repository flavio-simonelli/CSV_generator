package it.isw2.flaviosimonelli.utils.dao;

import it.isw2.flaviosimonelli.model.Repository;
import it.isw2.flaviosimonelli.utils.exception.GitException;

public interface GitDAO {
    void cloneRepository(Repository repository) throws GitException;
}
