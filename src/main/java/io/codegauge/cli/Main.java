package io.codegauge.cli;

import picocli.CommandLine;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Application entry point.
 *
 * <p>As of v1.0, argument parsing and subcommand dispatch is delegated to
 * Picocli via {@link RootCommand}. This class only wires up
 * {@link CommandLine} and translates its exit code to the process exit
 * code.
 */
public final class Main {

    /** CodeGauge's current version, surfaced by {@code --version}. */
    public static final String VERSION = "1.0.0";

    private Main() {
        // Entry point class; not instantiable.
    }

    public static void main(String[] args) {
        // Force UTF-8 regardless of the host platform's default console
        // encoding. On Windows this matters concretely: redirecting output
        // to a file (`> report.html`) was producing mojibake for non-ASCII
        // characters (an em dash rendered as U+FFFD) because System.out's
        // encoding wasn't guaranteed to match the UTF-8 we declare in the
        // HTML's own <meta charset> tag.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        int exitCode = new CommandLine(new RootCommand()).execute(args);
        System.exit(exitCode);
    }
}