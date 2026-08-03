package io.codegauge.cli;

/**
 * Application entry point.
 *
 * <p>As of v0.1 this class only proves that the project builds and runs; it
 * does not yet parse arguments or dispatch commands. Picocli-based command
 * parsing and subcommand dispatch (e.g. {@code analyze}, {@code dependencies},
 * {@code docs}, {@code smells}) is introduced in milestone v0.2 and will
 * replace the body of {@link #main(String[])} below.
 */
public final class Main {

    /** CodeGauge's current version, surfaced by the {@code --version} banner. */
    public static final String VERSION = "0.1.0";

    private Main() {
        // Entry point class; not instantiable.
    }

    /**
     * Prints a version banner and exits.
     *
     * @param args process arguments; currently ignored, will be handed to
     *             Picocli's {@code CommandLine} in v0.2
     */
    public static void main(String[] args) {
        System.out.println(bannerText());
    }

    /**
     * Builds the banner text shown on startup.
     *
     * <p>Extracted as its own method (rather than inlined in
     * {@link #main(String[])}) so it can be unit tested without capturing
     * {@code System.out}.
     *
     * @return the banner text, without a trailing newline
     */
    static String bannerText() {
        return "CodeGauge v" + VERSION + " — Repository Quality Analyzer";
    }
}
