package io.codegauge.core;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Structural metrics extracted from a single {@code .java} file.
 *
 * <p>Intermediate value type: produced by
 * {@code io.codegauge.parser.JavaFileParser}, consumed only by
 * {@code io.codegauge.analyzer.MetricsAnalyzer} to build the repository-wide
 * {@link MetricsResult}. Not intended to be surfaced to reports directly.
 *
 * @param relativePath     path relative to the repository root
 * @param lineCount        total lines in the file
 * @param classCount       top-level and nested class declarations
 * @param interfaceCount   interface declarations
 * @param enumCount        enum declarations
 * @param recordCount      record declarations
 * @param methodCount      method declarations (including interface defaults)
 * @param fieldCount       field declarations (each variable in a multi-variable
 *                         declaration counted separately)
 * @param constructorCount constructor declarations
 * @param totalMethodLines sum of line spans of every method, for computing
 *                         average method length across the repository
 * @param totalTypeLines   sum of line spans of every type declaration, for
 *                         computing average class size across the repository
 * @param largestTypeName  fully-qualified name of the largest type declaration
 *                         in this file, or {@code ""} if none
 * @param largestTypeLines line span of the largest type declaration in this file
 */
public record JavaFileMetrics(
        Path relativePath,
        int lineCount,
        int classCount,
        int interfaceCount,
        int enumCount,
        int recordCount,
        int methodCount,
        int fieldCount,
        int constructorCount,
        long totalMethodLines,
        long totalTypeLines,
        String largestTypeName,
        int largestTypeLines
) {
    public JavaFileMetrics {
        Objects.requireNonNull(relativePath, "relativePath must not be null");
        Objects.requireNonNull(largestTypeName, "largestTypeName must not be null");
    }

    /**
     * Builds a result for a file whose line count is known but whose
     * structure could not be parsed (all structural counts are zero).
     *
     * @param relativePath path relative to the repository root
     * @param lineCount    total lines in the file
     * @return a {@link JavaFileMetrics} with zeroed structural fields
     */
    public static JavaFileMetrics unparsed(Path relativePath, int lineCount) {
        return new JavaFileMetrics(relativePath, lineCount, 0, 0, 0, 0, 0, 0, 0, 0L, 0L, "", 0);
    }
}