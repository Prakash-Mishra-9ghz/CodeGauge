package io.codegauge.report;

import io.codegauge.core.Dependency;
import io.codegauge.core.DependencyResult;
import io.codegauge.core.DocumentationResult;
import io.codegauge.core.HealthScoreResult;
import io.codegauge.core.MetricsResult;
import io.codegauge.core.Plugin;
import io.codegauge.core.Repository;

/**
 * {@link ReportExporter} producing a single, self-contained static HTML
 * page — inline CSS, no external assets or JavaScript, so the output can be
 * opened directly or shared without a build step.
 *
 * <p>Scope for v0.9: a readable, styled report mirroring
 * {@link ConsoleReportExporter}'s sections, with scores rendered as
 * color-coded badges. This is deliberately not the interactive dashboard
 * (charts, dependency-tree graphs) the README lists separately under
 * "Future Enhancements" — that's a materially bigger feature and doesn't
 * belong in this milestone.
 *
 * <p>No templating library is used or needed: Java text blocks plus a
 * {@link StringBuilder} are sufficient for a document this size, keeping
 * this class free of any new dependency.
 */
public final class HtmlReportExporter implements ReportExporter {

    private static final String CSS = """
            body { font-family: -apple-system, Segoe UI, Roboto, sans-serif; max-width: 800px; margin: 2rem auto; color: #1a1a1a; }
            h1 { border-bottom: 3px solid #2b6cb0; padding-bottom: 0.5rem; }
            section { margin-bottom: 2rem; }
            table { width: 100%; border-collapse: collapse; }
            th, td { text-align: left; padding: 0.4rem 0.6rem; border-bottom: 1px solid #e2e8f0; }
            th { width: 40%; color: #4a5568; font-weight: 600; }
            ul { padding-left: 1.2rem; }
            .badge { display: inline-block; padding: 0.15rem 0.5rem; border-radius: 0.4rem; font-size: 0.85rem; font-weight: 600; }
            .badge.good { background: #c6f6d5; color: #22543d; }
            .badge.warn { background: #fefcbf; color: #744210; }
            .badge.bad { background: #fed7d7; color: #822727; }
            .muted { color: #a0aec0; }
            .warn-heading { color: #c05621; }
            li.warn { color: #c05621; }
            .overall { font-size: 1.2rem; margin-top: 0.5rem; }
            """;

