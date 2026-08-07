package io.codegauge.cli;

import picocli.CommandLine;

/**
 * Application entry point.
 *
 * <p>As of v0.8, argument parsing and subcommand dispatch is delegated to
 * Picocli via {@link RootCommand}. This class only wires up
 * {@link CommandLine} and translates its exit code to the process exit
 * code.
 */
public final class Main {

    /** CodeGauge's current version, surfaced by {@code --version}. */
    public static final String VERSION = "0.8.0";

    private Main() {
        // Entry point class; not instantiable.
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RootCommand()).execute(args);
        System.exit(exitCode);
    }
}