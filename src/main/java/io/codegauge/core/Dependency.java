package io.codegauge.core;

import java.util.Objects;

/**
 * A single Maven dependency declaration.
 *
 * @param groupId    Maven groupId
 * @param artifactId Maven artifactId
 * @param version    resolved version if known, or {@code ""} if the POM
 *                    declares no version (e.g. managed by a parent/BOM) or
 *                    the {@code ${property}} reference could not be resolved
 * @param scope      dependency scope; defaults to {@code "compile"} when
 *                    absent, matching Maven's own default
 */
public record Dependency(String groupId, String artifactId, String version, String scope) {
    public Dependency {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(artifactId, "artifactId must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(scope, "scope must not be null");
    }

    /** @return the {@code groupId:artifactId} coordinate, ignoring version */
    public String coordinate() {
        return groupId + ":" + artifactId;
    }
}   