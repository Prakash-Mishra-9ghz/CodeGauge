package io.codegauge.analyzer;

import io.codegauge.core.DependencyResult;
import io.codegauge.core.Repository;
import io.codegauge.core.SourceFile;
import io.codegauge.parser.PomParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Extracts Maven dependency and plugin information from a repository's
 * root {@code pom.xml}.
 *
 * <p>Scope for v0.5: single-module Maven projects only. Multi-module
 * projects (multiple {@code pom.xml} files) and Gradle projects are not yet
 * supported — see the milestone notes for why these are deferred rather
 * than half-implemented.
 */
public final class DependencyAnalyzer implements Analyzer {

    private final PomParser parser;

    public DependencyAnalyzer(PomParser parser) {
        this.parser = parser;
    }

    @Override
    public DependencyResult analyze(Repository repository) {
        Optional<SourceFile> pom = repository.files().stream()
                .filter(f -> f.relativePath().toString().equals("pom.xml"))
                .findFirst();

        if (pom.isEmpty()) {
            return DependencyResult.notFound();
        }

        Path absolutePath = repository.rootPath().resolve(pom.get().relativePath());
        try {
            return parser.parse(absolutePath);
        } catch (IOException e) {
            System.err.println("Failed to parse pom.xml: " + e.getMessage());
            return DependencyResult.notFound();
        }
    }
}