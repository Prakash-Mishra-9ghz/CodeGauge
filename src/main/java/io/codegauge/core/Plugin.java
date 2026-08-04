package io.codegauge.core;

import java.util.Objects;

/**
 * A single Maven build plugin declaration ({@code <build><plugins><plugin>}).
 *
 * @param version resolved version if known, or {@code ""} if unspecified
 */
public record Plugin(String groupId, String artifactId, String version) {
    public Plugin {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(artifactId, "artifactId must not be null");
        Objects.requireNonNull(version, "version must not be null");
    }
}