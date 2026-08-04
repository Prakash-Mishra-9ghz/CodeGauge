package io.codegauge.analyzer;

import io.codegauge.core.JavaFileMetrics;
import io.codegauge.core.MetricsResult;
import io.codegauge.core.Repository;
import io.codegauge.core.SourceFile;
import io.codegauge.parser.JavaFileParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MetricsAnalyzerTest {

    /** Fake parser returning pre-programmed results, keyed by relative path. */
    private static JavaFileParser fakeParser(Map<Path, JavaFileMetrics> byPath) {
        return (absoluteFilePath, relativePath) -> byPath.get(relativePath);
    }

    @Test
    void aggregatesAcrossMultipleJavaFiles() {
        Path a = Path.of("A.java");
        Path b = Path.of("B.java");

        JavaFileMetrics metricsA = new JavaFileMetrics(a, 100, 1, 0, 0, 0, 3, 2, 1, 30L, 80L, "A", 80);
        JavaFileMetrics metricsB = new JavaFileMetrics(b, 50, 1, 1, 0, 0, 2, 1, 1, 10L, 40L, "B", 40);

        MetricsAnalyzer analyzer = new MetricsAnalyzer(fakeParser(Map.of(a, metricsA, b, metricsB)));

        Repository repository = new Repository(
                "repo", Path.of("/repo"),
                List.of(new SourceFile(a, "java", 1000), new SourceFile(b, "java", 500)),
                0);

        MetricsResult result = analyzer.analyze(repository);

        assertEquals(2, result.javaFileCount());
        assertEquals(150, result.totalLinesOfCode());
        assertEquals(2, result.classCount());
        assertEquals(1, result.interfaceCount());
        assertEquals(5, result.methodCount());
        assertEquals(8.0, result.averageMethodLength()); // (30+10)/5
        assertEquals("A", result.largestClassName());
        assertEquals(80, result.largestClassLines());
        assertEquals(a, result.largestFilePath());
        assertEquals(100, result.largestFileLines());
    }

    @Test
    void ignoresNonJavaFiles() {
        MetricsAnalyzer analyzer = new MetricsAnalyzer(fakeParser(Map.of()));

        Repository repository = new Repository(
                "repo", Path.of("/repo"),
                List.of(new SourceFile(Path.of("README.md"), "md", 200)),
                0);

        MetricsResult result = analyzer.analyze(repository);

        assertEquals(0, result.javaFileCount());
        assertEquals(0, result.totalLinesOfCode());
        assertNull(result.largestFilePath());
    }

    @Test
    void avoidsDivisionByZeroWhenNoMethodsOrClassesFound() {
        Path a = Path.of("Empty.java");
        JavaFileMetrics empty = JavaFileMetrics.unparsed(a, 5);
        MetricsAnalyzer analyzer = new MetricsAnalyzer(fakeParser(Map.of(a, empty)));

        Repository repository = new Repository(
                "repo", Path.of("/repo"), List.of(new SourceFile(a, "java", 100)), 0);

        MetricsResult result = analyzer.analyze(repository);

        assertEquals(0.0, result.averageMethodLength());
        assertEquals(0.0, result.averageClassSize());
    }
}