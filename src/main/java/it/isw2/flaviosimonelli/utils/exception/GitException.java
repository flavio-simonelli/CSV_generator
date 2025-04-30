package it.isw2.flaviosimonelli.utils.exception;

public class GitException extends Exception {

    public GitException(String message) {
        super("A technical error occurred.\n" + message);
    }
}
