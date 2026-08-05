package io.codegauge.core;

/**
 * Overall repository health score, aggregated from other analyzers' results
 * by {@code io.codegauge.analyzer.HealthScoreCalculator}.
 *
 * @param documentationScore   always computable (0.0 is a valid score)
 * @param dependencyScore      {@code null} if no {@code pom.xml} was found —
 *                              there was nothing to assess, not zero
 * @param maintainabilityScore {@code null} if no Java files were found
 * @param overallScore         average of whichever components above are
 *                              non-null; {@code 0.0} if none were available
 */
public record HealthScoreResult(
        double documentationScore,
        Double dependencyScore,
        Double maintainabilityScore,
        double overallScore
) implements AnalysisResult {

    @Override
    public String analyzerName() {
        return "health";
    }
}