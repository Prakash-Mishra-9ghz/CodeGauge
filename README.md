# CodeGauge

A Java CLI tool that performs real repository analysis — project structure,
source code, documentation, and dependencies — and reports on engineering
quality and maintainability. Not a GitHub-stats wrapper: CodeGauge inspects
the repository itself.

**Status:** early development (v0.1 — project bootstrap). No analysis
functionality yet; see [Roadmap](#roadmap).

## Requirements

- Java 21+
- Maven 3.8+

## Building

```bash
mvn clean package
```

## Running

```bash
java -jar target/codegauge.jar
```

At this stage the CLI only prints its version banner. Argument parsing and
the `analyze` command arrive in v0.2.

## Architecture

CodeGauge follows a clean, layered architecture:

- `io.codegauge.core` — domain model (immutable, no I/O)
- `io.codegauge.scanner` — file system traversal (Java NIO)
- `io.codegauge.analyzer` — pluggable analyzers (Strategy pattern)
- `io.codegauge.report` — report model + exporters (console/JSON/HTML/CSV)
- `io.codegauge.cli` — argument parsing and command dispatch (Picocli)
- `io.codegauge.util` — small cross-cutting helpers

`core` has no dependency on any other package, which keeps domain logic
testable in isolation from the file system and from any particular output
format.

## Roadmap

| Version | Milestone |
|---|---|
| v0.1 | Project bootstrap |
| v0.2 | CLI |
| v0.3 | Repository Scanner |
| v0.4 | Metrics Engine |
| v0.5 | Dependency Analyzer |
| v0.6 | Documentation Analyzer |
| v0.7 | Health Score |
| v0.8 | JSON Export |
| v0.9 | HTML Report |
| v1.0 | Stable Release |

## License

MIT — see [LICENSE](LICENSE).

## Author

Prakash Mishra
