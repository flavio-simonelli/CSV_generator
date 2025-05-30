package it.isw2.flaviosimonelli.utils.dao.impl;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import it.isw2.flaviosimonelli.model.AnalyzedMethod;
import it.isw2.flaviosimonelli.model.Method;
import it.isw2.flaviosimonelli.model.Project.Project;

import it.isw2.flaviosimonelli.utils.VersionTagger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;


import java.io.ByteArrayInputStream;
import java.io.File;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;


public class GitService {


    /**
     * Clone repository
     * @param project istanza del progetto contenente l'URL del repository e la directory di destinazione
     */
    public void cloneRepository(Project project) {
        try {
            // Clone the repository
            Git.cloneRepository()
                .setURI(project.getGitURL())
                .setDirectory(new File(project.getGitDirectory()))
                .call();
            System.out.println("Repository cloned to: " + project.getGitDirectory());
        } catch (GitAPIException e) {
            System.err.println("Error cloning repository: " + e.getMessage());
        }
    }

    /**
     * Apre un repository Git esistente
     * @param project istanza del progetto contenente il percorso del repository
     */
    public void openRepository(Project project) {
        try {
            // Apre il repository esistente
            Git.open(new File(project.getGitDirectory()));
            System.out.println("Repository aperto da: " + project.getGitDirectory());
        } catch (Exception e) {
            System.err.println("Errore nell'apertura del repository: " + e.getMessage());
        }
    }

    /**
     * Ottiene il commit corrispettivo a una versione specifica
     * @param directory percorso della directory del repository
     * @param conventionTagName nome della convenzione del tag per le versioni
     * @param version nome della versione di cui ottenere il commit
     * @return l'ID del commit corrispondente alla versione specificata
     */
    public String getCommitByVersion(String directory, String conventionTagName ,String version) {
        // Applica la versione sulla convenzione del tag
        String versionTag = VersionTagger.applyVersion(conventionTagName, version);
        try (Git git = Git.open(new File(directory))) {
            // Ottiene il commit corrispondente alla versione specificata
            Ref ref = git.getRepository().findRef(versionTag);
            if (ref != null) {
                ObjectId objectId = ref.getObjectId();
                return objectId.getName(); // Restituisce l'ID del commit
            } else {
                System.err.println("Versione non trovata: " + versionTag);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Errore nell'apertura del repository: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ottiene la lista dei metodi di un commit specifico
     */
    public static List<Method> getMethodsInCommit(String repoPath, String commitHash, String commitName) throws Exception {
        List<Method> methods = new ArrayList<>();

        try (
             Repository repository = Git.open(new File(repoPath)).getRepository();
             Git git = new Git(repository);
             RevWalk walk = new RevWalk(repository)) {
                ObjectId commitId = repository.resolve(commitHash);
                RevCommit commit = walk.parseCommit(commitId);
                ObjectId treeId = commit.getTree().getId();

            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(treeId);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathSuffixFilter.create(".java"));

                while (treeWalk.next()) {
                    String path = treeWalk.getPathString();

                    // Escludi con precisione i file nella cartella src/test/
                    if (path.contains("/src/test/") || path.contains("\\src\\test\\")) {
                        continue;
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);

                    byte[] fileBytes = loader.getBytes();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

                    CompilationUnit cu;
                    try {
                        cu = StaticJavaParser.parse(inputStream);
                    } catch (Exception e) {
                        continue; // ignora file non validi
                    }

                    for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                        String className = classDecl.getNameAsString();

                        for (MethodDeclaration method : classDecl.getMethods()) {
                            String signature = method.getDeclarationAsString(false, false, false);
                            methods.add(new Method(signature, className, path, commitName));
                        }
                    }

                }
            }
        }

        return methods;
    }


}