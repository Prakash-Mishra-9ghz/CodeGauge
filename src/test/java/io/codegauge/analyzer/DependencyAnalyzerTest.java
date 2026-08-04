package io.codegauge.analyzer;

import io.codegauge.core.Dependency;
import io.codegauge.core.DependencyResult;
import io.codegauge.core.Repository;
import io.codegauge.core.SourceFile;
import io.codegauge.parser.PomParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DependencyAnalyzerTest {

    @Test
    void parsesRootPomWhenPresent() {
        DependencyResult fakeResult = new DependencyResult(
                true,
                List.of(new Dependency("g", "a", "1.0", "compile")),
                List.of(),
                List.of());
        PomParser fakeParser = pomFile -> fakeResult;

        DependencyAnalyzer analyzer = new DependencyAnalyzer(fakeParser);
        Repository repository = new Repository(
                "repo", Path.of("/repo"),
                List.of(new SourceFile(Path.of("pom.xml"), "xml", 500)),
                0);

        assertEquals(fakeResult, analyzer.analyze(repository));
    }

    @Test
    void reportsNotFoundWhenNoPomExists() {
        PomParser fakeParser = pomFile -> {
            throw new AssertionError("should not be called");
        };
        DependencyAnalyzer analyzer = new DependencyAnalyzer(fakeParser);

        Repository repository = new Repository(
                "repo", Path.of("/repo"),
                List.of(new SourceFile(Path.of("README.md"), "md", 100)),
                0);

        assertFalse(analyzer.analyze(repository).pomFound());
    }
}