package it.isw2.flaviosimonelli.utils.dao;

import it.isw2.flaviosimonelli.utils.dao.impl.JgitService;

public class GitDAOFactory {
    public static GitDAO create() {
        return new JgitService();
    }

}
