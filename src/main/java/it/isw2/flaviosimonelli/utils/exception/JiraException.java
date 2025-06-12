package it.isw2.flaviosimonelli.utils.exception;

public class JiraException extends Exception {
    private final String errorType;
    private final String url;

    public JiraException(String message, String errorType, String url) {
        super(message);
        this.errorType = errorType;
        this.url = url;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "JiraException{" +
                "message='" + getMessage() + '\'' +
                ", errorType='" + errorType + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}