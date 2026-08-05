package io.codegauge.analyzer;

import io.codegauge.core.DependencyResult;
import io.codegauge.core.DocumentationResult;
import io.codegauge.core.HealthScoreResult;
import io.codegauge.core.MetricsResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Combines the results of individual analyzers into an overall repository
 * health score.
 *
 * <p>Not an {@link Analyzer} itself — it consumes other analyzers' results
 * rather than scanning the repository directly, so it doesn't fit the
 * {@code Analyzer.analyze(Repository)} contract. {@link AnalysisManager}
 * runs the underlying analyzers; this class only aggregates.
 *
 * <p><strong>v0.7 scope:</strong> only components we can honestly measure
 * are included — documentation, dependency hygiene, and a code
 * maintainability heuristic. The README's original example output also
 * lists "Architecture", "Project Structure", and "Testing" components;
 * those require analyzers (code smell detection, test-file detection) that
 * don't exist yet, and inventing a score for a dimension we haven't
 * measured would be worse than not reporting it. A component whose
 * analyzer couldn't produce a meaningful score (e.g. no {@code pom.xml} was
 * found) is {@code null} in the result and excluded from the overall
 * average rather than defaulted to some invented neutral number.
 */
public final class HealthScoreCalculator {

    // Maintainability thresholds, chosen to align with the classic
    // "Large Class" / "Long Method" code-smell thresholds this project's
    // own README lists as a future detector — a repository under these
    // isn't penalized at all.
    private static final double IDEAL_MAX_METHOD_LENGTH = 30.0;
    private static final double IDEAL_MAX_CLASS_SIZE = 300.0;
    private static final int IDEAL_MAX_FILE_LINES = 1000;
    private static final double LARGE_FILE_PENALTY = 2.0;

    private static final double DUPLICATE_DEPENDENCY_PENALTY = 2.0;

    /**
     * @return the aggregated health score; see class javadoc for how
     * unavailable components are handled
     */
    public HealthScoreResult calculate(
            MetricsResult metrics, DependencyResult dependencies, DocumentationResult documentation) {

        double documentationScore = documentation.score();
        Double dependencyScore = scoreDependencies(dependencies);
        Double maintainabilityScore = scoreMaintainability(metrics);

        List<Double> availableComponents = new ArrayList<>();
        availableComponents.add(documentationScore);
        if (dependencyScore != null) {
            availableComponents.add(dependencyScore);
        }
        if (maintainabilityScore != null) {
            availableComponents.add(maintainabilityScore);
        }

        double overall = availableComponents.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return new HealthScoreResult(documentationScore, dependencyScore, maintainabilityScore, overall);
    }

    /** @return {@code null} if no pom.xml was found (nothing to assess) */
    private Double scoreDependencies(DependencyResult dependencies) {
        if (!dependencies.pomFound()) {
            return null;
        }
        double score = 10.0 - (dependencies.duplicateCoordinates().size() * DUPLICATE_DEPENDENCY_PENALTY);
        return clamp(score);
    }

    /** @return {@code null} if no Java files were found (nothing to assess) */
    private Double scoreMaintainability(MetricsResult metrics) {
        if (metrics.javaFileCount() == 0) {
            return null;
        }
        double score = 10.0;
        if (metrics.averageMethodLength() > IDEAL_MAX_METHOD_LENGTH) {
            score -= Math.min(4.0, (metrics.averageMethodLength() - IDEAL_MAX_METHOD_LENGTH) / 10.0);
        }
        if (metrics.averageClassSize() > IDEAL_MAX_CLASS_SIZE) {
            score -= Math.min(4.0, (metrics.averageClassSize() - IDEAL_MAX_CLASS_SIZE) / 100.0);
        }
        if (metrics.largestFileLines() > IDEAL_MAX_FILE_LINES) {
            score -= LARGE_FILE_PENALTY;
        }
        return clamp(score);
    }

    private static double clamp(double score) {
        return Math.max(0.0, Math.min(10.0, score));
    }
}