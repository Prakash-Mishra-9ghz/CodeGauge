package io.codegauge.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * {@code codegauge analyze <path>} — full repository analysis.
 *
 * <p>Delegation only: this class parses its argument and, once
 * {@code io.codegauge.analyzer.AnalysisManager} exists (v0.4+), will hand
 * off to it. Until then it reports that it is not yet implemented rather
 * than silently doing nothing.
 */
@Command(name = "analyze", description = "Run full repository analysis.")
final class AnalyzeCommand implements Callable<Integer> {

    @ParentCommand
    private RootCommand parent;

    @Parameters(index = "0", description = "Path to the repository to analyze.")
    private String path;

    @Override
    public Integer call() {
        System.out.printf(
                "analyze: not implemented yet (path=%s, format=%s)%n",
                path, parent.outputFormat());
        return 0;
    }
}