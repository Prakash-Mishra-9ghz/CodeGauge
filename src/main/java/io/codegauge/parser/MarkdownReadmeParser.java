package io.codegauge.parser;

import io.codegauge.core.ReadmeSections;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link ReadmeParser} using plain heading/keyword detection — no markdown
 * AST library, since the signals needed (does a heading mention
 * "installation"? is there an image?) don't require a full parse tree.
 *
 * <p>Detection is heading-based for text sections ({@code ## Installation}),
 * plus a standalone check for markdown image syntax anywhere in the file
 * for screenshots, since screenshots are often embedded without their own
 * heading.
 */
public final class MarkdownReadmeParser implements ReadmeParser {

    private static final Pattern HEADING = Pattern.compile("^#{1,6}\\s+.*");
    private static final Pattern IMAGE = Pattern.compile("!\\[[^]]*]\\([^)]+\\)");

    @Override
    public ReadmeSections parse(Path readmeFile) throws IOException {
        List<String> lines = Files.readAllLines(readmeFile);

        boolean installation = false;
        boolean usage = false;
        boolean examples = false;
        boolean screenshots = false;

        for (String line : lines) {
            String lower = line.toLowerCase();
            if (HEADING.matcher(line).matches()) {
                if (lower.contains("install")) {
                    installation = true;
                }
                if (lower.contains("usage") || lower.contains("getting started")) {
                    usage = true;
                }
                if (lower.contains("example")) {
                    examples = true;
                }
                if (lower.contains("screenshot") || lower.contains("demo")) {
                    screenshots = true;
                }
            }
            if (IMAGE.matcher(line).find()) {
                screenshots = true;
            }
        }

        return new ReadmeSections(installation, usage, examples, screenshots);
    }
}