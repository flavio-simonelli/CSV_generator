package it.isw2.flaviosimonelli.utils.exception;

public class InvalidProjectParameterException extends Exception {
    private final String parameterName;

    public InvalidProjectParameterException(String parameterName, String message) {
        super(message);
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    @Override
    public String toString() {
        return "InvalidProjectParameterException: parameter='" + parameterName + "', message='" + getMessage() + "'";
    }
}

