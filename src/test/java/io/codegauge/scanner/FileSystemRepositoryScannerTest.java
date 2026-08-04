package io.codegauge.scanner;

import io.codegauge.core.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemRepositoryScannerTest {

    private final RepositoryScanner scanner = new FileSystemRepositoryScanner();

    @Test
    void scansFlatDirectoryOfFiles(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Main.java"), "class Main {}");
        Files.writeString(tempDir.resolve("README.md"), "# hello");

        Repository repository = scanner.scan(tempDir);

        assertEquals(2, repository.fileCount());
        assertTrue(repository.files().stream().anyMatch(f -> f.extension().equals("java")));
        assertTrue(repository.files().stream().anyMatch(f -> f.extension().equals("md")));
    }

    @Test
    void countsNestedDirectories(@TempDir Path tempDir) throws IOException {
        Path nested = tempDir.resolve("src/main/java");
        Files.createDirectories(nested);
        Files.writeString(nested.resolve("App.java"), "class App {}");

        Repository repository = scanner.scan(tempDir);

        assertEquals(3, repository.directoryCount()); // src, src/main, src/main/java
        assertEquals(1, repository.fileCount());
    }

    @Test
    void excludesGitAndBuildDirectories(@TempDir Path tempDir) throws IOException {
        Path gitDir = tempDir.resolve(".git");
        Files.createDirectories(gitDir);
        Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/main");

        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(targetDir);
        Files.writeString(targetDir.resolve("Compiled.class"), "binary");

        Files.writeString(tempDir.resolve("Main.java"), "class Main {}");

        Repository repository = scanner.scan(tempDir);

        assertEquals(1, repository.fileCount());
        assertEquals(0, repository.directoryCount());
    }

    @Test
    void fileWithNoExtensionHasEmptyExtension(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Dockerfile"), "FROM openjdk:21");

        Repository repository = scanner.scan(tempDir);

        assertEquals("", repository.files().get(0).extension());
    }

    @Test
    void throwsWhenRootIsNotADirectory(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("notADirectory.txt");
        Files.writeString(file, "content");

        assertThrows(IllegalArgumentException.class, () -> scanner.scan(file));
    }
}
