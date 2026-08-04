package io.codegauge.core;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A single file discovered inside a repository.
 *
 * @param relativePath path of this file relative to the repository root
 * @param extension    file extension without the leading dot, or {@code ""}
 *                      if the file has none (e.g. {@code Dockerfile})
 * @param sizeInBytes  file size in bytes
 */
public record SourceFile(Path relativePath, String extension, long sizeInBytes) {
    public SourceFile {
        Objects.requireNonNull(relativePath, "relativePath must not be null");
        Objects.requireNonNull(extension, "extension must not be null");
        if (sizeInBytes < 0) {
            throw new IllegalArgumentException("sizeInBytes must not be negative");
        }
    }
}