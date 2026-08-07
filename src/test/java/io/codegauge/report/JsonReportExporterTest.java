package io.codegauge.report;

import io.codegauge.core.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonReportExporterTest {

    @Test
    void producesValidLookingJsonWithPathsAsStrings() {
        Repository repo = new Repository("codegauge", Path.of("/repo"),
                List.of(new SourceFile(Path.of("Main.java"), "java", 500)), 3);
        MetricsResult metrics = new MetricsResult(5, 500, 5, 1, 0, 2, 20, 10, 5,
                15.0, 100.0, "Foo", 100, Path.of("Foo.java"), 100);
        DependencyResult deps = DependencyResult.notFound();
        DocumentationResult docs = new DocumentationResult(true, true, true, true, false, true, false, true, 8.5);
        HealthScoreResult health = new HealthScoreResult(8.5, null, 10.0, 9.25);

        RepositoryReport report = new RepositoryReport(repo, metrics, deps, docs, health);

        String json = new JsonReportExporter().export(report);

        assertTrue(json.startsWith("{"));
        assertTrue(json.contains("\"name\" : \"codegauge\""));
        assertTrue(json.contains("\"largestFilePath\" : \"Foo.java\"")); // Path serialized as plain string
        assertTrue(json.contains("\"dependencyScore\" : null"));
    }
}