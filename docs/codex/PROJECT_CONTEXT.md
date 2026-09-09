# Project Context

## Purpose

SmartDoc-Agent converts API documentation into a Skill for frontend development. Every configured compilation must trigger a Skill update, and Skill generation failure must not interrupt the business build. All other product work is deferred.

The current source of truth is `docs/smartdoc-agent-design.md`, version 3.4.0. Microservices, multiple Java packages/modules, and multiple OpenAPI documents per service are required current scenarios. The previous compiler-plus-download-service scope remains superseded.

## Target Users

- Frontend developers using an API Skill with their coding agent.
- Backend maintainers connecting API documentation and Skill generation to their compilation workflow.

## Core Workflow

1. During each configured service compilation, prepare all configured required API documents and freeze the complete input set for this update.
2. Parse each OpenAPI 3.1.0 JSON document independently and resolve its local references within that document.
3. Render trusted Skill instructions, a compact catalog, operations, shared schemas, and optional tag navigation.
4. Validate a complete staged service Skill and replace only that service's generator-owned output directory.
5. On failure or timeout, warn, retain the previous complete Skill if available, and let the business compilation continue with its own normal success/failure semantics.

## Current Status

- Product scope v3.4 is ready as an implementation planning baseline; actual producer/lifecycle integration remains to be verified.
- The P0-P5 work plan is active. At the user's request, the standalone P1.3 testbed was implemented first; P0 remains ready and core work has not started.
- The Java 17 Maven parent exists and references `smartdoc-agent-core`.
- The legacy core POM, six IR records, and `ir-schema.json` have been removed. No replacement core implementation or tests exist.
- The missing core POM prevents Maven project loading; the earlier baseline passed with no tests before these deletions.
- Core generation and compilation integration remain unimplemented. A thin Maven plugin is proposed, not implemented.
- The target application's current-document production workflow, actual compile entry points, and output destination are unknown.
- The first realistic fixture exists at `testbeds/springdoc-multi-package/`: Java 17, Spring Boot 3.5.9, springdoc 2.8.15, four packages and two explicit groups. Five tests pass; validated OpenAPI 3.1.0 snapshots and metadata are in `fixtures/`. This alone does not prove independent microservices or ordinary-compile integration.

## Commands

Standalone testbed (independent of the broken root reactor):

```text
mvn -f testbeds/springdoc-multi-package/pom.xml test
mvn -f testbeds/springdoc-multi-package/pom.xml spring-boot:run
powershell -NoProfile -File testbeds/springdoc-multi-package/refresh-fixtures.ps1
```

Tests start the testbed on a random loopback port and close it afterward. Manual startup uses `127.0.0.1:18080`; see the testbed README. Only explicit refresh replaces frozen fixture files.

Existing Maven entry points, currently blocked by the missing core POM:

```text
mvn compile
mvn test
```

The target integration must cover ordinary and repeated compilation without requiring `clean`, and packaging that traverses compilation. Exact plugin binding/order must be verified with integration tests. IDE-independent compilation is not automatically covered by Maven integration.

The former `smartdoc skill build`, `smartdoc skill verify`, and `smartdoc serve` commands are deferred; they do not exist.

## Constraints

- Retain Java 17/Maven and the current OpenAPI 3.1.0 JSON input scope; no format compatibility expansion.
- A frozen fixture is sufficient for core tests, but repeated conversion of an old generated snapshot does not prove synchronization with current source code.
- Input preparation, freshness failure, generation, output replacement, and timeout must be included in the Skill failure boundary.
- Core consumes fixed local document bytes without LLM calls, business API calls, external-reference access, framework scanning, or UI parsing.
- The production document provider remains to be established; do not invent an automatically started business service or treat a deployed old instance as current compilation output.
- Preserve existing contract facts and local multi-level/shared/recursive references; report unsupported or dangling references explicitly.
- API free text is untrusted reference material and must not enter the trusted `SKILL.md` instruction template.
- Use bounded processing, safe stable filenames, complete staging, and replacement that preserves the old valid result on failure.
- Every compile attempts an update even when API content is unchanged. Identical content may remain identical.
- Minimal source metadata and update status are needed; package IDs, payload digest inventories, deterministic ZIPs, downloads, CLI, and installation management are deferred.
- Proposed default output is `target/smartdoc/<skillName>/`; final destination is unconfirmed. `clean` may remove prior target artifacts.
- Automatic generation-side updates do not imply cross-project installation or distribution.
- One service produces one Skill; each service accepts one or more explicitly identified OpenAPI documents. Multiple services update independently, including in separate repositories.
- Java packages, Maven modules, document groups, and services are distinct concepts. Existing document producers own package scanning; a service has one designated generation entry point with verified document readiness.
- Namespace operations, schemas, security schemes, links, and metadata by service/document identity. Do not overwrite or semantically merge same-named items across documents.
- All configured documents are required. One failure retains that service's old complete Skill and warns; other service updates continue. Explicit group removal cleans that group on the next successful update.
- Isolate output, staging, locking, and status by service. Partial compilation must report unavailable service documents rather than silently skipping or publishing mixed old/new inputs.
- Full, single-service, partial-module, repeated no-change, and parallel compile coverage remains to be verified. Global cross-service Skill aggregation and release coordination are deferred.

The testbed also provides Swagger UI at `http://127.0.0.1:18080/swagger-ui.html`, with account/business group selection for manual API inspection.

Milestone delivery follows AGENTS.md: verify each completed part of requested work, update documentation, commit, and push to the configured GitHub remote. This does not automatically begin the next product stage.
