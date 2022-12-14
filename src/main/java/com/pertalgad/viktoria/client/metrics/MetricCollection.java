package com.pertalgad.viktoria.client.metrics;


import com.pertalgad.viktoria.client.serialization.PrometheusSerializationStrategy;
import com.pertalgad.viktoria.client.serialization.SerializationStrategy;
import com.pertalgad.viktoria.client.validator.MetricNameValidator;

import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Collection of grouped metrics
 */
public final class MetricCollection {

    private final Map<String, Metric> collection = new ConcurrentHashMap<>();
    private final MetricNameValidator validator = new MetricNameValidator();

    private SerializationStrategy serializationStrategy = new PrometheusSerializationStrategy();

    private MetricCollection() {
    }

    public static MetricCollection create() {
        return new MetricCollection();
    }

    /**
     * Size of collection.
     */
    public int size() {
        return collection.size();
    }

    public CounterBuilder createCounter() {
        return new CounterBuilder();
    }

    public GaugeBuilder createGauge() {
        return new GaugeBuilder();
    }

    public HistogramBuilder createHistogram() {
        return new HistogramBuilder();
    }

    /**
     * Get {@link Counter} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Counter} if metric name is valid.
     */
    public Counter getOrCreateCounter(String name) {
        return (Counter) collection.computeIfAbsent(name, key -> {
            validator.validate(key);
            return new Counter(key);
        });
    }

    /**
     * Get {@link Gauge} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Gauge} if metric name is valid.
     */
    public Gauge getOrCreateGauge(String name, Supplier<Double> supplier) {
        return (Gauge) collection.computeIfAbsent(name, key -> {
            validator.validate(key);
            return new Gauge(key, supplier);
        });
    }

    /**
     * Get {@link Histogram} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Histogram} if metric name is valid.
     */
    public Histogram getOrCreateHistogram(String name) {
        return (Histogram) collection.computeIfAbsent(name, key -> {
            validator.validate(name);
            return new Histogram(key);
        });
    }

    /**
     * Write metricts
     * @param writer
     */
    public void write(Writer writer) {
        Collection<Metric> metrics = collection.values();
        metrics.forEach(metric -> serializationStrategy.serialize(metric, writer));
    }

    /**
     * Set strategy which applies when serialize a metric.
     * @param strategy  Implementation of serialization strategy
     */
    public void setSerializationStrategy(SerializationStrategy strategy) {
        this.serializationStrategy = strategy;
    }

    public interface MetricBuilder<T> {

        LabelBuilder<T> name(String name);

        /**
         * Register a metric in collection.
         */
        T register();
    }

    private abstract static class AbstractMetricBuilder<T> implements MetricBuilder<T> {
        private final NameBuilder<T> nameBuilder = new DefaultBuilder<>(this);

        @Override
        public LabelBuilder<T> name(String name) {
            return nameBuilder.name(name);
        }

        protected String getMetricName() {
            return nameBuilder.build();
        }
    }

    public class CounterBuilder extends AbstractMetricBuilder<Counter> {

        @Override
        public Counter register() {
            return (Counter) collection.computeIfAbsent(getMetricName(), Counter::new);
        }

    }

    public class GaugeBuilder extends AbstractMetricBuilder<Gauge> {

        private Supplier<Double> supplier;

        public GaugeBuilder withSupplier(Supplier<Double> supplier) {
            this.supplier = supplier;
            return this;
        }

        @Override
        public Gauge register() {
            return (Gauge) collection.computeIfAbsent(getMetricName(), name -> new Gauge(name, supplier));
        }
    }

    public class HistogramBuilder extends AbstractMetricBuilder<Histogram> {
        @Override
        public Histogram register() {
            return (Histogram) collection.computeIfAbsent(getMetricName(), Histogram::new);
        }
    }

    public static class DefaultBuilder<T> implements NameBuilder<T> {
        private String name;
        private final Map<String, String> labels = new LinkedHashMap<>();

        private final MetricBuilder<T> metricBuilder;

        private DefaultBuilder(MetricBuilder<T> metricBuilder) {
            this.metricBuilder = metricBuilder;
        }

        @Override
        public LabelBuilder<T> name(String name) {
            this.name = name;
            return new DefaultLabelBuilder(metricBuilder);
        }

        @Override
        public String build() {
            final String labels = buildLabels();
            return name + labels;
        }

        private String buildLabels() {
            if (labels.isEmpty()) {
                return "";
            }

            int size = labels.size();

            StringBuilder sb = new StringBuilder("{");

            final int[] i = {0};
            labels.forEach((name, value) -> {
                sb.append(name)
                        .append("=")
                        .append(("\""))
                        .append(value)
                        .append(("\""));

                if (i[0] < size - 1) {
                    sb.append(", ");
                }
                i[0]++;
            });

            sb.append("}");
            return sb.toString();
        }

        public class DefaultLabelBuilder implements LabelBuilder<T> {

            private final MetricBuilder<T> metricBuilder;

            public DefaultLabelBuilder(MetricBuilder<T> metricBuilder) {
                this.metricBuilder = metricBuilder;
            }

            @Override
            public LabelBuilder<T> addLabel(String name, String value) {
                labels.put(name, value);
                return this;
            }

            @Override
            public MetricBuilder<T> then() {
                return metricBuilder;
            }
        }
    }

    public interface NameBuilder<T> {

        LabelBuilder<T> name(String name);

        String build();

    }

    public interface LabelBuilder<T> {
        LabelBuilder<T> addLabel(String name, String value);

        MetricBuilder<T> then();
    }

}
