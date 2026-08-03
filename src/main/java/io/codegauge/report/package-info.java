/**
 * Report models and exporters.
 *
 * <p>Turns one or more {@code AnalysisResult}s into an output format
 * (console text, JSON, HTML, CSV). Exporters depend on the report model,
 * not on {@code io.codegauge.analyzer} internals, so a new export format
 * can be added without touching analysis code, and a new analyzer can be
 * added without touching export code.
 */
package io.codegauge.report;
