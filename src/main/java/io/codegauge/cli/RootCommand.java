package io.codegauge.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Root command: {@code codegauge}.
 *
 * <p>Holds only options shared across every subcommand — currently the
 * output format. Analysis logic lives in the subcommands' delegates, never
 * here; this class exists purely to define shared flags and group
 * subcommands under one banner.
 */
@Command(
        name = "codegauge",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        subcommands = {
                AnalyzeCommand.class,
                DependenciesCommand.class,
                DocsCommand.class,
                SmellsCommand.class
        },
        description = "Repository quality analyzer."
)
public final class RootCommand {

    /**
     * Output format shared by all subcommands. Defaults to console output;
     * exporters for JSON/HTML/CSV are introduced in v0.8/v0.9 and will read
     * this field. Only one may be set at a time.
     */
    @Option(names = "--json", description = "Export report as JSON.")
    boolean json;

    @Option(names = "--html", description = "Export report as HTML.")
    boolean html;

    @Option(names = "--csv", description = "Export report as CSV.")
    boolean csv;

    /**
     * @return the requested output format, defaulting to {@link OutputFormat#CONSOLE}
     * @throws picocli.CommandLine.ParameterException if more than one format flag was set
     */
    OutputFormat outputFormat() {
        int count = (json ? 1 : 0) + (html ? 1 : 0) + (csv ? 1 : 0);
        if (count > 1) {
            throw new IllegalArgumentException("Only one of --json, --html, --csv may be specified.");
        }
        if (json) return OutputFormat.JSON;
        if (html) return OutputFormat.HTML;
        if (csv) return OutputFormat.CSV;
        return OutputFormat.CONSOLE;
    }
}