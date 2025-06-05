package it.isw2.flaviosimonelli.utils.dao.impl;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.*;
import it.isw2.flaviosimonelli.model.Project.Project;

import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.model.method.Method;
import it.isw2.flaviosimonelli.model.method.Metric;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class GitService {


    /**
     * Clone repository
     * @param project instance of the project to clone
     */
    public void cloneRepository(Project project) {
        try {
            // Clone the repository
            Git.cloneRepository()
                .setURI(project.getGitURL())
                .setBranch(project.getGitBranch())
                .setDirectory(new File(project.getGitDirectory()))
                .call();
            System.out.println("Repository cloned to: " + project.getGitDirectory());
        } catch (GitAPIException e) {
            System.err.println("Error cloning repository: " + e.getMessage());
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
            System.out.println("Repository aperto da: " + project.getGitDirectory());
        } catch (Exception e) {
            System.err.println("Errore nell'apertura del repository: " + e.getMessage());
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

    public static int getCognitiveComplexity(MethodDeclaration methodDecl) {
        if (methodDecl.getBody().isEmpty()) {
            return 0; // Metodo senza corpo
        }

        BlockStmt body = methodDecl.getBody().get();
        AtomicInteger complexity = new AtomicInteger(0);
        AtomicInteger nestingLevel = new AtomicInteger(0);

        body.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(IfStmt ifStmt, Void arg) {
                complexity.incrementAndGet(); // Struttura di controllo base
                complexity.addAndGet(nestingLevel.get()); // Complessità per nidificazione

                // Aggiungi complessità per condizioni complesse
                ifStmt.getCondition().accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(BinaryExpr expr, Void arg) {
                        if (expr.getOperator() == BinaryExpr.Operator.AND || expr.getOperator() == BinaryExpr.Operator.OR) {
                            complexity.incrementAndGet();
                        }
                        super.visit(expr, arg);
                    }
                }, null);

                nestingLevel.incrementAndGet();
                super.visit(ifStmt, arg);
                nestingLevel.decrementAndGet();
            }

            @Override
            public void visit(ForStmt stmt, Void arg) {
                complexity.incrementAndGet();
                complexity.addAndGet(nestingLevel.get());
                nestingLevel.incrementAndGet();
                super.visit(stmt, arg);
                nestingLevel.decrementAndGet();
            }

            @Override
            public void visit(WhileStmt stmt, Void arg) {
                complexity.incrementAndGet();
                complexity.addAndGet(nestingLevel.get());
                nestingLevel.incrementAndGet();
                super.visit(stmt, arg);
                nestingLevel.decrementAndGet();
            }

            @Override
            public void visit(TryStmt stmt, Void arg) {
                complexity.incrementAndGet(); // Complessità per blocco try
                nestingLevel.incrementAndGet();
                super.visit(stmt, arg);
                nestingLevel.decrementAndGet();
            }

            @Override
            public void visit(LambdaExpr lambdaExpr, Void arg) {
                complexity.incrementAndGet(); // Complessità per lambda
                super.visit(lambdaExpr, arg);
            }

            @Override
            public void visit(ObjectCreationExpr objCreationExpr, Void arg) {
                if (objCreationExpr.getAnonymousClassBody().isPresent()) {
                    complexity.incrementAndGet(); // Complessità per classe anonima
                }
                super.visit(objCreationExpr, arg);
            }
        }, null);

        return complexity.get();
    }

}