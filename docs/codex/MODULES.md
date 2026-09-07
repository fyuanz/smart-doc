# Modules

## Module Index

| Module | Responsibility | Main Paths | Status |
| --- | --- | --- | --- |
| Parent project | Shared versions, Java level, and module aggregation | `pom.xml` | Implemented |
| `smartdoc-agent-core` | Deterministic IR contract; planned parsing, LLM adaptation, and knowledge generation | `smartdoc-agent-core/` | IR implemented; other areas planned |
| AI project docs | Persistent AI context and documentation maintenance workflow | `AGENTS.md`, `.agents/skills/ai-project-docs/`, `docs/codex/` | Implemented |

## Parent Project

Responsibilities:

- Aggregate Maven modules.
- Set Java 17 compilation and UTF-8 encoding.
- Manage Jackson 2.16.1 and JUnit 5.10.2 versions.

Public contracts:

- Maven coordinates `com.smartdoc.agent:smart-doc-agent:1.0.0-SNAPSHOT`.

## smartdoc-agent-core

Responsibilities:

- Define the versioned intermediate representation for projects, packages, classes, fields, methods, and parameters.
- Maintain the matching `ir-schema.json` contract.
- Later host bytecode/source parsers, LLM adapters, and `smartdoc.json` generation as described in the design document.

Main paths:

- `smartdoc-agent-core/src/main/java/com/smartdoc/agent/core/ir/`
- `smartdoc-agent-core/src/main/resources/schema/ir-schema.json`

Dependencies:

- Parent Maven project.
- Jackson Databind.

Public contracts:

- Java IR record types.
- IR JSON Schema version `0.1`.

## Planned Modules

The design document proposes CLI, runtime, probe, Maven plugin, testbed, and demo modules. They are not currently declared by the parent Maven build and must remain marked as planned until created.
