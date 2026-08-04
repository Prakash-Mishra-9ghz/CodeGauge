package io.codegauge.parser;

import io.codegauge.core.ReadmeSections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownReadmeParserTest {

    private final MarkdownReadmeParser parser = new MarkdownReadmeParser();

    @Test
    void detectsInstallationUsageAndExamplesHeadings(@TempDir Path tempDir) throws IOException {
        Path readme = write(tempDir, """
                # MyProject

                ## Installation

                Run `mvn install`.

                ## Usage

                Do the thing.

                ## Examples

                See below.
                """);

        ReadmeSections sections = parser.parse(readme);

        assertTrue(sections.installationSectionPresent());
        assertTrue(sections.usageSectionPresent());
        assertTrue(sections.examplesSectionPresent());
        assertFalse(sections.screenshotsPresent());
    }

    @Test
    void detectsScreenshotsViaMarkdownImage(@TempDir Path tempDir) throws IOException {
        Path readme = write(tempDir, "# MyProject\n\n![screenshot](docs/screenshot.png)\n");

        assertTrue(parser.parse(readme).screenshotsPresent());
    }

    @Test
    void detectsScreenshotsHeadingWithoutImage(@TempDir Path tempDir) throws IOException {
        Path readme = write(tempDir, "# MyProject\n\n## Screenshots\n\nComing soon.\n");

        assertTrue(parser.parse(readme).screenshotsPresent());
    }

    @Test
    void reportsNothingForBareReadme(@TempDir Path tempDir) throws IOException {
        Path readme = write(tempDir, "# MyProject\n\nA short description.\n");

        ReadmeSections sections = parser.parse(readme);

        assertFalse(sections.installationSectionPresent());
        assertFalse(sections.usageSectionPresent());
        assertFalse(sections.examplesSectionPresent());
        assertFalse(sections.screenshotsPresent());
    }

    private Path write(Path tempDir, String content) throws IOException {
        Path readme = tempDir.resolve("README.md");
        Files.writeString(readme, content);
        return readme;
    }
}