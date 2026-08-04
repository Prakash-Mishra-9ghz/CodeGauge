package io.codegauge.scanner;

import io.codegauge.core.Repository;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Discovers the contents of a repository rooted at a given path.
 *
 * <p>Implementations perform the file system I/O; everything downstream
 * (analyzers, reporters) works only with the resulting {@link Repository}.
 */
public interface RepositoryScanner {

    /**
     * @param root directory to scan
     * @return an immutable {@link Repository} describing what was found
     * @throws IOException              if the file tree cannot be walked
     * @throws IllegalArgumentException if {@code root} is not a directory
     */
    Repository scan(Path root) throws IOException;
}