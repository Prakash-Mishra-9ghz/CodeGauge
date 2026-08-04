package io.codegauge.analyzer;

import io.codegauge.core.AnalysisResult;
import io.codegauge.core.Repository;

/**
 * Strategy interface for a single analysis concern.
 *
 * <p>Every analyzer (metrics, dependencies, documentation, code smells — as
 * they're introduced in later milestones) implements this. An orchestrator
 * can hold a {@code List<Analyzer>} and run them uniformly, which is what
 * will let the future plugin system (Phase 5) add analyzers without
 * modifying existing ones.
 */
public interface Analyzer {
    AnalysisResult analyze(Repository repository);
}