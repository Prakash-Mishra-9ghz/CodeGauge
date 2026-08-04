package io.codegauge.scanner;

import io.codegauge.core.Repository;
import io.codegauge.core.SourceFile;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link RepositoryScanner}, backed by Java NIO's
 * {@link Files#walkFileTree}.
 *
 * <p>Excluded directories (see {@link ScanExclusions}) are pruned via
 * {@link FileVisitResult#SKIP_SUBTREE} rather than filtered after a full
 * walk, so large ignored trees like {@code node_modules} are never
 * descended into.
 */
public final class FileSystemRepositoryScanner implements RepositoryScanner {

    @Override
    public Repository scan(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Not a directory: " + root);
        }

        Path normalizedRoot = root.toAbsolutePath().normalize();
        List<SourceFile> files = new ArrayList<>();
        int[] directoryCount = {0};

        Files.walkFileTree(normalizedRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName() == null ? "" : dir.getFileName().toString();
                if (!dir.equals(normalizedRoot) && ScanExclusions.isExcluded(dirName)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                if (!dir.equals(normalizedRoot)) {
                    directoryCount[0]++;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path relative = normalizedRoot.relativize(file);
                String fileName = file.getFileName().toString();
                int dotIndex = fileName.lastIndexOf('.');
                String extension = (dotIndex >= 0 && dotIndex < fileName.length() - 1)
                        ? fileName.substring(dotIndex + 1)
                        : "";
                files.add(new SourceFile(relative, extension, attrs.size()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Skip unreadable files (permissions, broken symlinks) rather
                // than aborting the whole scan over one bad entry.
                return FileVisitResult.CONTINUE;
            }
        });

        String name = normalizedRoot.getFileName() != null
                ? normalizedRoot.getFileName().toString()
                : normalizedRoot.toString();

        return new Repository(name, normalizedRoot, files, directoryCount[0]);
    }
}