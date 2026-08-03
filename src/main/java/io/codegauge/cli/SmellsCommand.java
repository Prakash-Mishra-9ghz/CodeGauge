package io.codegauge.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/** {@code codegauge smells <path>} — code smell detection only. */
@Command(name = "smells", description = "Run code smell detection only.")
final class SmellsCommand implements Callable<Integer> {

    @ParentCommand
    private RootCommand parent;

    @Parameters(index = "0", description = "Path to the repository to analyze.")
    private String path;

    @Override
    public Integer call() {
        System.out.printf(
                "smells: not implemented yet (path=%s, format=%s)%n",
                path, parent.outputFormat());
        return 0;
    }
}