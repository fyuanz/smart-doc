# Modules

## Module Index

| Module | Responsibility | Status |
| --- | --- | --- |
| Parent project | Java 17/Maven dependency management and module aggregation | Buildable; root tests execute |
| `smartdoc-agent-core` | OpenAPI-to-Skill content conversion and necessary checks | P0 and P2 complete; 39 total tests |
| `smartdoc-agent-maven-plugin` | Compilation trigger and safe, non-blocking update coordination | Proposed, not implemented |
| AI project docs | Persistent scope, context, and implementation guidance | Exists; refreshed for v3.4 |
| `testbeds/springdoc-multi-package` | Standalone annotated sample and validated two-document fixture export | Implemented; five tests pass, snapshots frozen |

## Parent Project

Coordinates: `com.smartdoc.agent:smart-doc-agent:1.0.0-SNAPSHOT`.

The parent describes OpenAPI-to-Skill conversion. Core uses existing managed Jackson 2.16.1 and JUnit 5.10.2, compiler 3.13.0 and Surefire 3.2.5. No legacy IR is restored. Add a plugin module only with its first tested slice.

## Core

P2 is complete: `SkillGenerator.generate(serviceId, skillName, documents)` returns an immutable complete content map and performs no filesystem or network actions. `DocumentReferences` renders contract JSON in Markdown with document-local graph links. Operation files preserve original operations plus effective path/operation parameters, servers and security; schema and other referenced component files preserve exact JSON semantics. Catalogs list all operations, tags and schemas by document. Names are safe lowercase IDs; operation paths, schema pointers and tags use SHA-256 filenames to avoid unsafe characters and case collisions. Metadata records the generator format version, service/Skill identity, full document set, each input digest, OpenAPI/API versions, and operation/schema counts.

Bounds: 32 documents, 8 MiB per document, 32 MiB aggregate input, 128 graph nesting levels, 5000 schema/reference targets per document, 10000 output files and 64 MiB output. The final output cap is checked before returning the content map, not a process memory guarantee. Local JSON Pointer fragments, including root and escaped pointers, and bare multi-level Path Item aliases are supported. External, dangling, anchored, dynamic and rebased references fail explicitly; ambiguous Path Item `$ref` siblings are rejected. Example/default/enum/const and extension payloads remain data, not reference traversal targets. Known OpenAPI container shapes are checked, but core remains a converter rather than a full OpenAPI specification validator.

The test exports only sanitized snapshots under target. No filesystem update/rollback, source freshness, compiler hook or agent discovery is implied.

Target responsibilities:

- Accept a complete set of explicitly identified OpenAPI JSON documents for one service, each with the retained exact `3.1.0` input boundary; parse documents independently.
- Preserve API parameters, requests, responses, media types, authentication, inheritance/overrides, and schema facts.
- Resolve local shared, multi-level, recursive, and inline schema relationships with bounded traversal.
- Render a trusted `SKILL.md`, compact catalog, operation/schema files, optional tag navigation, and minimal source metadata.
- Check required structure, safe filenames, conflicts, bounds, and link targets.
- Return results and diagnostics; do not conceal conversion failure as success.
- Preserve service/document provenance and namespace same-named operations, schemas, and security definitions. Local references resolve only inside their source document; no automatic cross-document merge, deduplication, or external-reference support.
- Render one complete service Skill with a group-aware catalog and per-document references. Record all source document identities/digests and preserve documented server/authentication context without inventing gateway routes.

Dependency boundary:

- No Maven API, Spring Boot, springdoc internals, UI, LLM SDK, business API requests, or external-reference fetching.
- Introduce only the internal types needed by tests and rendering, not a speculative generic model or schema platform.
- No ZIP packaging, package identity system, artifact server, CLI framework, retrieval, chat, or installation management.

## Compilation Integration (Proposed Maven Plugin)

Target responsibilities:

- Execute once per participating service per configured compilation, including repeated no-change compilation and builds that traverse compilation; avoid duplicate module-inherited generation.
- Coordinate the actual current-document producer and the converter in a verified order.
- Detect producer failure even when an old document remains on disk; never report old-input fallback as successful synchronization.
- Isolate Skill configuration/preparation/conversion/write failures, enforce bounded execution, and emit actionable warnings without failing business compilation.
- Generate into staging and replace only a complete valid generator-owned Skill; retain/recover the previous valid result on failure.
- Handle output conflicts without interleaved writes and prevent timed-out work from later publishing.
- Record last-attempt status outside the Skill; preserve the old source identity after a failed attempt.
- Coordinate all required documents for one service before replacement. Any required document failure preserves the whole previous service Skill; other services update independently.
- Scope output, staging, mutual exclusion, and status by service. Reject colliding service outputs without overwriting or clearing their shared parent.
- Establish a service generation owner and verify full/partial/parallel build entry points. Partial builds with incomplete required input must diagnose the missed update; do not assume parent aggregator execution proves child readiness.

Core errors remain observable; only the integration layer decides the non-blocking build behavior. Existing business build failures must remain failures. Host process failure or inability to load the plugin is not an exception a running generator can intercept.

The current-document provider, concrete lifecycle binding, IDE coverage, output location, and timeout mechanism are not implemented or verified. Do not make a default runtime-service dependency from these unknowns.

## Deferred Modules And Contracts

The earlier `smartdoc-agent-cli`, `smartdoc-agent-server`, deterministic package/verify stack, package manifest schema, download APIs, and artifact repository are not current work.

The minimal Spring Boot testbed is implemented under `testbeds/springdoc-multi-package/` with an independent Spring Boot 3.5.9 parent and springdoc WebMVC UI 2.8.15. It owns sample controllers/DTO annotations, explicit groups, real HTTP export and Swagger UI/group-discovery assertions, three MockMvc behavior tests, and frozen sanitized inputs with source digests. `refresh-fixtures.ps1` runs clean tests and refreshes snapshots only on success. It does not implement core parsing or production compilation hooks. Test-only startup is bounded and loopback-only, and does not establish a requirement to start target business services during compilation.

Also defer automatic cross-project install/update, lock/drift management, search, RAG, AI enrichment, TypeScript, alternate knowledge-source types/formats, Agent plugins/MCP, and multi-tenant operations. Multiple OpenAPI documents within one service are required, not an alternate knowledge-source feature. Global cross-service Skill merging/release orchestration is deferred.

Generation-side automatic updates are required; cross-project installation management is a separate deferred capability.

