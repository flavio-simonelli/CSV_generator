package it.isw2.flaviosimonelli.utils.dao.impl;

import it.isw2.flaviosimonelli.model.Project.Project;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class GitService {

    /**
     * Scannerizza il repository Git estraendo tutti i metodi per ogni versione
     * @param project il progetto da scannerizzare
     * @return true se la scannerizzazione è andata a buon fine, false altrimenti
     */
    public boolean scanRepository(Project project) {
        Git git = null;
        if (project.getVersionManagerURL() == null) {
            git = openRepository(project);
        } else {
            git = cloneRepository(project);
        }
        if (git == null) {
            System.err.println("Errore nell'apertura o clonazione del repository.");
            return false;
        }


        return true;
    }

    /**
     * Clone repository
     * @param project repository to clone
     */
    public Git cloneRepository(Project project) {
        Git git = null;
        try {
            // Clone the repository
            git = Git.cloneRepository()
                    .setURI(project.getVersionManagerURL())
                    .setDirectory(new File(project.getDirectory()))
                    .setBranch(project.getBranchName())
                    .call();
            System.out.println("Repository cloned to: " + project.getDirectory());
        } catch (GitAPIException e) {
            System.err.println("Error cloning repository: " + e.getMessage());
        }
        return git;
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