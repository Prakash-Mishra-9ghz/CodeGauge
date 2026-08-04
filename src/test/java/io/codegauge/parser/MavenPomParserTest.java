package io.codegauge.parser;

import io.codegauge.core.Dependency;
import io.codegauge.core.DependencyResult;
import io.codegauge.core.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenPomParserTest {

    private final MavenPomParser parser = new MavenPomParser();

    @Test
    void parsesDependenciesWithScopeAndVersion(@TempDir Path tempDir) throws IOException {
        Path pom = writePom(tempDir, """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter</artifactId>
                            <version>5.10.3</version>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>info.picocli</groupId>
                            <artifactId>picocli</artifactId>
                            <version>4.7.6</version>
                        </dependency>
                    </dependencies>
                </project>
                """);

        DependencyResult result = parser.parse(pom);

        assertTrue(result.pomFound());
        assertEquals(2, result.dependencies().size());

        Dependency junit = result.dependencies().get(0);
        assertEquals("org.junit.jupiter", junit.groupId());
        assertEquals("test", junit.scope());

        Dependency picocli = result.dependencies().get(1);
        assertEquals("compile", picocli.scope()); // default when <scope> absent
    }

    @Test
    void resolvesVersionPropertyPlaceholders(@TempDir Path tempDir) throws IOException {
        Path pom = writePom(tempDir, """
                <project>
                    <properties>
                        <jackson.version>2.17.2</jackson.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>com.fasterxml.jackson.core</groupId>
                            <artifactId>jackson-databind</artifactId>
                            <version>${jackson.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """);

        DependencyResult result = parser.parse(pom);

        assertEquals("2.17.2", result.dependencies().get(0).version());
    }

    @Test
    void detectsDuplicateDependencyDeclarations(@TempDir Path tempDir) throws IOException {
        Path pom = writePom(tempDir, """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>com.fasterxml.jackson.core</groupId>
                            <artifactId>jackson-databind</artifactId>
                            <version>2.17.2</version>
                        </dependency>
                        <dependency>
                            <groupId>com.fasterxml.jackson.core</groupId>
                            <artifactId>jackson-databind</artifactId>
                            <version>2.15.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """);

        DependencyResult result = parser.parse(pom);

        assertEquals(1, result.duplicateCoordinates().size());
        assertEquals("com.fasterxml.jackson.core:jackson-databind", result.duplicateCoordinates().get(0));
    }

    @Test
    void parsesBuildPlugins(@TempDir Path tempDir) throws IOException {
        Path pom = writePom(tempDir, """
                <project>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.13.0</version>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """);

        DependencyResult result = parser.parse(pom);

        assertEquals(1, result.plugins().size());
        Plugin plugin = result.plugins().get(0);
        assertEquals("maven-compiler-plugin", plugin.artifactId());
        assertEquals("3.13.0", plugin.version());
    }

    @Test
    void handlesPomWithNoDependenciesOrPlugins(@TempDir Path tempDir) throws IOException {
        Path pom = writePom(tempDir, "<project></project>");

        DependencyResult result = parser.parse(pom);

        assertTrue(result.pomFound());
        assertTrue(result.dependencies().isEmpty());
        assertTrue(result.plugins().isEmpty());
    }

    private Path writePom(Path tempDir, String content) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, content);
        return pom;
    }
}