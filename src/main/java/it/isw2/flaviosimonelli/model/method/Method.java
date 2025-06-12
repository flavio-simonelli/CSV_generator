package it.isw2.flaviosimonelli.model.method;

/**
 * Represents a method within the system, with its metrics and information.
 * This class contains information about the method signature, its containing class,
 * the file path, and associated metrics.
 */
public class Method {
    private final String signature;    // The method signature (name and parameters)
    private final String classPath;    // The name of the class containing this method
    private final String version;      // The version of the project al quale appartiene il metodo
    private final String content;
    private final Metric metric;       // The metrics associated with this method
    private Boolean Buggy; // Indicates if the method is buggy or not, default is false

    /**
     * Constructs a new Method object with all required information.
     *
     * @param signature The method signature (name and parameters)
     * @param classPath The path of the class containing this method
     * @param metric    The metrics associated with this method
     * @param content   The content of the method (e.g., source code)
     * @param version   The version project al quale appartiene il metodo
     */
    public Method(String signature, String classPath, String version, String content, Metric metric) {
        this.signature = signature;
        this.classPath = classPath;
        this.content = content;
        this.version = version;
        this.metric = metric;
        this.Buggy = false; // Default value for Buggy
    }

    /**
     * Constructs a new Method object with all required information, without metric.
     *
     * @param signature The method signature (name and parameters)
     * @param classPath The path of the class containing this method
     * @param content   The content of the method (e.g., source code)
     * @param version   The version of the method al quale appartiene il metodo
     */
    public Method(String signature, String classPath, String version, String content) {
        this.signature = signature;
        this.classPath = classPath;
        this.version = version;
        this.content = content;
        this.metric = new Metric(); // Initialize with default metrics
        this.Buggy = false; // Default value for Buggy
    }

    /**
     * Returns the method signature.
     *
     * @return The method signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Returns the name of the class containing this method.
     *
     * @return The class name
     */
    public String getClassPath() {
        return classPath;
    }


    /**
     * Returns the content of the method.
     *
     * @return The content of the method
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the version of the method, if applicable.
     *
     * @return The version of the method
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the metrics associated with this method.
     *
     * @return The Metric object containing all metrics for this method
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * Returns whether the method is considered buggy.
     *
     * @return true if the method is buggy, false otherwise
     */
    public Boolean isBuggy() {
        return Buggy;
    }

    /**
     * Sets whether the method is considered buggy.
     *
     * @param buggy true if the method is buggy, false otherwise
     */
    public void setBuggy(Boolean buggy) {
        this.Buggy = buggy;
    }
}