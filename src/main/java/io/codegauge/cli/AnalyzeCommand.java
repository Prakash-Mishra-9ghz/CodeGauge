package io.codegauge.cli;

import io.codegauge.analyzer.MetricsAnalyzer;
import io.codegauge.core.MetricsResult;
import io.codegauge.core.Repository;
import io.codegauge.parser.JavaParserFileParser;
import io.codegauge.scanner.FileSystemRepositoryScanner;
import io.codegauge.scanner.RepositoryScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * {@code codegauge analyze <path>} — repository scan plus source code metrics.
 *
 * <p>Dependency/documentation analysis, code smells, and the health score
 * are not yet computed — those arrive in v0.5 onward via
 * {@code io.codegauge.analyzer.AnalysisManager}, which will orchestrate
 * multiple {@code Analyzer}s once more than one exists.
 */
@Command(name = "analyze", description = "Run full repository analysis.")
final class AnalyzeCommand implements Callable<Integer> {

    @ParentCommand
    private RootCommand parent;

    @Parameters(index = "0", description = "Path to the repository to analyze.")
    private String path;

    private final RepositoryScanner scanner = new FileSystemRepositoryScanner();
    private final MetricsAnalyzer metricsAnalyzer = new MetricsAnalyzer(new JavaParserFileParser());

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

        MetricsResult metrics = metricsAnalyzer.analyze(repository);

        System.out.println("Repository Summary");
        System.out.println("------------------------------");
        System.out.printf("Project        %s%n", repository.name());
        System.out.printf("Files          %d%n", repository.fileCount());
        System.out.printf("Directories    %d%n", repository.directoryCount());
        System.out.println();
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
        System.out.printf("(format=%s — health score and export arrive in later milestones)%n",
                parent.outputFormat());
        return 0;
    }
}