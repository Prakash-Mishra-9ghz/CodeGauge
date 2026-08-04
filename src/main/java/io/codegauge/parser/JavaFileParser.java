package io.codegauge.parser;

import io.codegauge.core.JavaFileMetrics;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Parses a single {@code .java} file into structural metrics.
 *
 * <p>Separate from {@code io.codegauge.scanner}, which only discovers that
 * a file exists — this package is responsible for understanding what's
 * inside it. Kept as an interface so the metrics engine isn't hard-wired to
 * JavaParser specifically.
 */
public interface JavaFileParser {

    /**
     * @param absoluteFilePath filesystem path to read
     * @param relativePath     path relative to the repository root, carried
     *                         through into the result for reporting
     * @return structural metrics for the file
     * @throws IOException if the file cannot be read
     */
    JavaFileMetrics parse(Path absoluteFilePath, Path relativePath) throws IOException;
}