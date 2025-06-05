package it.isw2.flaviosimonelli.model.method;

/**
 * Represents a collection of metrics associated with a method.
 * This class contains various code quality, complexity, and change history metrics
 * that can be used for analysis and evaluation of method quality and evolution.
 */
public class Metric {

    // --- Size and Complexity Metrics ---
    private int loc;                     // Lines of code
    private int statementsCount;         // Number of statements
    private int cyclomaticComplexity;    // McCabe's cyclomatic complexity
    private int cognitiveComplexity;     // Cognitive complexity
    private int nestingDepth;            // Maximum nesting depth
    private int numberOfBranches;        // Number of branch points
    private int parameterCount;          // Number of parameters

    // --- Halstead Metrics ---
    private double halsteadVolume;       // Halstead volume
    private double halsteadDifficulty;   // Halstead difficulty
    private double halsteadEffort;       // Halstead effort

    // --- Quality Metrics ---
    private int numberOfCodeSmells;      // Number of code smells
    private int duplication;             // Duplication metric

    // --- Change History Metrics ---
    private int methodHistories;         // Number of change histories
    private int authors;                 // Number of distinct authors
    private int stmtAdded;               // Total statements added
    private int maxStmtAdded;            // Maximum statements added in one change
    private double avgStmtAdded;         // Average statements added per change
    private int stmtDeleted;             // Total statements deleted
    private int maxStmtDeleted;          // Maximum statements deleted in one change
    private double avgStmtDeleted;       // Average statements deleted per change
    private int churn;                   // Code churn (added + deleted)
    private int maxChurn;                // Maximum churn in one change
    private double avgChurn;             // Average churn per change
    private int cond;                    // Conditional changes
    private int elseAdded;               // Else branches added
    private int elseDeleted;             // Else branches deleted

    /**
     * Creates a new Metric object with default values (all zeros).
     */
    public Metric() {
        // Initialize with default values
    }

    /**
     * Creates a new Metric object with specified complexity metrics.
     *
     * @param cyclomaticComplexity The cyclomatic complexity
     * @param cognitiveComplexity The cognitive complexity
     */
    public Metric(int cyclomaticComplexity, int cognitiveComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
        this.cognitiveComplexity = cognitiveComplexity;
    }

    // --- Getters and Setters ---

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getStatementsCount() {
        return statementsCount;
    }

    public void setStatementsCount(int statementsCount) {
        this.statementsCount = statementsCount;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public int getCognitiveComplexity() {
        return cognitiveComplexity;
    }

    public void setCognitiveComplexity(int cognitiveComplexity) {
        this.cognitiveComplexity = cognitiveComplexity;
    }

    public double getHalsteadVolume() {
        return halsteadVolume;
    }

    public void setHalsteadVolume(double halsteadVolume) {
        this.halsteadVolume = halsteadVolume;
    }

    public double getHalsteadDifficulty() {
        return halsteadDifficulty;
    }

    public void setHalsteadDifficulty(double halsteadDifficulty) {
        this.halsteadDifficulty = halsteadDifficulty;
    }

    public double getHalsteadEffort() {
        return halsteadEffort;
    }

    public void setHalsteadEffort(double halsteadEffort) {
        this.halsteadEffort = halsteadEffort;
    }

    public int getNestingDepth() {
        return nestingDepth;
    }

    public void setNestingDepth(int nestingDepth) {
        this.nestingDepth = nestingDepth;
    }

    public int getNumberOfBranches() {
        return numberOfBranches;
    }

    public void setNumberOfBranches(int numberOfBranches) {
        this.numberOfBranches = numberOfBranches;
    }

    public int getNumberOfCodeSmells() {
        return numberOfCodeSmells;
    }

    public void setNumberOfCodeSmells(int numberOfCodeSmells) {
        this.numberOfCodeSmells = numberOfCodeSmells;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public void setParameterCount(int parameterCount) {
        this.parameterCount = parameterCount;
    }

    public int getDuplication() {
        return duplication;
    }

    public void setDuplication(int duplication) {
        this.duplication = duplication;
    }

    public int getMethodHistories() {
        return methodHistories;
    }

    public void setMethodHistories(int methodHistories) {
        this.methodHistories = methodHistories;
    }

    public int getAuthors() {
        return authors;
    }

    public void setAuthors(int authors) {
        this.authors = authors;
    }

    public int getStmtAdded() {
        return stmtAdded;
    }

    public void setStmtAdded(int stmtAdded) {
        this.stmtAdded = stmtAdded;
    }

    public int getMaxStmtAdded() {
        return maxStmtAdded;
    }

    public void setMaxStmtAdded(int maxStmtAdded) {
        this.maxStmtAdded = maxStmtAdded;
    }

    public double getAvgStmtAdded() {
        return avgStmtAdded;
    }

    public void setAvgStmtAdded(double avgStmtAdded) {
        this.avgStmtAdded = avgStmtAdded;
    }

    public int getStmtDeleted() {
        return stmtDeleted;
    }

    public void setStmtDeleted(int stmtDeleted) {
        this.stmtDeleted = stmtDeleted;
    }

    public int getMaxStmtDeleted() {
        return maxStmtDeleted;
    }

    public void setMaxStmtDeleted(int maxStmtDeleted) {
        this.maxStmtDeleted = maxStmtDeleted;
    }

    public double getAvgStmtDeleted() {
        return avgStmtDeleted;
    }

    public void setAvgStmtDeleted(double avgStmtDeleted) {
        this.avgStmtDeleted = avgStmtDeleted;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public double getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getElseAdded() {
        return elseAdded;
    }

    public void setElseAdded(int elseAdded) {
        this.elseAdded = elseAdded;
    }

    public int getElseDeleted() {
        return elseDeleted;
    }

    public void setElseDeleted(int elseDeleted) {
        this.elseDeleted = elseDeleted;
    }
}