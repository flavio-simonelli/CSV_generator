package it.isw2.flaviosimonelli.utils.dao;

import ch.qos.logback.classic.Logger;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.*;
import it.isw2.flaviosimonelli.model.Project.Project;

import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.model.method.Method;
import it.isw2.flaviosimonelli.model.method.Metric;
import it.isw2.flaviosimonelli.utils.VersionTagger;
import it.isw2.flaviosimonelli.utils.exception.InvalidProjectParameterException;
import it.isw2.flaviosimonelli.utils.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.IOException;
import java.util.*;


public class GitService {


    private static final Logger LOGGER = (Logger) org.slf4j.LoggerFactory.getLogger(GitService.class);

    /**
     * Clone repository
     * @param gitURL URL of the Git repository to clone
     * @param gitBranch Branch to clone
     * @param gitDirectory Directory where the repository will be cloned
     */
    public void cloneRepository(String gitURL, String gitBranch, String gitDirectory) throws InvalidProjectParameterException {
        try {
            // Clone the repository
            Git.cloneRepository()
                .setURI(gitURL)
                .setBranch(gitBranch)
                .setDirectory(new File(gitDirectory))
                .call();
        } catch (GitAPIException e) {
            throw new GitException("clone", e.getMessage());
        }
    }

    /**
     * Check open an existing repository
     * @param project istance of the project to open
     */
    public void openRepository(Project project) {
        try {
            // Apre il repository esistente
            Git.open(new File(project.getGitDirectory()));
        } catch (IOException e) {
            throw new GitException("open", "Errore nell'aprire il repository: " + e.getMessage());
        }
    }

    /**
     * Ottiene il commit corrispettivo a una versione specifica
     * @param version nome della versione di cui ottenere il commit
     * @return l'ID del commit corrispondente alla versione specificata
     */
    public String getCommitByVersion(Project project, Version version) {
        // Applica la versione sulla convenzione del tag
        String versionTag = VersionTagger.applyVersion(project.getReleaseTagFormat(), version.getName());
        try (Git git = Git.open(new File(project.getGitDirectory()))) {
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
    public List<Method> getMethodsInVersion(Project project, Version version) throws Exception {
        List<Method> methods = new ArrayList<>();

        try (
             Repository repository = Git.open(new File(project.getGitDirectory())).getRepository();
             RevWalk walk = new RevWalk(repository)) {
                ObjectId commitId = repository.resolve(version.getHashCommit());
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

                        for (MethodDeclaration methodDecl : classDecl.getMethods()) {
                            String signature = methodDecl.getDeclarationAsString(false, false, false);
                            // Estrazione del body del metodo
                            BlockStmt methodBody = methodDecl.getBody().orElse(null);
                            String content = methodBody != null ? methodBody.toString() : "";
                            Method method = new Method(signature, className, path, version.getName(), content);

                            // Calcola e imposta la metrica LOC
                            int loc = calculateMethodLoc(methodDecl);
                            Metric metric = method.getMetric();
                            metric.setLoc(loc);
                            metric.setStatementsCount(getStatementCount(methodDecl));
                            metric.setCyclomaticComplexity(getCyclomaticComplexity(methodDecl));


                            methods.add(method);
                        }
                    }

                }
            }
        }

