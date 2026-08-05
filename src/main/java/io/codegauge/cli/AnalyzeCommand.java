package io.codegauge.cli;

import io.codegauge.analyzer.AnalysisManager;
import io.codegauge.analyzer.DependencyAnalyzer;
import io.codegauge.analyzer.DocumentationAnalyzer;
import io.codegauge.analyzer.HealthScoreCalculator;
import io.codegauge.analyzer.MetricsAnalyzer;
import io.codegauge.core.AnalysisResult;
import io.codegauge.core.DependencyResult;
import io.codegauge.core.DocumentationResult;
import io.codegauge.core.HealthScoreResult;
import io.codegauge.core.MetricsResult;
import io.codegauge.core.Repository;
import io.codegauge.parser.JavaParserFileParser;
import io.codegauge.parser.MarkdownReadmeParser;
import io.codegauge.parser.MavenPomParser;
import io.codegauge.scanner.FileSystemRepositoryScanner;
import io.codegauge.scanner.RepositoryScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * {@code codegauge analyze <path>} — full repository analysis: scan,
 * metrics, dependencies, documentation, and an aggregated health score.
 *
 * <p>Since v0.7, analyzer orchestration goes through
 * {@link AnalysisManager} rather than this class constructing and calling
 * each analyzer directly — see {@code AnalysisManager}'s javadoc for why.
 */
@Command(name = "analyze", description = "Run full repository analysis.")
final class AnalyzeCommand implements Callable<Integer> {

    @ParentCommand
    private RootCommand parent;

    @Parameters(index = "0", description = "Path to the repository to analyze.")
    private String path;

    private final RepositoryScanner scanner = new FileSystemRepositoryScanner();

    private final AnalysisManager analysisManager = new AnalysisManager(List.of(
            new MetricsAnalyzer(new JavaParserFileParser()),
            new DependencyAnalyzer(new MavenPomParser()),
            new DocumentationAnalyzer(new MarkdownReadmeParser())
    ));

    private final HealthScoreCalculator healthScoreCalculator = new HealthScoreCalculator();

    @Override
    public Integer call() {
        Repository repository;
        try {
            repository = scanner.scan(Path.of(path));
        } catch (IOException e) {
            System.err.println("Failed to scan repository: " + e.getMessage());
            return 1;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        List<AnalysisResult> results = analysisManager.runAll(repository);
        MetricsResult metrics = AnalysisManager.findResult(results, MetricsResult.class);
        DependencyResult dependencies = AnalysisManager.findResult(results, DependencyResult.class);
        DocumentationResult documentation = AnalysisManager.findResult(results, DocumentationResult.class);
        HealthScoreResult health = healthScoreCalculator.calculate(metrics, dependencies, documentation);

        System.out.println("Repository Summary");
        System.out.println("------------------------------");
        System.out.printf("Project        %s%n", repository.name());
        System.out.printf("Files          %d%n", repository.fileCount());
        System.out.printf("Directories    %d%n", repository.directoryCount());
        System.out.println();

        printMetrics(metrics);
        DependenciesCommand.printDependencyReport(dependencies);
        DocsCommand.printDocumentationReport(documentation);
        printHealthScore(health);

        System.out.printf("(format=%s — export arrives in later milestones)%n", parent.outputFormat());
        return 0;
    }

    private static void printMetrics(MetricsResult metrics) {
        System.out.println("Source Code Metrics (Java)");
        System.out.println("------------------------------");
        System.out.printf("Java Files         %d%n", metrics.javaFileCount());
        System.out.printf("Lines of Code      %d%n", metrics.totalLinesOfCode());
        System.out.printf("Classes            %d%n", metrics.classCount());
        System.out.printf("Interfaces         %d%n", metrics.interfaceCount());
        System.out.printf("Enums              %d%n", metrics.enumCount());
        System.out.printf("Records            %d%n", metrics.recordCount());
        System.out.printf("Methods            %d%n", metrics.methodCount());
        System.out.printf("Fields             %d%n", metrics.fieldCount());
        System.out.printf("Constructors       %d%n", metrics.constructorCount());
        System.out.printf("Avg Method Length  %.1f lines%n", metrics.averageMethodLength());
        System.out.printf("Avg Class Size     %.1f lines%n", metrics.averageClassSize());
        if (metrics.largestClassLines() > 0) {
            System.out.printf("Largest Class      %s (%d lines)%n",
                    metrics.largestClassName(), metrics.largestClassLines());
        }
        if (metrics.largestFilePath() != null) {
            System.out.printf("Largest File       %s (%d lines)%n",
                    metrics.largestFilePath(), metrics.largestFileLines());
        }
        System.out.println();
    }

    private static void printHealthScore(HealthScoreResult health) {
        System.out.println("Repository Health Score");
        System.out.println("------------------------------");
        System.out.printf("Documentation      %.1f / 10%n", health.documentationScore());
        System.out.printf("Dependencies       %s%n", formatComponent(health.dependencyScore()));
        System.out.printf("Maintainability    %s%n", formatComponent(health.maintainabilityScore()));
        System.out.println();
        System.out.printf("Overall            %.1f / 10%n", health.overallScore());
        System.out.println();
    }

    private static String formatComponent(Double score) {
        return score == null ? "N/A" : String.format("%.1f / 10", score);
    }
}