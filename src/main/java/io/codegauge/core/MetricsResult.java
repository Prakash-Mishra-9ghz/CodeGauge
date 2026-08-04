package io.codegauge.core;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Repository-wide source code metrics, aggregated across every {@code .java}
 * file. Produced by {@code io.codegauge.analyzer.MetricsAnalyzer}.
 *
 * <p>{@code totalLinesOfCode} counts Java source lines only — see the v0.4
 * milestone notes for why a whole-repository line count (spanning every
 * language) is deferred to the future Language Detection feature.
 *
 * @param largestFilePath path of the largest {@code .java} file, or
 *                         {@code null} if no Java files were found
 */
public record MetricsResult(
        int javaFileCount,
        long totalLinesOfCode,
        int classCount,
        int interfaceCount,
        int enumCount,
        int recordCount,
        int methodCount,
        int fieldCount,
        int constructorCount,
        double averageMethodLength,
        double averageClassSize,
        String largestClassName,
        int largestClassLines,
        Path largestFilePath,
        int largestFileLines
) implements AnalysisResult {

    public MetricsResult {
        Objects.requireNonNull(largestClassName, "largestClassName must not be null");
    }

    @Override
    public String analyzerName() {
        return "metrics";
    }
}