package com.pertalgad.viktoria.client.metrics;

/**
 * Prometheus-compatible metric.
 *
 */
public interface Metric {

    String getName();

    void accept(MetricVisitor visitor);

}
