# Project Context

## Purpose

SmartDoc-Agent is a Java-based intelligent software documentation engine. It extracts deterministic metadata from compiled bytecode or optional source code during the build, uses an LLM to produce a structured `smartdoc.json` knowledge base, and serves low-latency guidance at runtime.

The detailed product and architecture specification is `docs/smartdoc-agent-design.md`, which is the current source of truth for product requirements and confirmed architectural decisions.

## Target Users

- SDK and component-library maintainers.
- CI/CD and platform engineering teams.
- Downstream development teams and third-party integrators.
- Users who need guidance for black-box JARs or legacy systems.

## Core Workflows

1. Parse `.class`/`.jar` input by default, or `.java` input when source access is allowed.
2. Normalize extracted facts into the versioned intermediate representation (IR).
3. Generate and validate the semantic `smartdoc.json` knowledge base during the build.
4. Answer runtime questions from local search results.
5. Fall back to a runtime probe and LLM when local knowledge is missing or low confidence.

## Current Status

- The Maven parent project and `smartdoc-agent-core` module exist.
- Java IR records and `ir-schema.json` are implemented.
- A local Git repository is initialized with `main` as its primary branch and an initial project snapshot.
- The 2026-09-07 initialization baseline passes `mvn test` across both Maven reactor projects.
- No test sources exist yet, so the successful baseline currently verifies compilation and Maven configuration rather than behavior.
- Bytecode parsing, source parsing, LLM integration, generation, runtime search, probe, CLI, plugins, testbeds, and demos remain planned.

## Key Commands

```bash
mvn test
mvn compile
```

## Constraints

- Java 17 and Maven multi-module build.
- Bytecode input is the default to protect source code.
- The IR must contain only facts that parsers can extract deterministically.
- Runtime search should avoid network and LLM calls on the primary path.
- The planned runtime SDK target is under 300 KB and a typical local lookup target is under 20 ms.
