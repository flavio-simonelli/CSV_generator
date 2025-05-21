package it.isw2.flaviosimonelli.utils.dao.impl;

import it.isw2.flaviosimonelli.model.Project;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class GitService {

    /**
     * Clone repository
     * @param project repository to clone
     */
    public void cloneRepository(Project project) {
        try {
            // Clone the repository
            Git git = Git.cloneRepository()
                    .setURI(project.getVersionManagerURL())
                    .setDirectory(new File(project.getDirectory()))
                    .setBranch(project.getBranchName())
                    .call();
            System.out.println("Repository cloned to: " + project.getDirectory());
        } catch (GitAPIException e) {
            System.err.println("Error cloning repository: " + e.getMessage());
        }
    }

    /**
     * Apre un repository Git esistente
     * @param project il progetto da aprire
     * @return l'oggetto Git che rappresenta il repository
     */
    public Git openRepository(Project project) {
        String directory = project.getDirectory();
        try {
            // Apre il repository esistente
            Git git = Git.open(new File(directory));
            System.out.println("Repository aperto da: " + directory);
            return git;
        } catch (Exception e) {
            System.err.println("Errore nell'apertura del repository: " + e.getMessage());
            return null;
        }
    }


}