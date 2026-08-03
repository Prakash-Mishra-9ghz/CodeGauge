package io.codegauge.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/** {@code codegauge docs <path>} — documentation analysis only. */
@Command(name = "docs", description = "Run documentation analysis only.")
final class DocsCommand implements Callable<Integer> {

    @ParentCommand
    private RootCommand parent;

    @Parameters(index = "0", description = "Path to the repository to analyze.")
    private String path;

    @Override
    public Integer call() {
        System.out.printf(
                "docs: not implemented yet (path=%s, format=%s)%n",
                path, parent.outputFormat());
        return 0;
    }
}