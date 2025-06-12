package it.isw2.flaviosimonelli.utils.exception;

public class GitException extends RuntimeException {
    private final String operation;

    public GitException(String operation, String message) {
        super("Operazione Git fallita [" + operation + "]: " + message);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
