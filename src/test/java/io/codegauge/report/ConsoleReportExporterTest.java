package io.codegauge.report;

import io.codegauge.core.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleReportExporterTest {

    private static RepositoryReport sampleReport() {
        Repository repo = new Repository("codegauge", Path.of("/repo"),
                List.of(new SourceFile(Path.of("Main.java"), "java", 500)), 3);
        MetricsResult metrics = new MetricsResult(5, 500, 5, 1, 0, 2, 20, 10, 5,
                15.0, 100.0, "Foo", 100, Path.of("Foo.java"), 100);
        DependencyResult deps = new DependencyResult(true,
                List.of(new Dependency("info.picocli", "picocli", "4.7.6", "compile")),
                List.of(), List.of());
        DocumentationResult docs = new DocumentationResult(true, true, true, true, false, true, false, true, 8.5);
        HealthScoreResult health = new HealthScoreResult(8.5, 10.0, 10.0, 9.5);
        return new RepositoryReport(repo, metrics, deps, docs, health);
    }

    @Test
    void includesAllSections() {
        String output = new ConsoleReportExporter().export(sampleReport());

        assertTrue(output.contains("Repository Summary"));
        assertTrue(output.contains("Source Code Metrics"));
        assertTrue(output.contains("Dependencies"));
        assertTrue(output.contains("Documentation"));
        assertTrue(output.contains("Repository Health Score"));
        assertTrue(output.contains("Overall            9.5 / 10"));
    }

    @Test
    void reportsMissingPomWithoutDependencyList() {
        RepositoryReport report = sampleReport();
        RepositoryReport withoutPom = new RepositoryReport(
                report.repository(), report.metrics(), DependencyResult.notFound(),
                report.documentation(), report.health());

        String output = new ConsoleReportExporter().export(withoutPom);

        assertTrue(output.contains("No pom.xml found"));
    }
}