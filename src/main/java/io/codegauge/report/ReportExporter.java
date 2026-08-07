package io.codegauge.report;

/**
 * Turns a {@link RepositoryReport} into a specific output format.
 */
public interface ReportExporter {
    String export(RepositoryReport report);
}