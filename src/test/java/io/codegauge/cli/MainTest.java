package io.codegauge.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    private final ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void captureStdout() {
        originalOut = System.out;
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    @Test
    void versionOptionReportsCurrentVersion() {
        CommandLine cmd = new CommandLine(new RootCommand());
        assertEquals(Main.VERSION, cmd.getCommandSpec().version()[0]);
    }

    @Test
    void analyzeCommandRequiresPathArgument() {
        CommandLine cmd = new CommandLine(new RootCommand());
        int exitCode = cmd.execute("analyze");
        assertTrue(exitCode != 0, "missing required <path> should be a usage error");
    }

    @Test
    void analyzeCommandAcceptsPathAndSucceeds() {
        CommandLine cmd = new CommandLine(new RootCommand());
        int exitCode = cmd.execute("analyze", ".");
        assertEquals(0, exitCode);
    }

    /**
     * Regression test for the bug reported after v0.9: {@code --json}/
     * {@code --html} placed AFTER the subcommand and its path (the natural
     * way to type it, and what real users do) must be recognized. This
     * requires {@code scope = ScopeType.INHERIT} on {@link RootCommand}'s
     * options — without it, Picocli only accepts these flags before the
     * subcommand name, and silently reports "Unknown option" instead.
     */
    @Test
    void analyzeCommandAcceptsOutputFormatFlagAfterSubcommandAndPath() {
        CommandLine cmd = new CommandLine(new RootCommand());
        int exitCode = cmd.execute("analyze", ".", "--json");

        assertEquals(0, exitCode);
        String output = capturedOut.toString();
        assertTrue(output.trim().startsWith("{"),
                "expected JSON output when --json follows the subcommand and path, got: " + output);
    }

    @Test
    void rejectsMultipleOutputFormatFlagsWithCleanErrorNotStackTrace() {
        ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(capturedErr));
        try {
            CommandLine cmd = new CommandLine(new RootCommand());
            int exitCode = cmd.execute("analyze", ".", "--json", "--html");

            assertEquals(1, exitCode);
            String err = capturedErr.toString();
            assertTrue(err.contains("Only one of --json, --html, --csv may be specified"));
            assertTrue(!err.contains("at io.codegauge"), "should not print a Java stack trace to the user");
        } finally {
            System.setErr(originalErr);
        }
    }
}