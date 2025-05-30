package it.isw2.flaviosimonelli.model.method;

import java.util.Objects;

/**
 * Standard set of metrics for software methods analysis with type safety built in.
 * Each metric has a well-defined type without requiring unsafe casts.
 */
public enum StandardMetric implements Metric<Object> {
    /** Lines of code metric */
    LOC {
        @Override
        public Class<Object> getType() {
            return (Class<Object>) Double.class;
        }

        @Override
        public <R> R accept(MetricVisitor<R> visitor) {
            return visitor.visitDoubleMetric(this);
        }
    },

    /** Number of commits affecting this method */
    NUM_COMMITS {
        @Override
        public Class<Object> getType() {
            return (Class<Object>) Integer.class;
        }

        @Override
        public <R> R accept(MetricVisitor<R> visitor) {
            return visitor.visitIntegerMetric(this);
        }
    },

    /** Number of methods that call this method */
    FAN_IN {
        @Override
        public Class<Object> getType() {
            return (Class<Object>) Integer.class;
        }

        @Override
        public <R> R accept(MetricVisitor<R> visitor) {
            return visitor.visitIntegerMetric(this);
        }
    },

    /** Number of methods this method calls */
    FAN_OUT {
        @Override
        public Class<Object> getType() {
            return (Class<Object>) Integer.class;
        }

        @Override
        public <R> R accept(MetricVisitor<R> visitor) {
            return visitor.visitIntegerMetric(this);
        }
    },

    /** Whether the method is marked as deprecated */
    IS_DEPRECATED {
        @Override
        public Class<Object> getType() {
            return (Class<Object>) Boolean.class;
        }

        @Override
        public <R> R accept(MetricVisitor<R> visitor) {
            return visitor.visitBooleanMetric(this);
        }
    },

    /** Developer who last modified this method */
    LAST_MODIFIED_BY {
        @Override
        public Class<Object> getType() {
            return (Class<Object>) String.class;
        }

        @Override
        public <R> R accept(MetricVisitor<R> visitor) {
            return visitor.visitStringMetric(this);
        }
    };

    /**
     * Accepts a metric visitor for type-safe operations
     * @param visitor the visitor to accept
     * @param <R> the return type of the visitor
     * @return the result of the visitor operation
     */
    public abstract <R> R accept(MetricVisitor<R> visitor);

    /**
     * Visitor interface for type-safe metric operations
     */
    public interface MetricVisitor<R> {
        R visitDoubleMetric(StandardMetric metric);
        R visitIntegerMetric(StandardMetric metric);
        R visitBooleanMetric(StandardMetric metric);
        R visitStringMetric(StandardMetric metric);
    }

    /**
     * Checks if the given value is compatible with this metric's type
     * @param value the value to check
     * @return true if the value is compatible
     */
    public boolean isCompatibleWith(Object value) {
        if (value == null) return true;
        return accept(new MetricVisitor<Boolean>() {
            @Override
            public Boolean visitDoubleMetric(StandardMetric metric) {
                return value instanceof Double;
            }

            @Override
            public Boolean visitIntegerMetric(StandardMetric metric) {
                return value instanceof Integer;
            }

            @Override
            public Boolean visitBooleanMetric(StandardMetric metric) {
                return value instanceof Boolean;
            }

            @Override
            public Boolean visitStringMetric(StandardMetric metric) {
                return value instanceof String;
            }
        });
    }
}