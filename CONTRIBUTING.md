# Contributing to CodeGauge

Thanks for considering contributing. This document covers how the project
is organized and what's expected of a change.

## Getting started

```bash
git clone https://github.com/Prakash-Mishra-9ghz/CodeGauge.git
cd CodeGauge
mvn clean package
```

Fork the repository, create a branch for your change, and open a pull
request against `main`.

## Project conventions

- **Package boundaries matter.** `io.codegauge.core` has zero dependencies
  on any other package in the project and performs no I/O — it's the
  domain model. See each package's `package-info.java` for its specific
  responsibility and dependency rules before adding a class to it.
- **Analyzers are pluggable.** New analysis dimensions implement the
  `Analyzer` interface rather than being bolted onto an existing analyzer
  or the CLI layer.
- **Tests are expected**, not optional, for new logic — see existing
  `*Test.java` files for the project's testing style (fakes/stubs over
  mocking frameworks, one behavior per test method).
- **JavaDoc on public types and methods**, especially where a design
  decision or a known limitation isn't obvious from the code alone.
- **No new dependency without discussion** — several existing dependencies
  (JavaParser, Jackson) were deliberate trade-offs; please open an issue
  before adding another.

## Reporting bugs

Open an issue with the command you ran, the output you got, and what you
expected instead. If it's a parsing/scoring inaccuracy, attaching the
generated report (`--json` or `--html`) is the most useful thing you can
include.

## Code of Conduct

Participation in this project is governed by our
[Code of Conduct](CODE_OF_CONDUCT.md).