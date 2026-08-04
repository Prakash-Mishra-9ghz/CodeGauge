package io.codegauge.analyzer;

import io.codegauge.core.DocumentationResult;
import io.codegauge.core.ReadmeSections;
import io.codegauge.core.Repository;
import io.codegauge.core.SourceFile;
import io.codegauge.parser.ReadmeParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Evaluates a repository's documentation: presence of key files (README,
 * LICENSE, CONTRIBUTING, CODE_OF_CONDUCT) and, for README specifically,
 * whether it covers installation, usage, examples, and screenshots.
 *
 * <p>Scope for v0.6: root-level file detection only — files inside
 * {@code .github/} (a common location for CONTRIBUTING/CODE_OF_CONDUCT on
 * GitHub) are not yet checked. Flagged as a follow-up, not implemented here,
 * to keep this milestone's surface area consistent with v0.5's root-only
 * {@code pom.xml} lookup.
 */
public final class DocumentationAnalyzer implements Analyzer {

    private static final Set<String> README_NAMES =
            Set.of("readme.md", "readme", "readme.rst", "readme.txt");
    private static final Set<String> LICENSE_NAMES =
            Set.of("license", "license.md", "license.txt", "copying");
    private static final Set<String> CONTRIBUTING_NAMES =
            Set.of("contributing.md", "contributing");
    private static final Set<String> CODE_OF_CONDUCT_NAMES =
            Set.of("code_of_conduct.md", "code_of_conduct");

    private final ReadmeParser readmeParser;

    public DocumentationAnalyzer(ReadmeParser readmeParser) {
        this.readmeParser = readmeParser;
    }

    @Override
    public DocumentationResult analyze(Repository repository) {
        Optional<SourceFile> readme = findRootFile(repository, README_NAMES);
        boolean licensePresent = findRootFile(repository, LICENSE_NAMES).isPresent();
        boolean contributingPresent = findRootFile(repository, CONTRIBUTING_NAMES).isPresent();
        boolean codeOfConductPresent = findRootFile(repository, CODE_OF_CONDUCT_NAMES).isPresent();

        ReadmeSections sections = ReadmeSections.none();
        if (readme.isPresent()) {
            Path absolutePath = repository.rootPath().resolve(readme.get().relativePath());
            try {
                sections = readmeParser.parse(absolutePath);
            } catch (IOException e) {
                System.err.println("Failed to read README: " + e.getMessage());
            }
        }

        double score = computeScore(
                readme.isPresent(), licensePresent, sections, contributingPresent, codeOfConductPresent);

        return new DocumentationResult(
                readme.isPresent(),
                licensePresent,
                sections.installationSectionPresent(),
                sections.usageSectionPresent(),
                sections.examplesSectionPresent(),
                contributingPresent,
                codeOfConductPresent,
                sections.screenshotsPresent(),
                score
        );
    }

    private static Optional<SourceFile> findRootFile(Repository repository, Set<String> candidateNames) {
        return repository.files().stream()
                .filter(f -> f.relativePath().getNameCount() == 1)
                .filter(f -> candidateNames.contains(
                        f.relativePath().getFileName().toString().toLowerCase(Locale.ROOT)))
                .findFirst();
    }

    /**
     * Weighted checklist, out of 10. README presence is weighted most
     * heavily since every other README-derived signal is meaningless
     * without it.
     */
    private static double computeScore(
            boolean readmePresent,
            boolean licensePresent,
            ReadmeSections sections,
            boolean contributingPresent,
            boolean codeOfConductPresent) {

        double score = 0.0;
        if (readmePresent) {
            score += 2.5;
        }
        if (licensePresent) {
            score += 1.5;
        }
        if (sections.installationSectionPresent()) {
            score += 1.5;
        }
        if (sections.usageSectionPresent()) {
            score += 1.5;
        }
        if (sections.examplesSectionPresent()) {
            score += 1.0;
        }
        if (contributingPresent) {
            score += 1.0;
        }
        if (codeOfConductPresent) {
            score += 0.5;
        }
        if (sections.screenshotsPresent()) {
            score += 0.5;
        }
        return score;
    }
}