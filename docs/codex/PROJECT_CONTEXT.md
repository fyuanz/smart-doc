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

- Product scope v3.4 is the implementation baseline. Maven lifecycle integration for authoritative static documents is verified; runtime/generated-document integration still needs target evidence.
- The P0-P5 work plan is active. P0, P2, and P3 are complete; P4 is in progress with one verified input model.
- The Java 17 Maven parent exists and references `smartdoc-agent-core`.
- A new core POM and OpenApiInput validator pass 19 tests. Deleted legacy IR remains removed.
- Root Maven project loading and test discovery work; 57 tests pass (19 input, 20 content-generation, 15 safe-publication, and 3 Maven-goal tests).
- `smartdoc-agent-maven-plugin` implements the `generate-skill` goal with a default `compile` phase. It reads a complete configured local document set, calls the P2/P3 core boundary, and reports service-specific warnings without throwing a build failure for Skill update errors.
- A standalone two-service reactor verifies ordinary, repeated, targeted, parallel, and package-through-compile execution for authoritative static JSON, including input/configuration/write failures and preservation of Java compilation failures.
- The target application's current-document production workflow, actual compile entry points, and output destination are unknown.
- The first realistic fixture exists at `testbeds/springdoc-multi-package/`: Java 17, Spring Boot 3.5.9, springdoc 2.8.15, four packages and two explicit groups. Five tests pass; validated OpenAPI 3.1.0 snapshots and metadata are in `fixtures/`. This alone does not prove independent microservices or ordinary-compile integration.

## Commands

Standalone testbed (independent of the root reactor):

```text
mvn -f testbeds/springdoc-multi-package/pom.xml test
mvn -f testbeds/springdoc-multi-package/pom.xml spring-boot:run
powershell -NoProfile -File testbeds/springdoc-multi-package/refresh-fixtures.ps1
```

Tests start the testbed on a random loopback port and close it afterward. Manual startup uses `127.0.0.1:18080`; see the testbed README. Only explicit refresh replaces frozen fixture files.

Root Maven entry points (test verified):

```text
mvn compile
mvn test
```

The fixture-generation test writes a reviewable Skill to `smartdoc-agent-core/target/smartdoc/springdoc-multi-package-api/`. This test export is not a compile hook. Core callers use `new SkillGenerator().generate(serviceId, skillName, Map<String, byte[]>)` to obtain a complete immutable map of relative paths to UTF-8 content. All map entries are required; callers own the configured input set. `ServiceSkillUpdater.update(...)` runs bounded generation, validates the in-memory and staged trees, locks one output, and publishes or restores one complete generator-owned Skill. It returns an observable result and writes last-attempt status under the output parent's `.smartdoc/status/` directory.

The static-input testbed covers ordinary and repeated compilation without requiring `clean`, plus packaging that traverses compilation. A target with generated documents must still prove producer/goal order and freshness. IDE-independent compilation is not automatically covered by Maven integration.

Maven plugin lifecycle integration testbed:

```text
powershell -NoProfile -File testbeds/maven-plugin-integration/verify.ps1
```

The verifier installs the current plugin snapshot and exercises real Maven processes. It is intentionally separate from root unit tests.

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
- Full, single-service, repeated no-change, package-through-compile, and parallel Maven entry points are verified for independent services with static authoritative documents. A generated-document target, a service spanning partially built modules, and IDE compilation remain to be verified. Global cross-service Skill aggregation and release coordination are deferred.

The testbed also provides Swagger UI at `http://127.0.0.1:18080/swagger-ui.html`, with account/business group selection for manual API inspection.

Milestone delivery follows AGENTS.md: verify each completed part of requested work, update documentation, commit, and push to the configured GitHub remote. This does not automatically begin the next product stage.

