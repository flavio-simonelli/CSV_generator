package it.isw2.flaviosimonelli.model.method;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable representation of a method with associated metrics.
 * This class uses the Builder pattern to create instances with various properties.
 */
public final class Method {
    private final String signature;
    private final String className;
    private final String path;
    private final String version;
    private final Map<Metric<?>, Object> metrics;

    /**
     * Private constructor used by the Builder.
     *
     * @param builder The builder instance containing the method properties
     */
    private Method(Builder builder) {
        this.signature = builder.signature;
        this.className = builder.className;
        this.path = builder.path;
        this.version = builder.version;
        this.metrics = Collections.unmodifiableMap(new HashMap<>(builder.metrics));
    }

    /**
     * Direct constructor for simple method instances without metrics.
     *
     * @param signature Method signature
     * @param className Class containing the method
     * @param path File path to the class
     * @param version Version identifier
     */
    public Method(String signature, String className, String path, String version) {
        this.signature = signature;
        this.className = className;
        this.path = path;
        this.version = version;
        this.metrics = Collections.emptyMap();
    }

    /**
     * @return The method signature
     */
    public String getSignature() { return signature; }

    /**
     * @return The class name containing this method
     */
    public String getClassName() { return className; }

    /**
     * @return The file path to the class
     */
    public String getPath() { return path; }

    /**
     * @return The version identifier
     */
    public String getVersion() { return version; }

    /**
     * Retrieves a specific metric value.
     *
     * @param <T> The type of the metric value
     * @param metric The metric to retrieve
     * @return The value of the specified metric, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetric(Metric<T> metric) {
        return (T) metrics.get(metric);
    }

    /**
     * @return An unmodifiable map of all metrics
     */
    public Map<Metric<?>, Object> getAllMetrics() {
        return metrics;
    }

    /**
     * Convenience method to get Lines of Code metric.
     * @return LOC value or null if not set
     */
    public Double getLoc() {
        return getMetric(StandardMetric.LOC.typed());
    }

    /**
     * Convenience method to get Number of Commits metric.
     * @return NUM_COMMITS value or null if not set
     */
    public Integer getNumCommits() {
        return getMetric(StandardMetric.NUM_COMMITS.typed());
    }

    /**
     * Convenience method to get Fan-In metric.
     * @return FAN_IN value or null if not set
     */
    public Integer getFanIn() {
        return getMetric(StandardMetric.FAN_IN.typed());
    }

    /**
     * Convenience method to get Fan-Out metric.
     * @return FAN_OUT value or null if not set
     */
    public Integer getFanOut() {
        return getMetric(StandardMetric.FAN_OUT.typed());
    }

    /**
     * Convenience method to check if method is deprecated.
     * @return IS_DEPRECATED value or null if not set
     */
    public Boolean isDeprecated() {
        return getMetric(StandardMetric.IS_DEPRECATED.typed());
    }

    /**
     * Convenience method to get last modifier.
     * @return LAST_MODIFIED_BY value or null if not set
     */
    public String getLastModifiedBy() {
        return getMetric(StandardMetric.LAST_MODIFIED_BY.typed());
    }

    @Override
    public String toString() {
        return "Method{" +
                "signature='" + signature + '\'' +
                ", className='" + className + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", metrics=" + metrics +
                '}';
    }

    /**
     * Builder for creating Method instances with custom properties.
     */
    public static class Builder {
        private String signature;
        private String className;
        private String path;
        private String version;
        private final Map<Metric<?>, Object> metrics = new HashMap<>();

        /**
         * Sets the method signature.
         *
         * @param signature The method signature
         * @return This builder instance
         */
        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }

        /**
         * Sets the class name.
         *
         * @param className The class name
         * @return This builder instance
         */
        public Builder className(String className) {
            this.className = className;
            return this;
        }

        /**
         * Sets the file path.
         *
         * @param path The file path
         * @return This builder instance
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the version identifier.
         *
         * @param version The version identifier
         * @return This builder instance
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Adds a metric with its value.
         *
         * @param <T> The type of the metric value
         * @param metric The metric to add
         * @param value The value of the metric
         * @return This builder instance
         */
        public <T> Builder metric(Metric<T> metric, T value) {
            return metric(metric, value, false);
        }

        /**
         * Adds a metric with its value, with an option to override existing values.
         *
         * @param <T> The type of the metric value
         * @param metric The metric to add
         * @param value The value of the metric
         * @param override Whether to override an existing value
         * @return This builder instance
         * @throws IllegalStateException If the metric already exists and override is false
         * @throws IllegalArgumentException If the value type doesn't match the metric's type
         */
        public <T> Builder metric(Metric<T> metric, T value, boolean override) {
            if (!override && metrics.containsKey(metric)) {
                throw new IllegalStateException("Metric " + metric + " has already been set.");
            }
            if (value != null && !metric.getType().isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException("Invalid type for metric " + metric.name());
            }
            metrics.put(metric, value);
            return this;
        }

        /**
         * Convenience method to set Lines of Code metric.
         * @param loc The Lines of Code value
         * @return This builder instance
         */
        public Builder loc(Double loc) {
            return metric(StandardMetric.LOC.typed(), loc);
        }

        /**
         * Convenience method to set Number of Commits metric.
         * @param numCommits The Number of Commits value
         * @return This builder instance
         */
        public Builder numCommits(Integer numCommits) {
            return metric(StandardMetric.NUM_COMMITS.typed(), numCommits);
        }

        /**
         * Convenience method to set Fan-In metric.
         * @param fanIn The Fan-In value
         * @return This builder instance
         */
        public Builder fanIn(Integer fanIn) {
            return metric(StandardMetric.FAN_IN.typed(), fanIn);
        }

        /**
         * Convenience method to set Fan-Out metric.
         * @param fanOut The Fan-Out value
         * @return This builder instance
         */
        public Builder fanOut(Integer fanOut) {
            return metric(StandardMetric.FAN_OUT.typed(), fanOut);
        }

        /**
         * Convenience method to set Is Deprecated metric.
         * @param isDeprecated The Is Deprecated value
         * @return This builder instance
         */
        public Builder deprecated(Boolean isDeprecated) {
            return metric(StandardMetric.IS_DEPRECATED.typed(), isDeprecated);
        }

        /**
         * Convenience method to set Last Modified By metric.
         * @param lastModifiedBy The Last Modified By value
         * @return This builder instance
         */
        public Builder lastModifiedBy(String lastModifiedBy) {
            return metric(StandardMetric.LAST_MODIFIED_BY.typed(), lastModifiedBy);
        }

        /**
         * Builds a new Method instance.
         *
         * @return A new Method instance
         */
        public Method build() {
            return new Method(this);
        }
    }
}