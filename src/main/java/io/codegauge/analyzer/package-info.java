/**
 * Pluggable repository analyzers.
 *
 * <p>Each analyzer implements a single concern (structure, dependencies,
 * documentation, code smells, ...) against a {@code io.codegauge.core}
 * domain model and produces an {@code AnalysisResult}. Analyzers are
 * combined by an orchestrator that treats them uniformly — this is the
 * Strategy pattern, and it is what will let Phase 5's plugin system add new
 * analyzers without modifying existing ones (open/closed principle).
 *
 * <p>Analyzers must not perform file system I/O directly; they operate on
 * the model produced by {@code io.codegauge.scanner}. This keeps them fast
 * to unit test.
 */
package io.codegauge.analyzer;
