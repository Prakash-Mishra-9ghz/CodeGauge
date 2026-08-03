/**
 * Command-line entry point and argument parsing.
 *
 * <p>This package is responsible for turning process arguments into calls
 * against the rest of the application, and for translating results back
 * into process exit codes. It should contain no analysis logic of its own —
 * a command class parses arguments, delegates to the appropriate
 * collaborator (scanner, analyzer, report exporter), and prints or returns
 * the outcome.
 *
 * <p>Built on <a href="https://picocli.info/">Picocli</a>.
 */
package io.codegauge.cli;
