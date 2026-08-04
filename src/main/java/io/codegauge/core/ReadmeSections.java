package io.codegauge.core;

/**
 * Structural signals detected inside a README's content.
 *
 * <p>Intermediate value type: produced by
 * {@code io.codegauge.parser.ReadmeParser}, consumed only by
 * {@code io.codegauge.analyzer.DocumentationAnalyzer}.
 */
public record ReadmeSections(
        boolean installationSectionPresent,
        boolean usageSectionPresent,
        boolean examplesSectionPresent,
        boolean screenshotsPresent
) {
    /** @return an instance with every signal absent, e.g. when no README exists */
    public static ReadmeSections none() {
        return new ReadmeSections(false, false, false, false);
    }
}