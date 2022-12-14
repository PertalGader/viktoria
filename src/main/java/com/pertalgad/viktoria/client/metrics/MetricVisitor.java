/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package com.pertalgad.viktoria.client.metrics;

/**
 * @author Valery Kantor
 */
public interface MetricVisitor {

    void visit(Counter counter);

    void visit(Gauge gauge);

    void visit(Histogram histogram);
}
