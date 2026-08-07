package io.codegauge.report;

import io.codegauge.core.DependencyResult;
import io.codegauge.core.DocumentationResult;
import io.codegauge.core.HealthScoreResult;
import io.codegauge.core.MetricsResult;
import io.codegauge.core.Repository;

import java.util.Objects;

/**
 * Aggregates everything produced by a full {@code analyze} run into a
 * single object, for exporters to turn into console text, JSON, HTML, etc.
 */
public record RepositoryReport(
        Repository repository,
        MetricsResult metrics,
        DependencyResult dependencies,
        DocumentationResult documentation,
        HealthScoreResult health
) {
    public RepositoryReport {
        Objects.requireNonNull(repository, "repository must not be null");
        Objects.requireNonNull(metrics, "metrics must not be null");
        Objects.requireNonNull(dependencies, "dependencies must not be null");
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(health, "health must not be null");
    }
}