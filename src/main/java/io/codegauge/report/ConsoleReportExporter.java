package io.codegauge.report;

import io.codegauge.core.DependencyResult;
import io.codegauge.core.DocumentationResult;
import io.codegauge.core.HealthScoreResult;
import io.codegauge.core.MetricsResult;
import io.codegauge.core.Repository;

/**
 * {@link ReportExporter} producing the same human-readable text format
 * {@code analyze} has printed directly to the console since v0.3–v0.7,
 * now centralized here so console output goes through the same
 * {@link ReportExporter} abstraction as {@link JsonReportExporter}.
 */
public final class ConsoleReportExporter implements ReportExporter {

    @Override
    public String export(RepositoryReport report) {
        StringBuilder out = new StringBuilder();
        appendSummary(out, report.repository());
        appendMetrics(out, report.metrics());
        appendDependencies(out, report.dependencies());
        appendDocumentation(out, report.documentation());
        appendHealthScore(out, report.health());
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Repository repository) {
        out.append("Repository Summary\n");
        out.append("------------------------------\n");
        out.append(String.format("Project        %s%n", repository.name()));
        out.append(String.format("Files          %d%n", repository.fileCount()));
        out.append(String.format("Directories    %d%n", repository.directoryCount()));
        out.append('\n');
    }

    private void appendMetrics(StringBuilder out, MetricsResult metrics) {
        out.append("Source Code Metrics (Java)\n");
        out.append("------------------------------\n");
        out.append(String.format("Java Files         %d%n", metrics.javaFileCount()));
        out.append(String.format("Lines of Code      %d%n", metrics.totalLinesOfCode()));
        out.append(String.format("Classes            %d%n", metrics.classCount()));
        out.append(String.format("Interfaces         %d%n", metrics.interfaceCount()));
        out.append(String.format("Enums              %d%n", metrics.enumCount()));
        out.append(String.format("Records            %d%n", metrics.recordCount()));
        out.append(String.format("Methods            %d%n", metrics.methodCount()));
        out.append(String.format("Fields             %d%n", metrics.fieldCount()));
        out.append(String.format("Constructors       %d%n", metrics.constructorCount()));
        out.append(String.format("Avg Method Length  %.1f lines%n", metrics.averageMethodLength()));
        out.append(String.format("Avg Class Size     %.1f lines%n", metrics.averageClassSize()));
        if (metrics.largestClassLines() > 0) {
            out.append(String.format("Largest Class      %s (%d lines)%n",
                    metrics.largestClassName(), metrics.largestClassLines()));
        }
        if (metrics.largestFilePath() != null) {
            out.append(String.format("Largest File       %s (%d lines)%n",
                    metrics.largestFilePath(), metrics.largestFileLines()));
        }
        out.append('\n');
    }

    private void appendDependencies(StringBuilder out, DependencyResult result) {
        if (!result.pomFound()) {
            out.append("No pom.xml found at repository root.\n\n");
            return;
        }

        out.append("Dependencies\n");
        out.append("------------------------------\n");
        if (result.dependencies().isEmpty()) {
            out.append("(none declared)\n");
        } else {
            result.dependencies().forEach(d -> out.append(String.format("%s:%s%s (%s)%n",
                    d.groupId(), d.artifactId(),
                    d.version().isEmpty() ? "" : ":" + d.version(),
                    d.scope())));
        }

        out.append('\n').append("Plugins\n").append("------------------------------\n");
        if (result.plugins().isEmpty()) {
            out.append("(none declared)\n");
        } else {
            result.plugins().forEach(p -> out.append(String.format("%s:%s%s%n",
                    p.groupId(), p.artifactId(),
                    p.version().isEmpty() ? "" : ":" + p.version())));
        }

        if (!result.duplicateCoordinates().isEmpty()) {
            out.append('\n').append("Duplicate Dependencies\n").append("------------------------------\n");
            result.duplicateCoordinates().forEach(c -> out.append(c).append('\n'));
        }
        out.append('\n');
    }

    private void appendDocumentation(StringBuilder out, DocumentationResult result) {
        out.append("Documentation\n");
        out.append("------------------------------\n");
        out.append(String.format("README               %s%n", present(result.readmePresent())));
        out.append(String.format("LICENSE              %s%n", present(result.licensePresent())));
        out.append(String.format("Installation Guide   %s%n", present(result.installationSectionPresent())));
        out.append(String.format("Usage Section        %s%n", present(result.usageSectionPresent())));
        out.append(String.format("Examples             %s%n", present(result.examplesSectionPresent())));
        out.append(String.format("Contributing Guide   %s%n", present(result.contributingPresent())));
        out.append(String.format("Code of Conduct      %s%n", present(result.codeOfConductPresent())));
        out.append(String.format("Screenshots          %s%n", present(result.screenshotsPresent())));
        out.append('\n');
        out.append(String.format("Documentation Score  %.1f / 10%n", result.score()));
        out.append('\n');
    }

    private void appendHealthScore(StringBuilder out, HealthScoreResult health) {
        out.append("Repository Health Score\n");
        out.append("------------------------------\n");
        out.append(String.format("Documentation      %.1f / 10%n", health.documentationScore()));
        out.append(String.format("Dependencies       %s%n", formatComponent(health.dependencyScore())));
        out.append(String.format("Maintainability    %s%n", formatComponent(health.maintainabilityScore())));
        out.append('\n');
        out.append(String.format("Overall            %.1f / 10%n", health.overallScore()));
    }

    private static String present(boolean value) {
        return value ? "Present" : "Missing";
    }

    private static String formatComponent(Double score) {
        return score == null ? "N/A" : String.format("%.1f / 10", score);
    }
}