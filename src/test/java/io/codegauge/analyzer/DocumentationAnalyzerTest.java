package io.codegauge.analyzer;

import io.codegauge.core.DocumentationResult;
import io.codegauge.core.ReadmeSections;
import io.codegauge.core.Repository;
import io.codegauge.core.SourceFile;
import io.codegauge.parser.ReadmeParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentationAnalyzerTest {

    @Test
    void fullyDocumentedRepositoryScoresTen() {
        ReadmeParser fakeParser = readmeFile -> new ReadmeSections(true, true, true, true);
        DocumentationAnalyzer analyzer = new DocumentationAnalyzer(fakeParser);

        Repository repository = new Repository(
                "repo", Path.of("/repo"),
                List.of(
                        new SourceFile(Path.of("README.md"), "md", 1000),
                        new SourceFile(Path.of("LICENSE"), "", 100),
                        new SourceFile(Path.of("CONTRIBUTING.md"), "md", 200),
                        new SourceFile(Path.of("CODE_OF_CONDUCT.md"), "md", 200)
                ),
                0);

        DocumentationResult result = analyzer.analyze(repository);

        assertEquals(10.0, result.score());
        assertTrue(result.readmePresent());
        assertTrue(result.licensePresent());
    }

    @Test
    void emptyRepositoryScoresZero() {
        ReadmeParser fakeParser = readmeFile -> {
            throw new AssertionError("should not be called when README is absent");
        };
        DocumentationAnalyzer analyzer = new DocumentationAnalyzer(fakeParser);

        Repository repository = new Repository("repo", Path.of("/repo"), List.of(), 0);

        DocumentationResult result = analyzer.analyze(repository);

        assertEquals(0.0, result.score());
        assertFalse(result.readmePresent());
    }

    @Test
    void readmeOnlyScoresPartialCredit() {
        ReadmeParser fakeParser = readmeFile -> ReadmeSections.none();
        DocumentationAnalyzer analyzer = new DocumentationAnalyzer(fakeParser);

        Repository repository = new Repository(
                "repo", Path.of("/repo"),
                List.of(new SourceFile(Path.of("README.md"), "md", 500)),
                0);

        DocumentationResult result = analyzer.analyze(repository);

        assertEquals(2.5, result.score());
    }

    @Test
    void nonRootReadmeIsIgnored() {
        ReadmeParser fakeParser = readmeFile -> {
            throw new AssertionError("should not be called");
        };
        DocumentationAnalyzer analyzer = new DocumentationAnalyzer(fakeParser);

        Repository repository = new Repository(
                "repo", Path.of("/repo"),
                List.of(new SourceFile(Path.of("docs/README.md"), "md", 500)),
                0);

        DocumentationResult result = analyzer.analyze(repository);

        assertFalse(result.readmePresent());
    }
}