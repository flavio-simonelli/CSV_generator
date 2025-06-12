package it.isw2.flaviosimonelli.utils.dao;

import ch.qos.logback.classic.Logger;
import com.github.javaparser.ParseProblemException;
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
import it.isw2.flaviosimonelli.utils.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Servizio per operazioni su repository Git.
 * Tutti i metodi pubblici lanciano solo GitException.
 */
public class GitService {

    private static final Logger LOGGER = (Logger) org.slf4j.LoggerFactory.getLogger(GitService.class);

    /**
     * Clona un repository Git in una directory specificata.
     *
     * @param gitURL      URL del repository Git da clonare
     * @param gitBranch   Branch da clonare
     * @param gitDirectory Directory di destinazione per il clone
     * @throws GitException in caso di errore durante il clone
     */
    public void cloneRepository(String gitURL, String gitBranch, String gitDirectory) throws GitException {
        try {
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
     * Apre un repository Git esistente e cambia al branch specificato.
     *
     * @param gitDirectory Directory del repository Git
     * @param gitBranch    Branch da cui lavorare
     * @throws GitException in caso di errore durante l'apertura del repository o il checkout del branch
     */
    public void openRepository(String gitDirectory, String gitBranch) throws GitException {
        try {
            Git git = Git.open(new File(gitDirectory));
            git.checkout().setName(gitBranch).call();
            git.close();
        } catch (IOException | GitAPIException e) {
            throw new GitException("open", "Errore nell'aprire il repository: " + e.getMessage());
        }
    }

    /**
     * Recupera l'hash del commit associato a una versione specifica.
     *
     * @param project Il progetto da cui recuperare il commit
     * @param version La versione di cui si vuole ottenere l'hash del commit
     * @return L'hash del commit associato alla versione, o null se non trovato
     * @throws GitException in caso di errore durante l'operazione
     */
    public String getCommitByVersion(Project project, Version version) throws GitException {
        // Applica il formato del tag di rilascio per ottenere il nome del tag
        String versionTag = VersionTagger.applyVersion(project.getReleaseTagFormat(), version.getName());
        // Apre il repository git
        try (Git git = Git.open(new File(project.getGitDirectory()))) {
            // Trova il riferimento del tag nel repository
            Ref ref = git.getRepository().findRef(versionTag);
            if (ref != null) {
                // Se il riferimento esiste, ottiene l'ObjectId del commit associato al tag
                ObjectId objectId = ref.getObjectId();
                // Restituisce il nome del commit (hash)
                return objectId.getName();
            } else {
                // Se il riferimento non esiste, restituisce null
                return null;
            }
        } catch (IOException e) {
            throw new GitException("getCommitByVersion", "Errore nell'aprire il repository: " + e.getMessage());
        }
    }

    /**
     * Estrae tutti i metodi Java presenti in una specifica versione del progetto.
     *
     * @param project Il progetto da analizzare.
     * @param version La versione del progetto da cui estrarre i metodi.
     * @return Una lista di metodi presenti nella versione specificata.
     * @throws GitException Se si verifica un errore durante l'accesso al repository Git.
     */
    public List<Method> getMethodsInVersion(Project project, Version version) throws GitException {
        // Lista per memorizzare i metodi estratti
        List<Method> methods = new ArrayList<>();

        try (Repository repository = Git.open(new File(project.getGitDirectory())).getRepository();
             RevWalk revWalk = new RevWalk(repository)) {
            // va a cercare il commit associato alla versione
            RevCommit commit = revWalk.parseCommit(repository.resolve(version.getHashCommit()));
            // Ottiene l'ID dell'albero del commit
            ObjectId treeId = commit.getTree().getId();

            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                // Configura il TreeWalk per camminare nell'albero del commit
                treeWalk.addTree(treeId);
                // Imposta il TreeWalk per essere ricorsivo e filtrare solo i file .java
                treeWalk.setRecursive(true);
                // Filtra i file per estensione .java
                treeWalk.setFilter(PathSuffixFilter.create(".java"));
                // Itera attraverso i file trovati
                while (treeWalk.next()) {
                    // Ottiene il percorso del file corrente
                    String filePath = treeWalk.getPathString();
                    // Ignora i file di test
                    if (isTestFile(filePath)) continue;
                    // Estrae i metodi dal file corrente
                    List<Method> extracted = extractMethodsFromFile(repository, treeWalk.getObjectId(0), filePath, version);
                    // Aggiunge i metodi estratti alla lista principale
                    methods.addAll(extracted);
                }
            }
        } catch (IOException | ParseProblemException e) {
            throw new GitException("getMethodsInVersion", "Errore durante l'estrazione dei metodi: " + e.getMessage());
        }

        return methods;
    }

    /**
     * Verifica se il file è un test (basato sul path).
     */
    private boolean isTestFile(String path) {
        return path.contains("/src/test/") || path.contains("\\src\\test\\");
    }

    /**
     * Estrae i metodi Java da un file specifico nel repository.
     */
    private List<Method> extractMethodsFromFile(Repository repository, ObjectId objectId, String filePath, Version version) {
        // Lista per memorizzare i metodi estratti dal file
        List<Method> methods = new ArrayList<>();

        try {
            // Apre il file dal repository e lo legge come byte array
            byte[] fileBytes = repository.open(objectId).getBytes();
            // Converte il byte array in un InputStream per l'analisi
            CompilationUnit cu = StaticJavaParser.parse(new ByteArrayInputStream(fileBytes));
            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                // Per ogni dichiarazione di classe, ottiene il nome della classe
                String className = classDecl.getNameAsString();
                for (MethodDeclaration methodDecl : classDecl.getMethods()) {
                    // Per ogni dichiarazione di metodo, costruisce l'oggetto Method
                    Method method = buildMethodObject(methodDecl, className, filePath, version);
                    methods.add(method);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Impossibile analizzare il file {}: {}", filePath, e.getMessage());
        }

        return methods;
    }

    /**
     * Crea l'oggetto Method a partire dalla dichiarazione del metodo.
     */
    private Method buildMethodObject(MethodDeclaration methodDecl, String className, String path, Version version) {
        // Ottiene la firma del metodo come stringa, senza parametri e senza tipo di ritorno
        String signature = methodDecl.getDeclarationAsString(false, false, false);
        // Ottiene il corpo del metodo come stringa, se presente
        String content = methodDecl.getBody().map(BlockStmt::toString).orElse("");
        // Costruisce il classPath come path + "/" + className
        String classPath = path + "/" + className;

        Method method = new Method(signature, classPath, version.getName(), content);

        // TODO: Modificare per mettere una funzione privata che calcola le metriche e aggiungere tutte le metriche da calcolare
        Metric metric = method.getMetric();
        metric.setLoc(calculateMethodLoc(methodDecl));
        metric.setStatementsCount(getStatementCount(methodDecl));
        metric.setCyclomaticComplexity(getCyclomaticComplexity(methodDecl));

        return method;
    }

    /**
     * Trova l'ultimo commit che ha risolto un ticket specifico.
     *
     * @param project Il progetto in cui cercare il commit
     * @param ticketId L'ID del ticket da cercare
     * @return L'hash del commit che ha risolto il ticket, o null se non trovato
     * @throws GitException in caso di errore durante l'operazione
     */
    public String findFixCommitForTicket(Project project, String ticketId) throws GitException {
        // Normalizza l'ID del ticket per evitare problemi di case sensitivity
        final String normalizedTicketId = ticketId.toUpperCase(Locale.ROOT);
        // Definisce i pattern per la ricerca dei commit (strong pattern per commit con parola fix)
        final Pattern strongPattern = Pattern.compile(
                "\\b(fix(?:es|ed)?|resolve(?:s|d)?|close(?:s|d)?)\\b.*\\b" + Pattern.quote(normalizedTicketId) + "\\b",
                Pattern.CASE_INSENSITIVE
        );
        // Fallback pattern per commit che menzionano il ticket
        final Pattern containsPattern = Pattern.compile(
                "\\b" + Pattern.quote(normalizedTicketId) + "\\b",
                Pattern.CASE_INSENSITIVE
        );

        try (Git git = Git.open(new File(project.getGitDirectory()))) {
            // Recupera il log dei commit del repository
            Iterable<RevCommit> commits = git.log().call();
            RevCommit strongMatch = null;
            RevCommit weakMatch = null;

            for (RevCommit commit : commits) {
                // Ottiene il messaggio completo del commit
                String message = commit.getFullMessage();
                // Se il messaggio è null, salta al prossimo commit
                if (message == null) continue;

                if (strongPattern.matcher(message).find()) {
                    // Se il commit corrisponde al pattern forte, verifica se è il più recente fra quelli forti
                    if (strongMatch == null || commit.getCommitTime() > strongMatch.getCommitTime()) {
                        strongMatch = commit;
                    }
                } else if (containsPattern.matcher(message).find()) {
                    // Se il commit corrisponde al pattern debole, verifica se è il più recente fra quelli deboli
                    if (weakMatch == null || commit.getCommitTime() > weakMatch.getCommitTime()) {
                        weakMatch = commit;
                    }
                }
            }

            if (strongMatch != null) {
                // Se è stato trovato un commit forte, restituisce il suo nome
                LOGGER.info("Commit forte trovato: {}", strongMatch.getName());
                return strongMatch.getName();
            } else if (weakMatch != null) {
                // Se non è stato trovato un commit forte, ma uno debole, restituisce il suo nome
                LOGGER.info("Commit debole trovato: {}", weakMatch.getName());
                return weakMatch.getName();
            } else {
                // Se non è stato trovato nessun commit, logga l'informazione e restituisce null
                LOGGER.warn("Nessun commit trovato per il ticket: {}", normalizedTicketId);
                return null;
            }

        } catch (FileNotFoundException | NoHeadException e) {
            throw new GitException("findFixCommitForTicket", "Repository Git non inizializzato correttamente: " + e.getMessage());
        } catch (IOException e) {
            throw new GitException("findFixCommitForTicket", "Errore I/O durante l'accesso al repository: " + e.getMessage());
        } catch (GitAPIException e) {
            throw new GitException("findFixCommitForTicket", "Errore Git API durante l'esecuzione del log: " + e.getMessage());
        } catch (Exception e) {
            // Catch finale per errori inaspettati, ma loggato e tracciato correttamente
            LOGGER.error("Errore imprevisto nella ricerca commit per ticket {}: {}", normalizedTicketId, e.getMessage(), e);
            throw new GitException("findFixCommitForTicket", "Errore imprevisto: " + e.getMessage());
        }
    }

    /**
     * Recupera la data del commit associato a un hash specifico.
     *
     * @param project Il progetto da cui recuperare il commit
     * @param commitHash L'hash del commit di cui si vuole ottenere la data
     * @return La data del commit
     * @throws GitException in caso di errore durante l'operazione
     */
    public Date getCommitDate(Project project, String commitHash) throws GitException {
        try (Git git = Git.open(new File(project.getGitDirectory()))) {
            // Crea un RevWalk per camminare attraverso i commit
            RevWalk walk = new RevWalk(git.getRepository());
            // Risolve l'hash del commit in un ObjectId
            ObjectId commitId = git.getRepository().resolve(commitHash);
            //ottiene il commit associato all'ObjectId
            RevCommit commit = walk.parseCommit(commitId);

            return new Date(commit.getCommitTime() * 1000L);
        } catch (IOException e) {
            throw new GitException("getCommitDate", e.getMessage());
        }
    }

    /**
     * Trova la versione più vicina a un commit specifico.
     *
     * @param project Il progetto da cui recuperare le versioni
     * @param commitId L'hash del commit di cui si vuole trovare la versione
     * @return La versione più vicina al commit, o null se non trovata
     * @throws GitException in caso di errore durante l'operazione
     */
    /**
     * Restituisce la versione del progetto corrispondente a un commit, oppure la successiva se non trovata direttamente.
     *
     * @param project  Il progetto
     * @param commitId L'hash del commit da cercare
     * @return La Version associata o la prima Version successiva in ordine cronologico
     * @throws GitException In caso di errore durante l'accesso al repository
     */
    public Version getVersionForCommit(Project project, String commitId) throws GitException {
        try (Git git = Git.open(new File(project.getGitDirectory()));
             // Crea un RevWalk per camminare attraverso i commit
             Repository repo = git.getRepository();
             RevWalk revWalk = new RevWalk(repo)) {

            // Risolve l'hash del commit in un ObjectId
            ObjectId targetId = repo.resolve(commitId);
            if (targetId == null) {
                // Se l'hash del commit non è valido, logga un avviso e restituisce null
                LOGGER.warn("Commit non trovato: {}", commitId);
                return null;
            }

            // Ottiene il commit associato all'ObjectId
            RevCommit targetCommit = revWalk.parseCommit(targetId);
            // Converte il tempo del commit in un oggetto Instant per confronti temporali
            Instant commitInstant = Instant.ofEpochSecond(targetCommit.getCommitTime());

            // 1. Primo controllo: match diretto per hash
            for (Version version : project.getVersions()) {
                if (commitId.equals(version.getHashCommit())) {
                    return version;
                }
            }

            // 2. Fallback: trova la prima versione con commit successivo
            Version nextVersion = null;
            Instant nextInstant = null;

            for (Version version : project.getVersions()) {
                String versionHash = version.getHashCommit();
                if (versionHash == null) continue;

                try {
                    ObjectId versionId = repo.resolve(versionHash);
                    if (versionId == null) continue;

                    RevCommit versionCommit = revWalk.parseCommit(versionId);
                    Instant versionInstant = Instant.ofEpochSecond(versionCommit.getCommitTime());

                    if (versionInstant.isAfter(commitInstant)) {
                        if (nextInstant == null || versionInstant.isBefore(nextInstant)) {
                            nextVersion = version;
                            nextInstant = versionInstant;
                        }
                    }
                } catch (IOException e) {
                    LOGGER.warn("Errore durante l'elaborazione della versione {}: {}", version.getName(), e.getMessage());
                }
            }

            return nextVersion;

        } catch (IOException e) {
            throw new GitException("getVersionForCommit", "Errore di I/O o Git: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new GitException("getVersionForCommit", "Commit ID non valido: " + commitId);
        }
    }

    /**
     * Recupera le firme dei metodi modificati in un commit specifico.
     *
     * @param project Il progetto da cui recuperare i metodi
     * @param commitId L'hash del commit da analizzare
     * @return Una lista di firme dei metodi modificati
     * @throws GitException in caso di errore durante l'operazione
     */
    public List<String> getModifiedMethodSignaturesFromCommit(Project project, String commitId) throws GitException {
        List<String> modifiedSignatures = new ArrayList<>();

        try (Git git = Git.open(new File(project.getGitDirectory()));
             Repository repo = git.getRepository();
             RevWalk revWalk = new RevWalk(repo)) {

            ObjectId commitObjectId = repo.resolve(commitId);
            if (commitObjectId == null) {
                LOGGER.warn("Commit non trovato: {}", commitId);
                return modifiedSignatures;
            }

            RevCommit commit = revWalk.parseCommit(commitObjectId);

            if (commit.getParentCount() == 0) {
                // Primo commit → estrai tutto
                RevTree tree = commit.getTree();
                try (TreeWalk treeWalk = new TreeWalk(repo)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathSuffixFilter.create(".java"));

                    while (treeWalk.next()) {
                        String path = treeWalk.getPathString();
                        if (isTestFile(path)) continue;

                        List<Method> methods = extractMethodsFromFile(repo, treeWalk.getObjectId(0), path, null);
                        methods.stream()
                                .map(Method::getSignature)
                                .forEach(modifiedSignatures::add);
                    }
                }
                return modifiedSignatures;
            }

            RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());

            try (ObjectReader reader = repo.newObjectReader()) {
                CanonicalTreeParser newTree = new CanonicalTreeParser(null, reader, commit.getTree());
                CanonicalTreeParser oldTree = new CanonicalTreeParser(null, reader, parent.getTree());

                try (DiffFormatter diffFormatter = new DiffFormatter(new ByteArrayOutputStream())) {
                    diffFormatter.setRepository(repo);
                    List<DiffEntry> diffs = diffFormatter.scan(oldTree, newTree);

                    for (DiffEntry diff : diffs) {
                        if (!diff.getNewPath().endsWith(".java") || isTestFile(diff.getNewPath())) continue;

                        String newSource = getFileContent(repo, commit, diff.getNewPath());
                        if (newSource == null) continue;

                        CompilationUnit newCU;
                        try {
                            newCU = StaticJavaParser.parse(newSource);
                        } catch (Exception e) {
                            LOGGER.warn("Errore nel parsing di {}: {}", diff.getNewPath(), e.getMessage());
                            continue;
                        }

                        List<Integer> changedLines = getChangedLines(diffFormatter, diff);
                        for (MethodDeclaration method : newCU.findAll(MethodDeclaration.class)) {
                            int begin = method.getBegin().map(Position::line).orElse(-1);
                            int end = method.getEnd().map(Position::line).orElse(-1);
                            for (int line : changedLines) {
                                if (line >= begin && line <= end) {
                                    modifiedSignatures.add(method.getDeclarationAsString(false, false, true));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new GitException("getModifiedMethodSignaturesFromCommit", "Errore I/O: " + e.getMessage());
        } catch (Exception e) {
            throw new GitException("getModifiedMethodSignaturesFromCommit", "Errore generico: " + e.getMessage());
        }

        return modifiedSignatures;
    }


    private List<Integer> getChangedLines(DiffFormatter diffFormatter, DiffEntry diff) throws IOException {
        List<Integer> changedLines = new ArrayList<>();
        for (var edit : diffFormatter.toFileHeader(diff).toEditList()) {
            for (int i = edit.getBeginB(); i < edit.getEndB(); i++) {
                changedLines.add(i + 1); // linee in formato 1-based
            }
        }
        return changedLines;
    }


    private int calculateMethodLoc(MethodDeclaration methodDecl) {
        Optional<BlockStmt> bodyOpt = methodDecl.getBody();
        if (bodyOpt.isPresent()) {
            String bodyString = bodyOpt.get().toString();
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
            return 0;
        }
    }

    public static int getStatementCount(MethodDeclaration methodDecl) {
        Optional<BlockStmt> bodyOpt = methodDecl.getBody();
        if (bodyOpt.isEmpty()) {
            return 0;
        }
        BlockStmt body = bodyOpt.get();
        return (int) body.findAll(Statement.class).stream().count();
    }

    public static int getCyclomaticComplexity(MethodDeclaration methodDecl) {
        if (methodDecl.getBody().isEmpty()) {
            return 1;
        }
        BlockStmt body = methodDecl.getBody().get();
        int complexity = 1;
        complexity += body.findAll(IfStmt.class).size();
        complexity += body.findAll(ForStmt.class).size();
        complexity += body.findAll(ForEachStmt.class).size();
        complexity += body.findAll(WhileStmt.class).size();
        complexity += body.findAll(DoStmt.class).size();
        complexity += body.findAll(SwitchEntry.class).stream()
                .mapToInt(entry -> entry.getLabels().isEmpty() ? 0 : 1)
                .sum();
        complexity += (int) body.findAll(BinaryExpr.class).stream()
                .filter(expr -> expr.getOperator() == BinaryExpr.Operator.AND
                        || expr.getOperator() == BinaryExpr.Operator.OR)
                .count();
        return complexity;
    }
}