        return methods;
    }

    /**
     * Trova i commit che risolvono un ticket specifico.
     *
     * @param project Il progetto in cui cercare i commit
     * @param ticketId L'ID del ticket da cercare nei commit
     * @return L'ultimo commit che ha nel messaggio il ticketId specificato, o null se non trovato
     */
    public String findFixCommitForTicket(Project project, String ticketId) {
        ticketId = ticketId.toLowerCase();
        LOGGER.info("Finding fix commit for ticket: " + ticketId);
        RevCommit latestMatch = null;

        try (Git git = Git.open(new File(project.getGitDirectory()))) {
            Iterable<RevCommit> commits = git.log().call();

            for (RevCommit commit : commits) {
                String message = commit.getFullMessage();
                if (message != null && message.toLowerCase().contains(ticketId)) {
                    if (latestMatch == null || commit.getCommitTime() > latestMatch.getCommitTime()) {
                        latestMatch = commit;
                    }
                }
            }

            if (latestMatch != null) {
                LOGGER.info("Trovato commit: " + latestMatch.getName() + " per il ticket: " + ticketId);
                return latestMatch.getName();
            } else {
                LOGGER.info("Nessun commit trovato per il ticket: " + ticketId);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ottiene la data di commit per un commit specifico.
     *
     * @param commitHash L'hash del commit di cui ottenere la data
     * @return La data del commit, o null se non trovata
     */
    public Date getCommitDate(Project project, String commitHash) {
        try (Git git = Git.open(new File(project.getGitDirectory()))) {
            RevWalk walk = new RevWalk(git.getRepository());
            ObjectId commitId = git.getRepository().resolve(commitHash);
            RevCommit commit = walk.parseCommit(commitId);
            return new Date(commit.getCommitTime() * 1000L);
        } catch (IOException e) {
            LOGGER.error("Failed to get commit date for " + commitHash, e);
            return null;
        }
    }

    /**
     * Calcola la versione del progetto per un determinato commit.
     * @param project Il progetto per cui calcolare la versione
     * @param commitId L'ID del commit per cui calcolare la versione
     * @return La versione del progetto associata al commit, o null se non trovata
     */
    public Version getVersionForCommit(Project project, String commitId) {
        System.out.println("Cercando versione per commit: " + commitId);
        try (Git git = Git.open(new File(project.getGitDirectory()))) {
            Repository repo = git.getRepository();
            RevWalk revWalk = new RevWalk(repo);

            ObjectId startId = repo.resolve(commitId);
            if (startId == null) {
                System.out.println("Commit di partenza non trovato: " + commitId);
                return null;
            }
            RevCommit startCommit = revWalk.parseCommit(startId);
            System.out.println("Commit trovato: " + startCommit.getName() + " - " + startCommit.getShortMessage());

            // Prima cerca se il commit appartiene esattamente a una versione
            System.out.println("Numero versioni disponibili: " + project.getVersions().size());
            for (Version v : project.getVersions()) {
                System.out.println("Controllando versione: " + v.getName() + " con hash: " + v.getHashCommit());
                if (v.getHashCommit() != null && v.getHashCommit().equals(commitId)) {
                    System.out.println("Trovata corrispondenza esatta con versione: " + v.getName());
                    return v;
                }
            }

            // Cerca la prima versione dopo il commit
            Version nextVersion = null;
            Date commitDate = new Date(startCommit.getCommitTime() * 1000L);
            System.out.println("Data commit: " + commitDate);

            for (Version v : project.getVersions()) {
                if (v.getHashCommit() == null) {
                    System.out.println("Versione " + v.getName() + " senza hash commit");
                    continue;
                }

                try {
                    ObjectId versionId = repo.resolve(v.getHashCommit());
                    if (versionId == null) {
                        System.out.println("Hash non valido per versione " + v.getName() + ": " + v.getHashCommit());
                        continue;
                    }

                    RevCommit versionCommit = revWalk.parseCommit(versionId);
                    Date versionDate = new Date(versionCommit.getCommitTime() * 1000L);
                    System.out.println("Versione " + v.getName() + " data: " + versionDate);

                    // Se la versione è successiva al commit e precede altre versioni trovate
                    if (versionDate.after(commitDate) &&
                            (nextVersion == null || versionDate.before(new Date(revWalk.parseCommit(
                                    repo.resolve(nextVersion.getHashCommit())).getCommitTime() * 1000L)))) {
                        nextVersion = v;
                        System.out.println("Nuova versione candidata: " + v.getName());
                    }
                } catch (Exception e) {
                    System.out.println("Errore analizzando versione " + v.getName() + ": " + e.getMessage());
                }
            }

            revWalk.close();

            if (nextVersion != null) {
                System.out.println("Versione trovata: " + nextVersion.getName());
                return nextVersion;
            } else {
                System.out.println("Nessuna versione trovata dopo il commit " + commitId);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Errore in getVersionForCommit: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Ottiene i metodi modificati in un commit specifico.
     *
     * @param project Il progetto in cui cercare i metodi modificati
     * @param commitId L'ID del commit da analizzare
     * @return Una lista di firme dei metodi modificati nel commit
     * @throws Exception Se si verifica un errore durante l'analisi del commit
     */
    public List<String> getModifiedMethodSignaturesfromCommit(Project project, String commitId) throws Exception {
        List<String> modifiedMethods = new ArrayList<>();

        try (Git git = Git.open(new File(project.getGitDirectory()))) {
            Repository repo = git.getRepository();
            RevWalk revWalk = new RevWalk(repo);

            ObjectId commitObjectId = repo.resolve(commitId);
            RevCommit commit = revWalk.parseCommit(commitObjectId);

            if (commit.getParentCount() == 0) {
                // Primo commit: tutti i metodi sono nuovi, estraiamoli come in getMethodsInVersion
                try (TreeWalk treeWalk = new TreeWalk(repo)) {
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathSuffixFilter.create(".java"));

                    while (treeWalk.next()) {
                        String path = treeWalk.getPathString();

                        // Se vuoi escludere test, come fai nel getMethodsInVersion
                        if (path.contains("/src/test/") || path.contains("\\src\\test\\")) {
                            continue;
                        }

                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repo.open(objectId);

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

                            for (MethodDeclaration methodDecl : classDecl.getMethods()) {
                                String signature = methodDecl.getDeclarationAsString(false, false, false);
                                modifiedMethods.add(signature);
                            }
                        }
                    }
                }
                return modifiedMethods;
            }

            RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());

            // Prepara iterator per alberi
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();

            try (var reader = repo.newObjectReader()) {
                newTreeIter.reset(reader, commit.getTree());
                oldTreeIter.reset(reader, parent.getTree());
            }

            try (DiffFormatter diffFormatter = new DiffFormatter(new ByteArrayOutputStream())) {
                diffFormatter.setRepository(repo);
                List<DiffEntry> diffs = diffFormatter.scan(oldTreeIter, newTreeIter);

                for (DiffEntry diff : diffs) {
                    if (!diff.getNewPath().endsWith(".java")) {
                        continue; // solo file Java
                    }

                    // Ottieni codice sorgente vecchio e nuovo
                    String oldSource = getFileContent(repo, parent, diff.getOldPath());
                    String newSource = getFileContent(repo, commit, diff.getNewPath());

                    if (oldSource == null || newSource == null) {
                        continue;
                    }

                    // Parse Java
                    CompilationUnit oldCU = StaticJavaParser.parse(oldSource);
                    CompilationUnit newCU = StaticJavaParser.parse(newSource);

                    // Trova metodi modificati basandoti sul diff (linee modificate)
                    List<String> changedMethods = findChangedMethods(diffFormatter, diff, oldCU, newCU);

                    modifiedMethods.addAll(changedMethods);
                }
            }
        }

        return modifiedMethods;
    }

    // Funzione di supporto per leggere contenuto file a partire da commit e path
    private String getFileContent(Repository repo, RevCommit commit, String path) {
        try {
            var treeWalk = TreeWalk.forPath(repo, path, commit.getTree());
            if (treeWalk == null) return null;
            ObjectId objectId = treeWalk.getObjectId(0);
            try (var objectReader = repo.newObjectReader()) {
                byte[] data = objectReader.open(objectId).getBytes();
                return new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Trova metodi modificati usando le linee modificate del diff
    private List<String> findChangedMethods(DiffFormatter diffFormatter, DiffEntry diff, CompilationUnit oldCU, CompilationUnit newCU) throws Exception {
        List<String> modifiedMethods = new ArrayList<>();

        // Estrai le linee modificate dal diff
        var edits = diffFormatter.toFileHeader(diff).toEditList();

        // Mappa linee modificate per file nuovo
        // Per semplicità prendiamo le linee aggiunte/modificate sul file nuovo
        List<Integer> changedLines = new ArrayList<>();
        for (var edit : edits) {
            // Consideriamo solo le linee nel file nuovo (da getBeginB a getEndB)
            for (int i = edit.getBeginB(); i < edit.getEndB(); i++) {
                changedLines.add(i + 1); // le linee partono da 1
            }
        }

        // Per ogni metodo nel nuovo CU controlla se il range di linee include qualche linea modificata
        for (MethodDeclaration method : newCU.findAll(MethodDeclaration.class)) {
            int beginLine = method.getBegin().map(p -> p.line).orElse(-1);
            int endLine = method.getEnd().map(p -> p.line).orElse(-1);

            for (int line : changedLines) {
                if (line >= beginLine && line <= endLine) {
                    // Firma metodo come stringa: tipo di ritorno + nome + parametri
                    String signature = method.getDeclarationAsString(false, false, true);
                    modifiedMethods.add(signature);
                    break;
                }
            }
        }

        return modifiedMethods;
    }




    /**
     * Calcola il numero effettivo di linee di codice per un metodo.
     *
     * @param methodDecl La dichiarazione del metodo analizzato
     * @return Il conteggio delle linee di codice significative
     */
    private int calculateMethodLoc(MethodDeclaration methodDecl) {
        Optional<BlockStmt> bodyOpt = methodDecl.getBody();

        if (bodyOpt.isPresent()) {
            String bodyString = bodyOpt.get().toString();

            // Conta le righe non vuote e non di commento (semplificato)
            long loc = Arrays.stream(bodyString.split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("//"))
                    .filter(line -> !line.startsWith("/*"))
                    .filter(line -> !line.startsWith("*"))
                    .filter(line -> !line.startsWith("*/"))
                    .count();

            return (int) loc;
        } else {
            // Metodo senza corpo (es. astratto o interfaccia)
            return 0;
        }
    }

    public static int getStatementCount(MethodDeclaration methodDecl) {

        Optional<BlockStmt> bodyOpt = methodDecl.getBody();
        if (bodyOpt.isEmpty()) {
            return 0;
        }

        BlockStmt body = bodyOpt.get();

        // Contiamo tutti gli statement diretti nel blocco principale
        return (int) body.findAll(Statement.class).stream().count();
    }

    public static int getCyclomaticComplexity(MethodDeclaration methodDecl) {
        if (methodDecl.getBody().isEmpty()) {
            return 1; // valore minimo
        }

        BlockStmt body = methodDecl.getBody().get();
        int complexity = 1; // punto di partenza

        // Conta le strutture di controllo
        complexity += body.findAll(IfStmt.class).size();
        complexity += body.findAll(ForStmt.class).size();
        complexity += body.findAll(ForEachStmt.class).size();
        complexity += body.findAll(WhileStmt.class).size();
        complexity += body.findAll(DoStmt.class).size();
        complexity += body.findAll(SwitchEntry.class).stream()
                .mapToInt(entry -> entry.getLabels().isEmpty() ? 0 : 1)
                .sum(); // conta i 'case', ignora 'default'
        complexity += body.findAll(CatchClause.class).size();

        // Conta gli operatori logici && e || come rami condizionali
        complexity += (int) body.findAll(BinaryExpr.class).stream()
                .filter(expr -> expr.getOperator() == BinaryExpr.Operator.AND
                        || expr.getOperator() == BinaryExpr.Operator.OR)
                .count();

        return complexity;
    }

}