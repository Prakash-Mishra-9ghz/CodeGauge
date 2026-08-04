package io.codegauge.cli;

import io.codegauge.analyzer.DocumentationAnalyzer;
import io.codegauge.core.DocumentationResult;
import io.codegauge.core.Repository;
import io.codegauge.parser.MarkdownReadmeParser;
import io.codegauge.scanner.FileSystemRepositoryScanner;
import io.codegauge.scanner.RepositoryScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/** {@code codegauge docs <path>} — documentation analysis only. */
@Command(name = "docs", description = "Run documentation analysis only.")
final class DocsCommand implements Callable<Integer> {

    @ParentCommand
    private RootCommand parent;

    @Parameters(index = "0", description = "Path to the repository to analyze.")
    private String path;

    private final RepositoryScanner scanner = new FileSystemRepositoryScanner();
    private final DocumentationAnalyzer documentationAnalyzer =
            new DocumentationAnalyzer(new MarkdownReadmeParser());

    @Override
    public Integer call() {
        Repository repository;
        try {
            repository = scanner.scan(Path.of(path));
        } catch (IOException e) {
            System.err.println("Failed to scan repository: " + e.getMessage());
            return 1;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        DocumentationResult result = documentationAnalyzer.analyze(repository);
        printDocumentationReport(result);
        System.out.printf("(format=%s — export arrives in later milestones)%n", parent.outputFormat());
        return 0;
    }

    /** Shared with {@link AnalyzeCommand} so both commands print identically. */
    static void printDocumentationReport(DocumentationResult result) {
        System.out.println("Documentation");
        System.out.println("------------------------------");
        System.out.printf("README               %s%n", present(result.readmePresent()));
        System.out.printf("LICENSE              %s%n", present(result.licensePresent()));
        System.out.printf("Installation Guide   %s%n", present(result.installationSectionPresent()));
        System.out.printf("Usage Section        %s%n", present(result.usageSectionPresent()));
        System.out.printf("Examples             %s%n", present(result.examplesSectionPresent()));
        System.out.printf("Contributing Guide   %s%n", present(result.contributingPresent()));
        System.out.printf("Code of Conduct      %s%n", present(result.codeOfConductPresent()));
        System.out.printf("Screenshots          %s%n", present(result.screenshotsPresent()));
        System.out.println();
        System.out.printf("Documentation Score  %.1f / 10%n", result.score());
        System.out.println();
    }

    private static String present(boolean value) {
        return value ? "Present" : "Missing";
    }
}