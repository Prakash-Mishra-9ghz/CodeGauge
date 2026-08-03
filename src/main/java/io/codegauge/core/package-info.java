/**
 * Domain model of CodeGauge.
 *
 * <p>Types in this package describe <em>what a repository is</em> —
 * {@code Repository}, {@code SourceFile}, {@code AnalysisResult}, and similar
 * value types — independent of how a repository was discovered, how it is
 * analyzed, or how results are reported.
 *
 * <p><strong>Dependency rule:</strong> this package must not depend on
 * {@code io.codegauge.cli}, {@code io.codegauge.scanner},
 * {@code io.codegauge.analyzer}, or {@code io.codegauge.report}. It may only
 * depend on the JDK and, where unavoidable, {@code io.codegauge.util}.
 * Domain types are expected to be immutable (records, where possible) so
 * they can be freely shared across analyzers and threads without defensive
 * copying.
 *
 * <p>Violating this rule (e.g. a domain type that knows how to serialize
 * itself to JSON, or reads from the file system) is a sign the type belongs
 * in {@code report} or {@code scanner} instead.
 */
package io.codegauge.core;
