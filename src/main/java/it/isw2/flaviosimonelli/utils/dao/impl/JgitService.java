package it.isw2.flaviosimonelli.utils.dao.impl;

import it.isw2.flaviosimonelli.model.Repository;
import it.isw2.flaviosimonelli.utils.dao.GitDAO;
import it.isw2.flaviosimonelli.utils.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class JgitService implements GitDAO {

    @Override
    public void cloneRepository(Repository repository) throws GitException {
        String repoUrl = repository.getUrl();
        String destinationBasePath = repository.getLocalPath();

        try {
            // Estrai il nome del repository dall'URL
            String repoName = extractRepoName(repoUrl);

            // Costruisci il path completo
            File destinationPath = new File(destinationBasePath, repoName);

            // Clona
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(destinationPath)
                    .call()
                    .close();

        } catch (GitAPIException e) {
            throw new GitException(e.getMessage());
        }
    }

    private String extractRepoName(String repoUrl) {
        // Estrai l'ultima parte dell'URL, rimuovi ".git" se presente
        String[] parts = repoUrl.split("/");
        String repoName = parts[parts.length - 1];
        if (repoName.endsWith(".git")) {
            repoName = repoName.substring(0, repoName.length() - 4);
        }
        System.out.println(repoName);
        return repoName;
    }
}
