package io.codegauge.parser;

import io.codegauge.core.ReadmeSections;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Parses a README file's content for structural signals (installation/usage/
 * examples sections, screenshots).
 */
public interface ReadmeParser {

    /**
     * @param readmeFile filesystem path to a README file
     * @return signals detected in the README's content
     * @throws IOException if the file cannot be read
     */
    ReadmeSections parse(Path readmeFile) throws IOException;
}