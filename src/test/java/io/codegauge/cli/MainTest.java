package io.codegauge.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test for {@link Main}.
 *
 * <p>This test exists primarily to prove the JUnit 5 + Surefire wiring
 * works from the very first commit, not to exercise meaningful behavior —
 * there isn't any yet. It will grow alongside {@link Main} as argument
 * parsing is introduced in v0.2.
 */
class MainTest {

    @Test
    void bannerContainsCurrentVersion() {
        assertTrue(Main.bannerText().contains(Main.VERSION));
    }

    @Test
    void versionMatchesExpectedBootstrapRelease() {
        assertEquals("0.1.0", Main.VERSION);
    }
}
