package io.codegauge.analyzer;

import io.codegauge.core.AnalysisResult;
import io.codegauge.core.Repository;

import java.util.List;

/**
 * Runs a fixed set of {@link Analyzer}s against a repository and collects
 * their results.
 *
 * <p>Introduced in v0.7 to replace the ad hoc scanner+analyzer wiring that
 * had accumulated directly inside CLI command classes since v0.5. CLI
 * commands now depend only on this orchestrator and on
 * {@link #findResult(List, Class)} to retrieve a specific typed result,
 * rather than constructing and calling individual analyzers themselves.
 */
public final class AnalysisManager {

    private final List<Analyzer> analyzers;

    public AnalysisManager(List<Analyzer> analyzers) {
        this.analyzers = List.copyOf(analyzers);
    }

    /**
     * @param repository repository to analyze
     * @return one {@link AnalysisResult} per configured analyzer, in
     * configuration order
     */
    public List<AnalysisResult> runAll(Repository repository) {
        return analyzers.stream()
                .map(analyzer -> analyzer.analyze(repository))
                .toList();
    }

    /**
     * Retrieves the single result of a specific type from a result list,
     * so callers don't need an {@code instanceof} chain.
     *
     * @throws IllegalStateException if no result of the given type is present
     */
    public static <T extends AnalysisResult> T findResult(List<AnalysisResult> results, Class<T> type) {
        return results.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No result of type " + type.getSimpleName()));
    }
}