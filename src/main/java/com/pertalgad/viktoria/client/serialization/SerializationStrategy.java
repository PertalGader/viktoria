/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package com.pertalgad.viktoria.client.serialization;

import com.pertalgad.viktoria.client.metrics.Metric;

import java.io.Writer;

/**
 * @author Valery Kantor
 */
public interface SerializationStrategy {

    void serialize(Metric metric, Writer writer);

}
