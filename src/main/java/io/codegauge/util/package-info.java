/**
 * Small, genuinely cross-cutting helpers with no domain knowledge.
 *
 * <p>This package is intentionally kept minimal. A "util" package is an easy
 * place for unrelated static methods to accumulate into a de facto God
 * class. Before adding something here, prefer: (1) does this belong as a
 * method on an existing domain type instead, or (2) is this specific to one
 * package (e.g. a scanner-only helper), in which case it should live there
 * as a package-private class instead of here.
 */
package io.codegauge.util;
