package io.codegauge.core;

/**
 * Marker interface implemented by every analyzer's result type.
 *
 * <p>Deliberately minimal — each analyzer defines its own result shape
 * (e.g. {@link MetricsResult}) rather than forcing a one-size-fits-all
 * structure. {@code analyzerName()} exists so a report exporter can label
 * results generically without an {@code instanceof} chain.
 */
public interface AnalysisResult {
    String analyzerName();
}