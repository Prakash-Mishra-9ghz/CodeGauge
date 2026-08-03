package io.codegauge.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for CLI parsing and dispatch, exercised through {@link CommandLine}
 * directly rather than {@link Main#main} so exit codes can be asserted
 * without a {@code System.exit} call terminating the JVM.
 */
class MainTest {

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

    @Test
    void rejectsMultipleOutputFormatFlags() {
        CommandLine cmd = new CommandLine(new RootCommand());
        int exitCode = cmd.execute("analyze", ".", "--json", "--html");
        assertTrue(exitCode != 0, "conflicting format flags should fail");
    }
}