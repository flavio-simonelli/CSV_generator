package it.isw2.flaviosimonelli.model.method;

public record Metric(
        int loc,
        int statementsCount,
        int cyclomaticComplexity,
        int cognitiveComplexity,

        double halsteadVolume,
        double halsteadDifficulty,
        double halsteadEffort,

        int nestingDepth,
        int numberOfBranches,
        int numberOfCodeSmells,
        int parameterCount,
        int duplication,

        int methodHistories,
        int authors,
        int stmtAdded,
        int maxStmtAdded,
        double avgStmtAdded,
        int stmtDeleted,
        int maxStmtDeleted,
        double avgStmtDeleted,
        int churn,
        int maxChurn,
        double avgChurn,
        int cond,
        int elseAdded,
        int elseDeleted
) {}

