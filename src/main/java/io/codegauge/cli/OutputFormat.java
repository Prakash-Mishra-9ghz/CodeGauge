package io.codegauge.cli;

/**
 * Report export format, selected via a root-level flag
 * ({@code --json}/{@code --html}/{@code --csv}) and shared by every
 * subcommand. Exporters for each non-console value are introduced in v0.8
 * (JSON) and v0.9 (HTML); CSV is not yet scheduled on the roadmap.
 */
enum OutputFormat {
    CONSOLE,
    JSON,
    HTML,
    CSV
}