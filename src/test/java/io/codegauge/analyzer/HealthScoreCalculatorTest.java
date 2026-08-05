package io.codegauge.analyzer;

import io.codegauge.core.DependencyResult;
import io.codegauge.core.DocumentationResult;
import io.codegauge.core.HealthScoreResult;
import io.codegauge.core.MetricsResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HealthScoreCalculatorTest {

    private final HealthScoreCalculator calculator = new HealthScoreCalculator();

    private static final MetricsResult HEALTHY_METRICS = new MetricsResult(
            5, 500, 5, 0, 0, 0, 20, 10, 5, 15.0, 100.0, "Foo", 100, Path.of("Foo.java"), 100);
    private static final DependencyResult NO_DUPLICATES = new DependencyResult(true, List.of(), List.of(), List.of());
    private static final DocumentationResult FULL_DOCS = new DocumentationResult(
            true, true, true, true, true, true, true, true, 10.0);

    @Test
    void perfectRepositoryScoresTenAcrossAllComponents() {
        HealthScoreResult result = calculator.calculate(HEALTHY_METRICS, NO_DUPLICATES, FULL_DOCS);

        assertEquals(10.0, result.documentationScore());
        assertEquals(10.0, result.dependencyScore());
        assertEquals(10.0, result.maintainabilityScore());
        assertEquals(10.0, result.overallScore());
    }

    @Test
    void missingPomExcludesDependencyComponentRatherThanZeroingIt() {
        HealthScoreResult result = calculator.calculate(HEALTHY_METRICS, DependencyResult.notFound(), FULL_DOCS);

        assertNull(result.dependencyScore());
        assertEquals(10.0, result.overallScore()); // average of doc(10) + maintainability(10), dep excluded
    }

    @Test
    void noJavaFilesExcludesMaintainabilityComponent() {
        MetricsResult noJava = new MetricsResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0, "", 0, null, 0);
        HealthScoreResult result = calculator.calculate(noJava, NO_DUPLICATES, FULL_DOCS);

        assertNull(result.maintainabilityScore());
        assertEquals(10.0, result.overallScore());
    }

    @Test
    void duplicateDependenciesReducePenaltyPerDuplicate() {
        DependencyResult twoDuplicates = new DependencyResult(true, List.of(), List.of(), List.of("g:a", "g:b"));
        HealthScoreResult result = calculator.calculate(HEALTHY_METRICS, twoDuplicates, FULL_DOCS);

        assertEquals(6.0, result.dependencyScore()); // 10 - (2 * 2.0)
    }

    @Test
    void bloatedCodeStacksAllMaintainabilityPenalties() {
        MetricsResult bloated = new MetricsResult(
                5, 5000, 5, 0, 0, 0, 20, 10, 5, 50.0, 500.0, "Bloated", 500, Path.of("Bloated.java"), 1500);
        HealthScoreResult result = calculator.calculate(bloated, NO_DUPLICATES, FULL_DOCS);

        // methodLength penalty: min(4, (50-30)/10)=2.0
        // classSize penalty:   min(4, (500-300)/100)=2.0
        // largeFile penalty:   2.0
        // 10 - 2 - 2 - 2 = 4.0
        assertEquals(4.0, result.maintainabilityScore());
    }
}