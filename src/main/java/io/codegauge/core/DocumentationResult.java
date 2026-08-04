package io.codegauge.core;

/**
 * Result of documentation analysis for a repository.
 *
 * @param score weighted score out of 10 — see
 *              {@code io.codegauge.analyzer.DocumentationAnalyzer} for the
 *              weighting rationale
 */
public record DocumentationResult(
        boolean readmePresent,
        boolean licensePresent,
        boolean installationSectionPresent,
        boolean usageSectionPresent,
        boolean examplesSectionPresent,
        boolean contributingPresent,
        boolean codeOfConductPresent,
        boolean screenshotsPresent,
        double score
) implements AnalysisResult {

    @Override
    public String analyzerName() {
        return "documentation";
    }
}