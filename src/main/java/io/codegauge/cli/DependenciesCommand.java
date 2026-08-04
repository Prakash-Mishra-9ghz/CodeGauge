package io.codegauge.cli;

import io.codegauge.analyzer.DependencyAnalyzer;
import io.codegauge.core.DependencyResult;
import io.codegauge.core.Repository;
import io.codegauge.parser.MavenPomParser;
import io.codegauge.scanner.FileSystemRepositoryScanner;
import io.codegauge.scanner.RepositoryScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/** {@code codegauge dependencies <path>} — Maven dependency analysis only. */
@Command(name = "dependencies", description = "Run dependency analysis only.")
final class DependenciesCommand implements Callable<Integer> {

    @ParentCommand
    private RootCommand parent;

    @Parameters(index = "0", description = "Path to the repository to analyze.")
    private String path;

    private final RepositoryScanner scanner = new FileSystemRepositoryScanner();
    private final DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(new MavenPomParser());

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

        DependencyResult result = dependencyAnalyzer.analyze(repository);
        printDependencyReport(result);
        System.out.printf("(format=%s — export arrives in later milestones)%n", parent.outputFormat());
        return 0;
    }

    /**
     * Shared with {@link AnalyzeCommand} so the dependency section of a full
     * {@code analyze} run and a standalone {@code dependencies} run print
     * identically, without duplicating formatting logic in two places.
     */
    static void printDependencyReport(DependencyResult result) {
        if (!result.pomFound()) {
            System.out.println("No pom.xml found at repository root.");
            return;
        }

        System.out.println("Dependencies");
        System.out.println("------------------------------");
        if (result.dependencies().isEmpty()) {
            System.out.println("(none declared)");
        } else {
            result.dependencies().forEach(d ->
                    System.out.printf("%s:%s%s (%s)%n",
                            d.groupId(), d.artifactId(),
                            d.version().isEmpty() ? "" : ":" + d.version(),
                            d.scope()));
        }

        System.out.println();
        System.out.println("Plugins");
        System.out.println("------------------------------");
        if (result.plugins().isEmpty()) {
            System.out.println("(none declared)");
        } else {
            result.plugins().forEach(p ->
                    System.out.printf("%s:%s%s%n",
                            p.groupId(), p.artifactId(),
                            p.version().isEmpty() ? "" : ":" + p.version()));
        }

        if (!result.duplicateCoordinates().isEmpty()) {
            System.out.println();
            System.out.println("Duplicate Dependencies");
            System.out.println("------------------------------");
            result.duplicateCoordinates().forEach(System.out::println);
        }
        System.out.println();
    }
}