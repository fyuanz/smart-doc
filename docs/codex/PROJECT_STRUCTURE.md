# Project Structure

## Current Directory Map

```text
.
|-- AGENTS.md
|-- .gitignore
|-- docs/
|   |-- smartdoc-agent-design.md
|   `-- codex/
|       |-- PROJECT_CONTEXT.md
|       |-- PROJECT_STRUCTURE.md
|       |-- MODULES.md
|       |-- TASKS.md
|       `-- DECISIONS.md
|-- testbeds/
|   `-- springdoc-multi-package/
|       |-- pom.xml           # standalone Spring Boot parent
|       |-- README.md
|       |-- refresh-fixtures.ps1
|       |-- fixtures/         # account/business JSON and metadata
|       `-- src/              # sample application and contract/runtime tests
|-- smartdoc-agent-core/
|   |-- pom.xml
|   `-- src/                # input validation, Skill generation, local refs, tags and focused tests
`-- pom.xml
```

## Important Files

| Path | Purpose |
| --- | --- |
| `AGENTS.md` | First-read, test-first development, and documentation rules |
| `docs/smartdoc-agent-design.md` | v3.4 scope, service/document boundaries, compile updates, and acceptance |
| `pom.xml` | Java 17 Maven parent; aggregates the new core module |
| `docs/codex/DECISIONS.md` | Historical and current scope decisions |

P0 rebuilt the core POM and exact-version input boundary. P2 adds `SkillGenerator` and `DocumentReferences`, with separate fixture, contract, reference and limit tests under `src/test/java`. Legacy IR remains deleted.

## Implementation Locations

Core paths now exist; the Maven plugin remains proposed:

- `smartdoc-agent-core/src/main/java/com/smartdoc/agent/core/`: OpenApiInput.java (version/JSON boundary), SkillGenerator.java (service assembly), DocumentReferences.java (local graph, contract rendering and links).
- Trusted Skill template currently resides in SkillGenerator.java; no resources directory is needed.
- `smartdoc-agent-core/src/test/`: sanitized OpenAPI fixture, small boundary inputs, and meaningful semantic tests.
- `smartdoc-agent-maven-plugin/`: proposed thin compile integration, document preparation coordination, failure isolation, bounded execution, staging/replacement, and status.
- Plugin integration tests: full, single-service, partial-module, repeated, and parallel compilation, document readiness, and per-service non-blocking generation failures; choose test paths with the first implementation.

Do not add CLI, server, package repository, generic ingestion, or distribution modules. The implemented standalone fixture at `testbeds/springdoc-multi-package/` has `user`, `order`, `file`, and `common` packages and explicit `account`/`business` groups. It is outside the root reactor and must not become a production dependency. Future core tests consume its frozen sanitized snapshots rather than start it on every run.

## Generated Or Ignored Directories

- Maven `**/target/` output is not source.
- `smartdoc-agent-core/target/smartdoc/springdoc-multi-package-api/` is the verified test-generated Skill. Production output/publishing remains unimplemented.
- Staging and update status should be outside the final Skill directory and inside a controlled output parent; exact names are implementation details.
- Each service owns a unique Skill output, staging, lock, and status location. Proposed references use `references/documents/<documentId>/operations/`, `schemas/`, and optional `tags/`; single-document services also use a document namespace.
- Preserve safe local ignore rules for IDE files, secrets, logs, and temporary files.
- Generated restricted API documentation must not be committed as a substitute for sanitized fixtures.

## Caution Areas

- Do not silently restore the user's core deletions or call the missing-POM state a completed migration.
- Output replacement must only affect a validated generator-owned directory, never a source root or a directory with unrelated manual content.
- Do not treat a local test snapshot as proof of current-code freshness in production.
- Do not claim Maven integration covers independent IDE compilation without verification.
- Stable links and filenames matter; a ZIP identity/verification platform is outside scope.
- Never replace a shared parent containing multiple service outputs. Do not infer service boundaries from Java packages or Maven directory names.

