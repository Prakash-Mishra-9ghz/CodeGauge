package io.codegauge.report;

import io.codegauge.core.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlReportExporterTest {

    private static RepositoryReport sampleReport(String projectName, Double dependencyScore, double maintainability) {
        Repository repo = new Repository(projectName, Path.of("/repo"),
                List.of(new SourceFile(Path.of("Main.java"), "java", 500)), 3);
        MetricsResult metrics = new MetricsResult(5, 500, 5, 1, 0, 2, 20, 10, 5,
                15.0, 100.0, "Foo", 100, Path.of("Foo.java"), 100);
        DependencyResult deps = new DependencyResult(true,
                List.of(new Dependency("info.picocli", "picocli", "4.7.6", "compile")),
                List.of(), List.of("com.fasterxml.jackson.core:jackson-databind"));
        DocumentationResult docs = new DocumentationResult(true, true, true, true, false, true, false, true, 8.5);
        HealthScoreResult health = new HealthScoreResult(8.5, dependencyScore, maintainability, 7.0);
        return new RepositoryReport(repo, metrics, deps, docs, health);
    }

    @Test
    void producesWellFormedSelfContainedHtmlWithNoJavaScript() {
        String html = new HtmlReportExporter().export(sampleReport("codegauge", 10.0, 10.0));

        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("codegauge"));
        assertTrue(html.contains("Repository Health Score"));
        assertFalse(html.contains("<script"));
    }

    @Test
    void escapesProjectNameToPreventBrokenMarkup() {
        String html = new HtmlReportExporter().export(sampleReport("<script>evil</script>", 10.0, 10.0));

        assertFalse(html.contains("<script>evil</script>"));
        assertTrue(html.contains("&lt;script&gt;"));
    }

    @Test
    void highScoreGetsGoodBadgeClass() {
        String html = new HtmlReportExporter().export(sampleReport("repo", 9.0, 9.0));
        assertTrue(html.contains("badge good"));
    }

    @Test
    void midScoreGetsWarnBadgeClass() {
        String html = new HtmlReportExporter().export(sampleReport("repo", 6.0, 9.0));
        assertTrue(html.contains("badge warn"));
    }

    @Test
    void lowScoreGetsBadBadgeClass() {
        String html = new HtmlReportExporter().export(sampleReport("repo", 3.0, 9.0));
        assertTrue(html.contains("badge bad"));
    }

    @Test
    void naComponentRendersAsMutedTextNotAScore() {
        String html = new HtmlReportExporter().export(sampleReport("repo", null, 9.0));
        assertTrue(html.contains("<span class=\"muted\">N/A</span>"));
    }

    @Test
    void duplicateDependenciesGetWarnStyling() {
        String html = new HtmlReportExporter().export(sampleReport("repo", 10.0, 10.0));
        assertTrue(html.contains("class=\"warn-heading\""));
        assertTrue(html.contains("li class=\"warn\""));
    }
}