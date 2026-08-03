/**
 * File system traversal.
 *
 * <p>Responsible for walking a repository's directory tree (via Java NIO's
 * {@code Files.walk}/{@code FileVisitor} APIs) and producing the
 * {@code io.codegauge.core} domain objects that describe what was found.
 * This is the only package permitted to perform file system I/O for the
 * purpose of discovering repository contents.
 *
 * <p>Analyzers consume the domain objects this package produces; they do
 * not walk the file system themselves. This keeps analyzers unit-testable
 * with in-memory fixtures instead of real directories on disk.
 */
package io.codegauge.scanner;
