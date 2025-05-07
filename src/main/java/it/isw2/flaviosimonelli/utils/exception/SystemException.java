package it.isw2.flaviosimonelli.utils.exception;

/**
 * Eccezione personalizzata per errori di sistema nell'applicazione.
 */
public class SystemException extends Exception {

    /**
     * Costruttore di default
     */
    public SystemException() {
        super();
    }

    /**
     * Costruttore con messaggio di errore
     *
     * @param message Il messaggio di errore
     */
    public SystemException(String message) {
        super(message);
    }

    /**
     * Costruttore con causa
     *
     * @param cause La causa dell'eccezione
     */
    public SystemException(Throwable cause) {
        super(cause);
    }

    /**
     * Costruttore con messaggio e causa
     *
     * @param message Il messaggio di errore
     * @param cause La causa dell'eccezione
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}