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
import io.codegauge.report.ConsoleReportExporter;
import io.codegauge.report.JsonReportExporter;
import io.codegauge.report.ReportExporter;
import io.codegauge.report.RepositoryReport;
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
 * metrics, dependencies, documentation, health score, and export.
 *
 * <p>Since v0.8, output goes through a {@link ReportExporter} rather than
 * this class printing directly — see {@code report}'s package-info for why.
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

        RepositoryReport report = new RepositoryReport(repository, metrics, dependencies, documentation, health);

        System.out.println(selectExporter().export(report));
        return 0;
    }

    private ReportExporter selectExporter() {
        return switch (parent.outputFormat()) {
            case JSON -> new JsonReportExporter();
            case CONSOLE -> new ConsoleReportExporter();
            case HTML, CSV -> {
                System.err.printf("(%s export not yet implemented — falling back to console)%n",
                        parent.outputFormat());
                yield new ConsoleReportExporter();
            }
        };
    }
}