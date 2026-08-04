package io.codegauge.scanner;

import java.util.Set;

/**
 * Directory names skipped during scanning: VCS internals and build/IDE
 * output that isn't part of the repository's actual source.
 *
 * <p>Package-private and intentionally not configurable yet — if a real
 * need for user-configurable exclusions shows up, promote this to a proper
 * config object rather than growing this class's responsibilities.
 */
final class ScanExclusions {

    private static final Set<String> EXCLUDED_DIRECTORY_NAMES = Set.of(
            ".git", "target", "build", "node_modules", ".idea", ".vscode", "dist", "out", ".gradle"
    );

    private ScanExclusions() {
    }

    static boolean isExcluded(String directoryName) {
        return EXCLUDED_DIRECTORY_NAMES.contains(directoryName);
    }
}