    @Override
    public String export(RepositoryReport report) {
        StringBuilder body = new StringBuilder();
        appendSummary(body, report.repository());
        appendMetrics(body, report.metrics());
        appendDependencies(body, report.dependencies());
        appendDocumentation(body, report.documentation());
        appendHealthScore(body, report.health());

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="UTF-8">
                <title>CodeGauge Report — %s</title>
                <style>%s</style>
                </head>
                <body>
                <h1>CodeGauge Report</h1>
                %s
                </body>
                </html>
                """.formatted(escape(report.repository().name()), CSS, body);
    }

    private void appendSummary(StringBuilder out, Repository repository) {
        out.append("<section><h2>Repository Summary</h2><table>");
        row(out, "Project", escape(repository.name()));
        row(out, "Files", String.valueOf(repository.fileCount()));
        row(out, "Directories", String.valueOf(repository.directoryCount()));
        out.append("</table></section>");
    }

    private void appendMetrics(StringBuilder out, MetricsResult metrics) {
        out.append("<section><h2>Source Code Metrics (Java)</h2><table>");
        row(out, "Java Files", String.valueOf(metrics.javaFileCount()));
        row(out, "Lines of Code", String.valueOf(metrics.totalLinesOfCode()));
        row(out, "Classes", String.valueOf(metrics.classCount()));
        row(out, "Interfaces", String.valueOf(metrics.interfaceCount()));
        row(out, "Enums", String.valueOf(metrics.enumCount()));
        row(out, "Records", String.valueOf(metrics.recordCount()));
        row(out, "Methods", String.valueOf(metrics.methodCount()));
        row(out, "Fields", String.valueOf(metrics.fieldCount()));
        row(out, "Constructors", String.valueOf(metrics.constructorCount()));
        row(out, "Avg Method Length", "%.1f lines".formatted(metrics.averageMethodLength()));
        row(out, "Avg Class Size", "%.1f lines".formatted(metrics.averageClassSize()));
        if (metrics.largestClassLines() > 0) {
            row(out, "Largest Class", "%s (%d lines)"
                    .formatted(escape(metrics.largestClassName()), metrics.largestClassLines()));
        }
        if (metrics.largestFilePath() != null) {
            row(out, "Largest File", "%s (%d lines)"
                    .formatted(escape(metrics.largestFilePath().toString()), metrics.largestFileLines()));
        }
        out.append("</table></section>");
    }

    private void appendDependencies(StringBuilder out, DependencyResult result) {
        out.append("<section><h2>Dependencies</h2>");
        if (!result.pomFound()) {
            out.append("<p class=\"muted\">No pom.xml found at repository root.</p></section>");
            return;
        }

        out.append("<ul>");
        if (result.dependencies().isEmpty()) {
            out.append("<li class=\"muted\">(none declared)</li>");
        } else {
            for (Dependency d : result.dependencies()) {
                out.append("<li>").append(escape(d.coordinate()))
                        .append(d.version().isEmpty() ? "" : ":" + escape(d.version()))
                        .append(" <span class=\"badge\">").append(escape(d.scope())).append("</span></li>");
            }
        }
        out.append("</ul><h3>Plugins</h3><ul>");
        if (result.plugins().isEmpty()) {
            out.append("<li class=\"muted\">(none declared)</li>");
        } else {
            for (Plugin p : result.plugins()) {
                out.append("<li>").append(escape(p.groupId())).append(":").append(escape(p.artifactId()))
                        .append(p.version().isEmpty() ? "" : ":" + escape(p.version())).append("</li>");
            }
        }
        out.append("</ul>");

        if (!result.duplicateCoordinates().isEmpty()) {
            out.append("<h3 class=\"warn-heading\">Duplicate Dependencies</h3><ul>");
            for (String coordinate : result.duplicateCoordinates()) {
                out.append("<li class=\"warn\">").append(escape(coordinate)).append("</li>");
            }
            out.append("</ul>");
        }
        out.append("</section>");
    }

    private void appendDocumentation(StringBuilder out, DocumentationResult result) {
        out.append("<section><h2>Documentation</h2><table>");
        row(out, "README", presence(result.readmePresent()));
        row(out, "LICENSE", presence(result.licensePresent()));
        row(out, "Installation Guide", presence(result.installationSectionPresent()));
        row(out, "Usage Section", presence(result.usageSectionPresent()));
        row(out, "Examples", presence(result.examplesSectionPresent()));
        row(out, "Contributing Guide", presence(result.contributingPresent()));
        row(out, "Code of Conduct", presence(result.codeOfConductPresent()));
        row(out, "Screenshots", presence(result.screenshotsPresent()));
        out.append("</table><p>").append(scoreBadge("Documentation Score", result.score())).append("</p></section>");
    }

    private void appendHealthScore(StringBuilder out, HealthScoreResult health) {
        out.append("<section><h2>Repository Health Score</h2><table>");
        row(out, "Documentation", scoreBadge(null, health.documentationScore()));
        row(out, "Dependencies", health.dependencyScore() == null
                ? "<span class=\"muted\">N/A</span>" : scoreBadge(null, health.dependencyScore()));
        row(out, "Maintainability", health.maintainabilityScore() == null
                ? "<span class=\"muted\">N/A</span>" : scoreBadge(null, health.maintainabilityScore()));
        out.append("</table><div class=\"overall\">Overall: ")
                .append(scoreBadge(null, health.overallScore()))
                .append("</div></section>");
    }

    private static void row(StringBuilder out, String label, String value) {
        out.append("<tr><th>").append(escape(label)).append("</th><td>").append(value).append("</td></tr>");
    }

    private static String presence(boolean value) {
        return value ? "<span class=\"badge good\">Present</span>" : "<span class=\"badge bad\">Missing</span>";
    }

    private static String scoreBadge(String label, double score) {
        String cssClass = score >= 8.0 ? "good" : score >= 5.0 ? "warn" : "bad";
        String prefix = label == null ? "" : escape(label) + " ";
        return "%s<span class=\"badge %s\">%.1f / 10</span>".formatted(prefix, cssClass, score);
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}