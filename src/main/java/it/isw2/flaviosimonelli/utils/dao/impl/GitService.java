package it.isw2.flaviosimonelli.utils.dao.impl;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.exception.SystemException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitService {

    /**
     * Clones or opens a Git repository
     *
     * @param reposDirectory Directory where repositories are stored
     * @param projectName Name of the project
     * @param repoUrl Repository URL
     * @return Git instance
     * @throws SystemException If an error occurs
     */
    public static Git initRepository(String reposDirectory, String projectName, String repoUrl) throws SystemException {
        try {
            File localPath = new File(reposDirectory + File.separator + projectName);

            // Create directory if it doesn't exist
            new File(reposDirectory).mkdirs();

            Git git;
            if (localPath.exists() && new File(localPath, ".git").exists()) {
                // Repository exists, open it
                Repository repository = new FileRepositoryBuilder()
                        .setGitDir(new File(localPath, ".git"))
                        .build();
                git = new Git(repository);

                // Fetch latest changes
                git.fetch().call();
            } else {
                // Clone the repository
                System.out.println("Cloning repository: " + repoUrl);
                git = Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(localPath)
                        .call();
            }
            return git;
        } catch (GitAPIException | IOException e) {
            throw new SystemException("Error initializing Git repository: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts method information from all Java files for each version
     *
     * @param reposDirectory Directory where repositories are stored
     * @param projectName Project name
     * @param repoUrl Repository URL
     * @param versions List of versions to analyze
     * @return Map of version name to list of method information
     * @throws SystemException If an error occurs
     */
    public static Map<String, List<MethodInfo>> extractMethodInfoByVersion(String reposDirectory,
                                                                           String projectName,
                                                                           String repoUrl,
                                                                           List<Version> versions) throws SystemException {
        Map<String, List<MethodInfo>> results = new HashMap<>();
        Git git = null;

        try {
            // Initialize repository
            git = initRepository(reposDirectory, projectName, repoUrl);

            for (Version version : versions) {
                String versionName = version.getName();
                System.out.println("Processing version: " + versionName);

                try {
                    // Checkout version
                    checkoutVersion(git, versionName);

                    // Extract method info for this version
                    List<MethodInfo> methodInfoList = extractAllMethodsInfo(reposDirectory, projectName);
                    results.put(versionName, methodInfoList);

                    System.out.println("Found " + methodInfoList.size() + " methods in version " + versionName);
                } catch (Exception e) {
                    System.err.println("Error processing version " + versionName + ": " + e.getMessage());
                    // Continue with next version
                }
            }

            return results;
        } finally {
            // Close resources
            if (git != null) {
                git.getRepository().close();
                git.close();
            }
        }
    }

    /**
     * Checks out a specific version/tag/branch
     *
     * @param git Git instance
     * @param version Version to checkout
     * @throws SystemException If checkout fails
     */
    private static void checkoutVersion(Git git, String version) throws SystemException {
        try {
            // First try to clean up the working directory
            git.reset().setMode(ResetCommand.ResetType.HARD).call();

            // Checkout the version
            CheckoutCommand checkout = git.checkout();
            checkout.setName("tags/" + version);

            // If tag checkout fails, try branch
            try {
                checkout.call();
            } catch (RefNotFoundException e) {
                // Try as a branch
                checkout.setName(version).call();
            }
        } catch (CheckoutConflictException e) {
            // Try force checkout
            try {
                git.clean().setForce(true).call();
                git.checkout().setName("tags/" + version).setForce(true).call();
            } catch (GitAPIException ex) {
                throw new SystemException("Failed to checkout version " + version, ex);
            }
        } catch (GitAPIException e) {
            throw new SystemException("Error checking out version " + version + ": " + e.getMessage(), e);
        }
    }

    /**
     * Extracts method information from all Java files in the project
     *
     * @param reposDirectory Directory where repositories are stored
     * @param projectName Project name
     * @return List of method information
     */
    private static List<MethodInfo> extractAllMethodsInfo(String reposDirectory, String projectName) {
        List<MethodInfo> allMethods = new ArrayList<>();
        File projectDir = new File(reposDirectory + File.separator + projectName);

        try {
            // Find all Java files
            List<File> javaFiles = findJavaFiles(projectDir);

            for (File file : javaFiles) {
                try {
                    // Parse Java file
                    CompilationUnit cu = StaticJavaParser.parse(file);

                    // Extract package name
                    String packageName = cu.getPackageDeclaration()
                            .map(pd -> pd.getName().asString())
                            .orElse("");

                    // Visit all classes and methods
                    cu.accept(new VoidVisitorAdapter<List<MethodInfo>>() {
                        @Override
                        public void visit(ClassOrInterfaceDeclaration classDecl, List<MethodInfo> collector) {
                            super.visit(classDecl, collector);

                            String className = packageName + "." + classDecl.getNameAsString();

                            // Process methods in this class
                            for (MethodDeclaration method : classDecl.getMethods()) {
                                MethodInfo methodInfo = new MethodInfo();
                                methodInfo.setClassName(className);
                                methodInfo.setMethodName(method.getNameAsString());
                                methodInfo.setLineCount(
                                        method.getEnd().get().line - method.getBegin().get().line + 1);
                                methodInfo.setParameterCount(method.getParameters().size());
                                methodInfo.setCyclomaticComplexity(calculateComplexity(method));
                                methodInfo.setReturnType(method.getType().asString());
                                methodInfo.setModifiers(method.getModifiers().toString());
                                methodInfo.setFilePath(file.getPath());

                                allMethods.add(methodInfo);
                            }
                        }
                    }, allMethods);
                } catch (IOException e) {
                    System.err.println("Error parsing file " + file.getPath() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding Java files: " + e.getMessage());
        }

        return allMethods;
    }

    /**
     * Calculate cyclomatic complexity of a method
     *
     * @param method Method declaration
     * @return Complexity score
     */
    private static int calculateComplexity(MethodDeclaration method) {
        // Simple complexity calculation based on control flow statements
        String body = method.toString();
        int complexity = 1; // Base complexity

        // Count common control flow statements
        complexity += countOccurrences(body, "if ");
        complexity += countOccurrences(body, "else ");
        complexity += countOccurrences(body, "for ");
        complexity += countOccurrences(body, "while ");
        complexity += countOccurrences(body, "case ");
        complexity += countOccurrences(body, "catch ");
        complexity += countOccurrences(body, "&&");
        complexity += countOccurrences(body, "||");

        return complexity;
    }

    /**
     * Count occurrences of a substring in a string
     *
     * @param text Text to search in
     * @param substr Substring to count
     * @return Number of occurrences
     */
    private static int countOccurrences(String text, String substr) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substr, index)) != -1) {
            count++;
            index += substr.length();
        }
        return count;
    }

    /**
     * Finds all Java files in a directory recursively
     *
     * @param directory Directory to search
     * @return List of Java files
     * @throws IOException If an I/O error occurs
     */
    private static List<File> findJavaFiles(File directory) throws IOException {
        List<File> javaFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(directory.getPath()))) {
            javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }

        return javaFiles;
    }

    /**
     * Class to hold method information
     */
    public static class MethodInfo {
        private String className;
        private String methodName;
        private int lineCount;
        private int parameterCount;
        private int cyclomaticComplexity;
        private String returnType;
        private String modifiers;
        private String filePath;

        // Getters and setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }

        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }

        public int getParameterCount() { return parameterCount; }
        public void setParameterCount(int parameterCount) { this.parameterCount = parameterCount; }

        public int getCyclomaticComplexity() { return cyclomaticComplexity; }
        public void setCyclomaticComplexity(int cyclomaticComplexity) { this.cyclomaticComplexity = cyclomaticComplexity; }

        public String getReturnType() { return returnType; }
        public void setReturnType(String returnType) { this.returnType = returnType; }

        public String getModifiers() { return modifiers; }
        public void setModifiers(String modifiers) { this.modifiers = modifiers; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        @Override
        public String toString() {
            return "Method: " + className + "." + methodName +
                    " (lines: " + lineCount +
                    ", parameters: " + parameterCount +
                    ", complexity: " + cyclomaticComplexity + ")";
        }
    }
}