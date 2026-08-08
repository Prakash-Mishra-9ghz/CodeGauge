# CodeGauge

A Java CLI tool that performs real repository analysis — project structure,
source code, dependencies, and documentation — and reports on engineering
quality with a weighted health score. Not a GitHub-stats wrapper: CodeGauge
inspects the repository itself.

## Features

- **Repository scanning** — NIO-based file tree walk, with VCS/build
  directories (`.git`, `target`, `node_modules`, etc.) excluded
- **Source code metrics (Java)** — classes, interfaces, enums, records,
  methods, fields, constructors, average method length, average class size,
  largest class, largest file — via real AST parsing ([JavaParser](https://javaparser.org/)),
  not regex
- **Dependency analysis (Maven)** — parses `pom.xml`, extracts dependencies,
  scopes, plugins, resolves `${property}` placeholders, detects duplicate
  dependency declarations
- **Documentation analysis** — checks for README, LICENSE, CONTRIBUTING,
  CODE_OF_CONDUCT, and scans the README itself for installation/usage/
  examples/screenshots, producing a weighted score out of 10
- **Repository health score** — combines the above into an overall score;
  a component that can't be measured (e.g. no `pom.xml`) is excluded from
  the average rather than penalized
- **Export formats** — console (default), JSON, HTML (self-contained,
  no external assets or JavaScript)
- **Native packaging** — `jpackage` scripts to build a standalone
  Windows/Linux app with a bundled JVM (see [Native app](#native-app-optional))

## Requirements

- Java 21+
- Maven 3.8+

## Installation

Build from source:

```bash
git clone https://github.com/Prakash-Mishra-9ghz/CodeGauge.git
cd CodeGauge
mvn clean package
```

This produces a runnable shaded jar at `target/codegauge.jar`.

## Usage

```bash
java -jar target/codegauge.jar analyze <path>
java -jar target/codegauge.jar analyze <path> --json
java -jar target/codegauge.jar analyze <path> --html > report.html

java -jar target/codegauge.jar dependencies <path>
java -jar target/codegauge.jar docs <path>
```

Analyze the current directory:

```bash
java -jar target/codegauge.jar analyze .
```

## Examples
Repository Summary

Project CodeGauge
Files 70
Directories 27

Source Code Metrics (Java)

Java Files 57
Lines of Code 3062
Classes 32
...
Avg Method Length 12.4 lines
Avg Class Size 41.8 lines
Largest Class io.codegauge.report.HtmlReportExporter (163 lines)

Repository Health Score

Documentation 5.5 / 10
Dependencies 10.0 / 10
Maintainability 10.0 / 10

Overall 8.5 / 10

Real output from `codegauge analyze .` run against this repository itself.

## Screenshots

Coming soon — for now, see the [Examples](#examples) section above for
sample console output, and the HTML export for a styled report.

## Architecture
io.codegauge.cli entry point, argument parsing (Picocli)
io.codegauge.core domain model — immutable records, zero I/O
io.codegauge.scanner file system traversal (Java NIO)
io.codegauge.parser file content parsing (JavaParser, DOM XML, README text)
io.codegauge.analyzer pluggable analyzers (Strategy pattern) + health scoring
io.codegauge.report report aggregation + exporters (Console/JSON/HTML)
`core` depends on nothing else in the project — every other package depends
on `core`, never the reverse. This is what lets analyzers be unit-tested
against in-memory fixtures instead of real files or a real repository.

Each analyzer implements a common `Analyzer` interface; `AnalysisManager`
runs the configured set uniformly, so adding a new analysis dimension
doesn't require modifying existing ones.

## Native app (optional)

CodeGauge can be packaged as a standalone native app with a bundled JVM,
via `jpackage` (bundled with the JDK — no extra download):

```powershell
scripts\package-app-image-windows.bat
```

produces `target\dist\CodeGauge\CodeGauge.exe`, runnable directly without a
separate Java install. A real Windows installer (Start Menu entry,
uninstaller) is available via `scripts\package-installer-windows.bat`,
which additionally requires the [WiX Toolset](https://wixtoolset.org/).
Linux: `scripts/package-linux.sh`.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Code of Conduct

See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Roadmap

| Version | Milestone | Status |
|---|---|---|
| v0.1 | Project bootstrap | Done |
| v0.2 | CLI | Done |
| v0.3 | Repository Scanner | Done |
| v0.4 | Metrics Engine | Done |
| v0.5 | Dependency Analyzer | Done |
| v0.6 | Documentation Analyzer | Done |
| v0.7 | Health Score | Done |
| v0.8 | JSON Export | Done |
| v0.9 | HTML Report | Done |
| v1.0 | Stable Release + native packaging | Done |

Ideas under consideration for post-v1.0 (not yet scheduled): code smell
detection (large class/long method/deep nesting), multi-module Maven and
Gradle dependency support, GitHub repository cloning, CSV export.

## License

MIT — see [LICENSE](LICENSE).

## Author

Prakash Mishra

Google Summer of Code 2025 Contributor