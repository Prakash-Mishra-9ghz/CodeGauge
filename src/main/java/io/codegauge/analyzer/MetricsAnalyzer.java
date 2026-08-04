package io.codegauge.analyzer;

import io.codegauge.core.JavaFileMetrics;
import io.codegauge.core.MetricsResult;
import io.codegauge.core.Repository;
import io.codegauge.core.SourceFile;
import io.codegauge.parser.JavaFileParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Aggregates {@link JavaFileParser} output across every {@code .java} file
 * in a repository into a repository-wide {@link MetricsResult}.
 *
 * <p>The parser is injected rather than constructed here, so this class can
 * be unit tested against a fake {@link JavaFileParser} without invoking
 * JavaParser or touching disk.
 */
public final class MetricsAnalyzer implements Analyzer {

    private final JavaFileParser parser;

    public MetricsAnalyzer(JavaFileParser parser) {
        this.parser = parser;
    }

    @Override
    public MetricsResult analyze(Repository repository) {
        List<SourceFile> javaFiles = repository.files().stream()
                .filter(f -> f.extension().equals("java"))
                .toList();

        long totalLinesOfCode = 0;
        int classCount = 0;
        int interfaceCount = 0;
        int enumCount = 0;
        int recordCount = 0;
        int methodCount = 0;
        int fieldCount = 0;
        int constructorCount = 0;
        long totalMethodLines = 0;
        long totalTypeLines = 0;
        int totalTypeCount = 0;

        String largestClassName = "";
        int largestClassLines = 0;
        Path largestFilePath = null;
        int largestFileLines = 0;

        for (SourceFile file : javaFiles) {
            Path absolutePath = repository.rootPath().resolve(file.relativePath());
            JavaFileMetrics metrics;
            try {
                metrics = parser.parse(absolutePath, file.relativePath());
            } catch (IOException e) {
                System.err.println("Skipping unreadable file: " + file.relativePath() + " (" + e.getMessage() + ")");
                continue;
            }

            totalLinesOfCode += metrics.lineCount();
            classCount += metrics.classCount();
            interfaceCount += metrics.interfaceCount();
            enumCount += metrics.enumCount();
            recordCount += metrics.recordCount();
            methodCount += metrics.methodCount();
            fieldCount += metrics.fieldCount();
            constructorCount += metrics.constructorCount();
            totalMethodLines += metrics.totalMethodLines();
            totalTypeLines += metrics.totalTypeLines();
            totalTypeCount += metrics.classCount() + metrics.interfaceCount()
                    + metrics.enumCount() + metrics.recordCount();

            if (metrics.largestTypeLines() > largestClassLines) {
                largestClassLines = metrics.largestTypeLines();
                largestClassName = metrics.largestTypeName();
            }
            if (metrics.lineCount() > largestFileLines) {
                largestFileLines = metrics.lineCount();
                largestFilePath = file.relativePath();
            }
        }

        double averageMethodLength = methodCount == 0 ? 0.0 : (double) totalMethodLines / methodCount;
        double averageClassSize = totalTypeCount == 0 ? 0.0 : (double) totalTypeLines / totalTypeCount;

        return new MetricsResult(
                javaFiles.size(),
                totalLinesOfCode,
                classCount,
                interfaceCount,
                enumCount,
                recordCount,
                methodCount,
                fieldCount,
                constructorCount,
                averageMethodLength,
                averageClassSize,
                largestClassName,
                largestClassLines,
                largestFilePath,
                largestFileLines
        );
    }
}