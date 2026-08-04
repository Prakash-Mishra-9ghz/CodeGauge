package io.codegauge.parser;

import io.codegauge.core.DependencyResult;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Parses a Maven {@code pom.xml} into dependency/plugin facts.
 */
public interface PomParser {

    /**
     * @param pomFile filesystem path to a {@code pom.xml}
     * @return dependency and plugin information extracted from the file
     * @throws IOException if the file cannot be read or is not valid XML
     */
    DependencyResult parse(Path pomFile) throws IOException;
}