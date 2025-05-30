package it.isw2.flaviosimonelli.model.method;

/**
 * Interface for defining metrics that can be associated with methods.
 *
 * @param <T> The type of the metric value
 */
public interface Metric<T> {
    /**
     * @return The name of the metric
     */
    String name();

    /**
     * @return The class representing the type of the metric value
     */
    Class<T> getType();
}
