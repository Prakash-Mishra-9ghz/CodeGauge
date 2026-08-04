package io.codegauge.core;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * An analyzed repository: the root path plus everything discovered inside it.
 *
 * <p>Produced by {@code io.codegauge.scanner.RepositoryScanner}. Consumed
 * (read-only) by every analyzer.
 *
 * @param name           repository name, derived from the root directory's name
 * @param rootPath       absolute, normalized path to the repository root
 * @param files          every discovered file, defensively copied to an
 *                        immutable list
 * @param directoryCount number of non-excluded, non-root directories found
 */
public record Repository(String name, Path rootPath, List<SourceFile> files, int directoryCount) {
    public Repository {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(rootPath, "rootPath must not be null");
        Objects.requireNonNull(files, "files must not be null");
        if (directoryCount < 0) {
            throw new IllegalArgumentException("directoryCount must not be negative");
        }
        files = List.copyOf(files);
    }

    /** @return total number of files discovered */
    public int fileCount() {
        return files.size();
    }
}