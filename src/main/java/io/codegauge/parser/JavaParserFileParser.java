package io.codegauge.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import io.codegauge.core.JavaFileMetrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * {@link JavaFileParser} backed by <a href="https://javaparser.org/">JavaParser</a>.
 *
 * <p>Line counting is done independently of AST parsing (plain line read),
 * so a file that fails to parse still contributes an accurate line count
 * via {@link JavaFileMetrics#unparsed}, even though its structural counts
 * are unknown.
 */
public final class JavaParserFileParser implements JavaFileParser {

    private final JavaParser javaParser;

    public JavaParserFileParser() {
        ParserConfiguration configuration = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        this.javaParser = new JavaParser(configuration);
    }

    @Override
    public JavaFileMetrics parse(Path absoluteFilePath, Path relativePath) throws IOException {
        int lineCount = countLines(absoluteFilePath);

        ParseResult<CompilationUnit> result = javaParser.parse(absoluteFilePath);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return JavaFileMetrics.unparsed(relativePath, lineCount);
        }

        CompilationUnit unit = result.getResult().get();

        int classCount = 0;
        int interfaceCount = 0;
        int enumCount = 0;
        int recordCount = 0;
        long totalTypeLines = 0;
        String largestTypeName = "";
        int largestTypeLines = 0;

        for (TypeDeclaration<?> type : unit.findAll(TypeDeclaration.class)) {
            int lines = lineSpanOf(type);
            totalTypeLines += lines;
            if (lines > largestTypeLines) {
                largestTypeLines = lines;
                largestTypeName = type.getFullyQualifiedName().orElse(type.getNameAsString());
            }
            if (type instanceof ClassOrInterfaceDeclaration coid) {
                if (coid.isInterface()) {
                    interfaceCount++;
                } else {
                    classCount++;
                }
            } else if (type instanceof EnumDeclaration) {
                enumCount++;
            } else if (type instanceof RecordDeclaration) {
                recordCount++;
            }
        }

        List<MethodDeclaration> methods = unit.findAll(MethodDeclaration.class);
        long totalMethodLines = methods.stream().mapToLong(this::lineSpanOf).sum();

        int fieldCount = unit.findAll(FieldDeclaration.class).stream()
                .mapToInt(f -> f.getVariables().size())
                .sum();

        int constructorCount = unit.findAll(ConstructorDeclaration.class).size();

        return new JavaFileMetrics(
                relativePath,
                lineCount,
                classCount,
                interfaceCount,
                enumCount,
                recordCount,
                methods.size(),
                fieldCount,
                constructorCount,
                totalMethodLines,
                totalTypeLines,
                largestTypeName,
                largestTypeLines
        );
    }

    private int lineSpanOf(Node node) {
        return node.getRange().map(r -> r.end.line - r.begin.line + 1).orElse(0);
    }

    private static int countLines(Path path) throws IOException {
        try (var lines = Files.lines(path)) {
            return (int) lines.count();
        }
    }
}