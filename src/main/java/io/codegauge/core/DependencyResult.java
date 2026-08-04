package io.codegauge.core;

import java.util.List;
import java.util.Objects;

/**
 * Result of dependency analysis for a repository's root {@code pom.xml}.
 *
 * @param pomFound             whether a {@code pom.xml} was found at the
 *                              repository root
 * @param duplicateCoordinates {@code groupId:artifactId} coordinates declared
 *                              more than once in {@code <dependencies>} —
 *                              a genuine code smell regardless of version
 */
public record DependencyResult(
        boolean pomFound,
        List<Dependency> dependencies,
        List<Plugin> plugins,
        List<String> duplicateCoordinates
) implements AnalysisResult {

    public DependencyResult {
        dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
        plugins = List.copyOf(Objects.requireNonNullElse(plugins, List.of()));
        duplicateCoordinates = List.copyOf(Objects.requireNonNullElse(duplicateCoordinates, List.of()));
    }

    /** @return a result representing "no pom.xml found" */
    public static DependencyResult notFound() {
        return new DependencyResult(false, List.of(), List.of(), List.of());
    }

    @Override
    public String analyzerName() {
        return "dependencies";
    }
}