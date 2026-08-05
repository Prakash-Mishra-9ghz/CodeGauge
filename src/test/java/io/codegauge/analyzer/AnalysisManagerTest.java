package io.codegauge.analyzer;

import io.codegauge.core.AnalysisResult;
import io.codegauge.core.Repository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnalysisManagerTest {

    private record FakeResult(String analyzerName) implements AnalysisResult {
    }

    @Test
    void runsEveryAnalyzerAndCollectsResults() {
        Analyzer first = repository -> new FakeResult("first");
        Analyzer second = repository -> new FakeResult("second");
        AnalysisManager manager = new AnalysisManager(List.of(first, second));

        Repository repository = new Repository("repo", Path.of("/repo"), List.of(), 0);
        List<AnalysisResult> results = manager.runAll(repository);

        assertEquals(2, results.size());
    }

    @Test
    void findResultReturnsMatchingType() {
        Analyzer analyzer = repository -> new FakeResult("only");
        AnalysisManager manager = new AnalysisManager(List.of(analyzer));

        Repository repository = new Repository("repo", Path.of("/repo"), List.of(), 0);
        List<AnalysisResult> results = manager.runAll(repository);

        FakeResult found = AnalysisManager.findResult(results, FakeResult.class);
        assertEquals("only", found.analyzerName());
    }

    @Test
    void findResultThrowsWhenTypeMissing() {
        AnalysisManager manager = new AnalysisManager(List.of());
        Repository repository = new Repository("repo", Path.of("/repo"), List.of(), 0);
        List<AnalysisResult> results = manager.runAll(repository);

        assertThrows(IllegalStateException.class, () -> AnalysisManager.findResult(results, FakeResult.class));
    }
}