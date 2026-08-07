package io.codegauge.report;

/** Thrown when a {@link ReportExporter} cannot produce its output. */
public final class ReportExportException extends RuntimeException {
    public ReportExportException(String message, Throwable cause) {
        super(message, cause);
    }
}