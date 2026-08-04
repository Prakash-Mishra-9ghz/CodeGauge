package io.codegauge.cli;

import io.codegauge.core.Repository;
import io.codegauge.scanner.FileSystemRepositoryScanner;
import io.codegauge.scanner.RepositoryScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * {@code codegauge analyze <path>} — full repository analysis.
 *
 * <p>As of v0.3 this runs the repository scan and prints a basic summary.
 * Metrics, dependency/documentation analysis, code smells, and the health
 * score are not yet computed — those arrive in v0.4 onward via
 * {@code io.codegauge.analyzer.AnalysisManager}, which this class will
 * delegate to once it exists.
 */
@Command(name = "analyze", description = "Run full repository analysis.")
final class AnalyzeCommand implements Callable<Integer> {

    @ParentCommand
    private RootCommand parent;

    @Parameters(index = "0", description = "Path to the repository to analyze.")
    private String path;

    private final RepositoryScanner scanner = new FileSystemRepositoryScanner();

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

        System.out.println("Repository Summary");
        System.out.println("------------------------------");
        System.out.printf("Project        %s%n", repository.name());
        System.out.printf("Files          %d%n", repository.fileCount());
        System.out.printf("Directories    %d%n", repository.directoryCount());
        System.out.println();
        System.out.printf("(format=%s — metrics, health score, and export arrive in later milestones)%n",
                parent.outputFormat());
        return 0;
    }
}