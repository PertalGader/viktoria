/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package com.pertalgad.viktoria.client.serialization;


import com.pertalgad.viktoria.client.metrics.*;
import com.pertalgad.viktoria.client.utils.Pair;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Valery Kantor
 */
public class PrometheusSerializationStrategy implements SerializationStrategy {

    public void serialize(Metric metric, Writer writer) {
        MetricVisitor visitor = new MetricVisitor() {
            @Override
            public void visit(Counter counter) {
                String prefix = counter.getName();
                long value = counter.get();

                try {
                    writer.write(prefix);
                    writer.write(" ");
                    writer.write(String.valueOf(value));
                    writer.write("\n");
                } catch (IOException e) {
                    throw new MetricSerializationException("Unable to serialize Counter metric" + prefix, e);
                }
            }

            @Override
            public void visit(Gauge gauge) {
                String prefix = gauge.getName();
                double value = gauge.get();

                try {
                    writer.write(prefix);
                    writer.write(" ");
                    writer.write(String.valueOf(value));
                    writer.write("\n");
                } catch (IOException e) {
                    throw new MetricSerializationException("Unable to serialize Gauge metric: " + prefix, e);
                }
            }

            @Override
            public void visit(Histogram histogram) {
                writeHistogram(writer, histogram);
            }
        };

        metric.accept(visitor);
    }

    private void writeHistogram(Writer writer, Histogram histogram) {
        String prefix = histogram.getName();
        LongAdder countTotal = new LongAdder();

        histogram.visit((vmrange, count) -> {
            Pair<String, String> metricPair = splitMetricName(prefix);
            String name = metricPair.getKey();
            String labels = metricPair.getValue();

            final String tag = "vmrange=\"" + vmrange + "\"";
            labels = labels.isEmpty() ? tag : labels + "," + tag;

            try {
                writer.write(name);
                writer.write("_bucket");
                writer.write("{");
                writer.write(labels);
                writer.write("}");
                writer.write(Double.toString(count));
                writer.write("\n");
            }  catch (IOException e) {
                throw new MetricSerializationException("Unable to serialize Histogram metric: " + prefix, e);
            }

            countTotal.increment();
        });

        Pair<String, String> metricPair = splitMetricName(prefix);
        String name = metricPair.getKey();
        String labels = metricPair.getValue();
        double sum = histogram.getSum();

        try {
            writeSum(writer, name, labels, sum);
            writeCount(writer, name, labels, countTotal.sum());
        } catch (IOException e) {
            throw new MetricSerializationException("Unable to serialize Histogram sum", e);
        }
    }

    private void writeSum(Writer writer, String name, String labels, double sum) throws IOException {
        writer.write(name);
        writer.write("_sum");

        if (!labels.isEmpty()) {
            writer.write("{");
            writer.write(labels);
            writer.write("}");
        }

        writer.write(" ");
        writer.write(Double.toString(sum));
        writer.write("\n");
    }

    private void writeCount(Writer writer, String name, String labels, double sum) throws IOException {
        writer.write(name);
        writer.write("_count");

        if (!labels.isEmpty()) {
            writer.write("{");
            writer.write(labels);
            writer.write("}");
        }

        writer.write(" ");
        writer.write(Double.toString(sum));
        writer.write("\n");
    }

    private Pair<String, String> splitMetricName(String name) {
        int index = name.indexOf('{');
        if (index < 0) {
            return Pair.of(name, "");
        }

        int tailingIndex = name.indexOf("}");
        String metricName = name.substring(0, index);
        String labels = name.substring(index + 1, tailingIndex);
        return Pair.of(metricName, labels);
    }
}
