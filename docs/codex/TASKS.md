# Tasks

## Current Phase

Scope review completed: one or more API documents per service → one frontend Skill per service, updated on every configured compilation, with generation failures isolated from the business build and other services. Product design v3.4.0 is the implementation baseline. P1.3's standalone multi-package testbed is implemented; P0 is complete; the first P2 snapshot conversion slice is complete, while full conversion coverage and compilation integration remain open.

## Completed Tasks

| Task | Acceptance |
| --- | --- |
| Establish repository guidance and AI documentation | Root instructions and five core context files exist |
| Remove the project-local documentation skill | Skill, templates, and active configuration references removed; project guidance and context documents retained |
| Establish historical project baseline | Git and Maven parent exist; the earlier baseline passed with no tests before core deletions |
| Adopt OpenAPI document input | Source/bytecode scanning and UI scraping are superseded |
| Retain current input boundary | Exact OpenAPI 3.1.0 JSON; broader formats and UI compatibility deferred |
| Review current product scope | Design v3.3 distinguishes required conversion/compile updates from deferred distribution and platform work |
| Document non-blocking updates | Defines per-compile execution, fresh-input dependency, staging/replacement, warnings, and retained old output on failure |
| Include microservices and multi-document services | v3.4 defines service/document ownership, namespace isolation, complete-service replacement, and independent failures |
| Synchronize project docs | Context, structure, modules, tasks, and decisions match v3.4 scope |
| Establish implementation work plan | P0-P5 define dependencies, acceptance evidence, and target-integration gates; no implementation is marked complete |
| Select the first realistic fixture workflow | Plan a minimal multi-package Spring Boot service with explicit document groups; distinguish fixture export from compile-time integration |
| P1.3: Implement the standalone test service | Java 17 / Boot 3.5.9 / springdoc 2.8.15; four packages, two explicit groups, sample endpoints and validated frozen JSON |
| Add Swagger UI for manual inspection | UI entry, JavaScript assets and exact account/business configuration verified; 5 total tests pass |
| Establish milestone delivery workflow | AGENTS.md records verification, documentation updates, commit and normal push after each completed part of requested work |

## Active Tasks

On 2026-09-09 the user requested the minimal Spring Boot test service first. P1.3 is complete as a local fixture slice; its independent POM bypasses the missing root core module without restoring deleted files. P0 is now complete; P1's production integration gates remain open.

P1.3 evidence:

- Red: `mvn -B -f testbeds/springdoc-multi-package/pom.xml clean test` executed 4 tests, 4 assertion failures, 0 errors (missing group and sample endpoints returned 404).
- Green: the same service's `test` passed 4 tests, 0 failures/errors. `refresh-fixtures.ps1` repeated `clean test` successfully and saved both validated snapshots and metadata.
- Actual HTTP output: exact OpenAPI 3.1.0; account = 2 operations/3 schemas, business = 2 operations/4 schemas. Descriptions, group boundaries, parameters, requests/responses, required/minimum constraints, security, shared/recursive local refs and multipart are asserted.
- Environment: Maven 3.9.16, Azul JDK 17.0.19. Initial sandbox javac resource-close failures were setup failures, not behavioral red evidence; successful verification ran outside that sandbox.
- P1.1/P1.2/P1.4-P1.6 remain open; test export does not prove compile-time production freshness, non-blocking failures, microservice independence or reactor ordering.

## Work Plan

Status vocabulary: **Ready** means it can be started; **In progress** means a verified slice exists with remaining acceptance; **Pending** means listed dependencies must be met; **Needs target evidence** identifies an external-information gate; **Complete** requires recorded verification evidence.

| Stage | Deliverable | Dependencies | Status |
| --- | --- | --- | --- |
| P0 | Buildable core foundation and first tested input rule | Existing repository | Complete |
| P1 | Verified current-document and compilation integration contract | Standalone testbed independent of P0; target configuration for final verification | P1.3 complete; remaining integration needs target evidence |
| P2 | Usable per-service Skill from one or more documents | P0; independent of the target producer | In progress: two-snapshot Skill delivered |
| P3 | Complete service updates with observable failure and preserved output | P2 | Pending |
| P4 | Real per-compilation integration, including multiple services/modules | P1 verified, P2, P3 | Pending |
| P5 | End-to-end/frontend acceptance and minimal usage instructions | P4 | Pending |

The user's testbed-first request advances standalone P1.3 ahead of P0. Next establish P0, then continue core and integration work. If target configuration is unavailable, continue P2/P3 with explicit fixtures while leaving P1 unverified. Do not call fixture-only generation a working production integration.

### P0 — Establish The Build And First Red-To-Green Slice

- P0.1: Establish a minimal core POM consistent with the Java 17 parent and correct the obsolete bytecode description. Do not implicitly restore deleted legacy IR or add a compatibility layer.
- P0.2: Add a tiny sanitized input fixture and a meaningful failing test for the retained OpenAPI root-version rule; implement only that first rule and verify green.
- P0.3: Confirm the test runner actually discovers and executes tests. A missing-POM error, test compilation error, or `No tests to run` is not the intended behavioral red/green evidence.

The user-selected first realistic integration test is the Spring Boot producer in P1.3 below. P0's small version-rule test remains the fast unit-test foundation; neither a running sample application nor exported files alone count as effective assertions.

Exit evidence: runnable `mvn test`, nonzero test count, the first behavior's observed failure followed by success, and recorded commands/results. Initial POM wiring is setup; behavioral implementation follows failing tests.

### P1 — Verify Current Documents And Compile Entry Points

- P1.1: Record the target serviceId/skillName, packages, Maven modules, required documentId/source list, document producer, actual compile commands, one generation owner, output path, and retention expectation across `clean`.
- P1.2: Distinguish authoritative static contracts from code-generated contracts. Establish how every required document signals successful preparation for the current update; an old file or an unchanged digest alone is not that signal.
- P1.3 — **Complete (2026-09-09)**: The standalone fixture at `testbeds/springdoc-multi-package/` uses Java 17, Spring Boot 3.5.9 and `springdoc-openapi-starter-webmvc-ui:2.8.15`. No database, registry or gateway is required; Swagger UI supports manual inspection. This is a test service, not a production dependency. The following acceptance requirements are implemented, except the explicitly separate P1.4/P4 probe:
  - Write failing assertions for the expected groups, exact `openapi: 3.1.0`, operation membership, field descriptions, required fields, responses, and local schema references before adding the corresponding controllers/models/configuration.
  - Use packages such as `user`, `order`, `file`, and `common`; define two explicit groups, for example `account` (user) and `business` (order/file). Packages do not automatically produce separate documents.
  - Add a few representative endpoints with Spring mappings, validation, and explicit `@Operation`, `@Parameter`, `@Schema`, and response annotations where needed. Assert descriptions actually reach JSON; do not assume ordinary source comments are exported by default.
  - Include query/path parameters, a JSON request, multipart upload, multiple responses, security metadata, and shared/recursive DTOs. Keep the sample small and supplement malformed/colliding inputs with focused core fixtures.
  - Explicitly set `springdoc.api-docs.version=OPENAPI_3_1`; start only the test service on loopback during fixture export/integration tests, retrieve `/v3/api-docs/account` and `/v3/api-docs/business`, and assert both documents before accepting the set. Ensure bounded startup/capture and process cleanup on failure.
  - Freeze the passing JSON set with exact producer versions, capture command, source digests, and expected operation/schema counts for ordinary offline core tests. They later drive one service Skill in P2; generated Skills remain unimplemented until their failing content tests are implemented.
  - Official springdoc Maven export uses a running application and the integration-test phase (`mvn verify` in its example). This fixture path is not proof of `mvn compile` coverage; ordinary `mvn package` does not reach integration-test under the default lifecycle. Verify exact commands rather than call all three entry points equivalent.
  - A single multi-package application proves multi-group input, not independent microservices or multi-module reactor ordering. Add a second minimal service/multi-module probe for P1.4/P4's isolation and ordering cases, without building a full microservice platform.
- P1.4: Verify generation timing on ordinary compile and repeated no-change compile; then examine full, service-only, partial-module, and parallel entry points. Do not assume a parent aggregator runs after its children or that IDE compilation delegates to Maven.
- P1.5: Make document preparation fail while an old JSON still exists; prove the result is an update failure and business compilation can still proceed. Do not publish the old JSON as fresh.
- P1.6: Record the selected lifecycle/producer contract and supported entry points in DECISIONS.md. Keep an unverified entry point explicitly unverified, and revise the approach if it cannot meet the required compile semantics.

Exit evidence: repeatable command/configuration, observed preparation/generation ordering, current-input evidence, and the stale-input failure case. Final acceptance requires target configuration or a confirmed representative equivalent. A synthetic producer proves orchestration only; it does not prove the actual springdoc integration.

If the existing producer requires application startup or a later lifecycle phase, resolve that conflict in this stage rather than silently changing the product to package-time/runtime generation. Do not add automatic business-service startup without an explicit reviewed integration decision.

Producer reference: [springdoc grouping and properties](https://springdoc.org/v2/), [springdoc Maven export workflow](https://github.com/springdoc/springdoc-openapi-maven-plugin), and [Maven lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html), checked 2026-09-09. Tool versions and actual outputs still require implementation-time verification.

### P2 — Convert Documents Into A Usable Service Skill

- P2.1: Add failing tests for explicit service/document ownership and required input sets. Cover one service with two groups and a second service with colliding operation/schema/security names.
- P2.2: Implement the thin per-document model and extraction needed for parameters, request/response content, media types, security, inheritance/overrides, examples, multipart, nullability, and schema structure.
- P2.3: Test and implement document-local shared, recursive, multi-level, inline, dangling, and forbidden external references. Never resolve an absent local target against another document's same-named object.
- P2.4: Generate trusted `SKILL.md`, group-aware catalog, per-document operation/schema references, necessary tag navigation, and complete source metadata. Keep names/links stable and preserve documented server context without inventing gateway routing.
- P2.5: Test safe paths, case collisions, processing/file limits, and source-text isolation from trusted instructions. Verify content semantically instead of copying all generated output into broad golden snapshots.

Exit evidence: a complete service Skill from two documents, navigable references, all expected operations represented, correct conflicting-name ownership, and meaningful core tests. Core verification uses fixed local files with no LLM, business API, or external-reference calls.

### P3 — Make Service Updates Complete And Failure-Safe

- P3.1: Test first, then implement complete staging, validation, and replacement of only the generator-owned service directory. Do not clear the shared parent.
- P3.2: Verify modified/deleted interfaces and explicitly removed groups leave no stale references after success. A missing required input is a failure, not an implicit removal.
- P3.3: Cover first-run failure, failed updates with existing output, and one-of-many document failure. Preserve the entire old valid service result and record last-attempt status outside it.
- P3.4: Exercise denied writes, replacement failure, timeout, same-service write contention, and conflicting service outputs. Timed-out work cannot publish later; replacement failure must preserve/recover the old result.
- P3.5: Verify two independent services update concurrently and one service's failure cannot mutate or prevent the other's result. Keep status and temporary files isolated by service.

Exit evidence: targeted failure-injection tests and comparisons proving the old service tree remains complete/unchanged on failure, deleted files disappear on success, and unrelated outputs remain untouched. These tests do not yet prove Maven's exit behavior; P4 does.

### P4 — Integrate The Complete Update Into Compilation

- P4.1: Finish the thin plugin using the verified P1 contract and P2/P3 implementation. Parse Skill configuration, prepare all required documents, generate, and replace within the Skill failure boundary.
- P4.2: Add real build tests for the entry-point matrix below; plugin inheritance must not cause multiple modules to overwrite one service output.
- P4.3: Exercise document/configuration/conversion/write/timeout errors through the real build entry point. Emit service/document-specific warnings, preserve old output if present, and keep otherwise valid business builds successful.
- P4.4: Deliberately introduce a business compilation error and prove it still fails the build. Do not use blanket shell exit-code suppression.

| Scenario | Required observation |
| --- | --- |
| Ordinary compile, first run | Participating service produces a complete Skill |
| Repeat compile, no source change | Update still executes; content may remain identical |
| Edit API contract/code and compile | Correct fresh contract reaches the corresponding Skill |
| Delete operation/group and compile | Stale files removed on complete success |
| Full multi-module compilation | Exactly one complete update per participating service |
| Compile one service | Other service outputs unaffected; no dependency on unrelated services being online |
| Partial module compilation | Verified update entry point; incomplete required input produces a clear service warning and preserved prior output |
| Parallel compilation | Service outputs isolated, no interleaved writes or silent output collision |
| Packaging through compilation | Update occurs without a second duplicate service generation |
| One document/service generation fails | Old affected service preserved; other services and valid business compilation continue |
| Business compilation fails | Build failure remains visible |

Exit evidence: exact commands, observed update invocation counts, expected output/source identities, warnings, and exit outcomes for this matrix. Unit tests alone cannot close P4. Independent IDE entry points must be verified if included in the agreed target workflow.

### P5 — Validate Frontend Use And Close The MVP

- P5.1: Put generated test Skills into a consuming test project and verify discovery/routing with the actual supported agent workflow.
- P5.2: Use two service Skills together: locate same-named interfaces, explain required versus nullable fields, authentication, responses, and service/group ownership; check frontend call examples against the contract.
- P5.3: Confirm multi-package producer coverage using expected operations and record missing/unknown source facts without fabricating them.
- P5.4: Add concise verified setup/compile/output/failure-diagnosis instructions, including required documents and what a failed update means. Document actual commands only after they work.
- P5.5: Close tasks with evidence and update current status/decisions. Keep downloads, ZIP infrastructure, installation management, and other deferred capabilities out of acceptance.

Exit evidence: P0-P4 checks pass, representative frontend tasks are recorded, and the actual producer/compile workflow has no unacknowledged coverage gaps. Remaining target-specific uncertainty prevents claiming full MVP completion.

## Plan Tracking And Estimates

- Every stage records status, dependencies, actual verification commands/results, and remaining blockers when work changes.
- Work test-first: observe a behavioral failure, implement the smallest matching behavior, verify green, then run appropriate integration checks.
- Keep source and test files below approximately 800-1000 lines by responsibility.
- Do not require target access to begin P0 or core conversion; do require it (or a confirmed equivalent) to close P1 and production integration acceptance.
- Earlier single-document effort estimates are provisional. Re-estimate the remaining work after P1 confirms producer/lifecycle feasibility; no fixed delivery date is committed by this plan.

## Deferred Work

- Deterministic ZIP packaging, full file digest/identity envelopes, independent verification CLI, immutable publishing, and package repositories.
- Read-only download service, HTTP manifests, cache headers, gateways, deployment, and public/internal distribution decisions.
- Independent CLI and separate YAML configuration platform.
- Cross-project automatic installation/update, locks, and drift handling.
- A single merged Skill for all microservices, global service version coordination, and cross-repository build scheduling.
- A full microservice test platform with database, service discovery, gateway, or custom UI. The minimal multi-package producer and bundled Swagger UI are implemented P1.3 work.
- Other OpenAPI versions, Swagger 2.0, YAML, external references, generic URL ingestion, and UI-specific behavior.
- Knowledge Bundles, search, vectors, RAG, AI enrichment, chat/SSE, TypeScript generation, additional knowledge sources, Agent plugins, and MCP.

## Blockers And Unknowns

- Root Maven loading is repaired; no P0 blocker remains.
- The target application and its current OpenAPI production configuration are absent; the fresh-document integration cannot be verified from this repository.
- Actual compile entry points and final output destination need to be established before claiming end-to-end coverage.
- The target service/module/document mapping and partial-build generation owner/readiness strategy are not yet available or verified.

Distribution visibility, storage, gateway deployment, and a pinned Spring Boot testbed patch are no longer blockers for the current scope.

## Known Gaps

P0 and the first P2 snapshot slice are complete. Root tests pass 26 cases. Broader P2 semantics/reference/limit coverage, P3 publishing and P1/P4 production integration remain open.

## Last Updated

2026-09-09.

## 2026-09-09 - Swagger UI Follow-up Completed

- User-requested Swagger UI is available at `http://127.0.0.1:18080/swagger-ui.html` with account/business selection.
- Red: the new UI HTTP test failed on 404. Green: explicit fixture refresh ran clean test, 5 tests passed, 0 failures/errors; UI assets/configuration and original OpenAPI contracts are verified.
- JSON snapshot digests are unchanged; source metadata and README are refreshed. P0 and production integration status are unchanged.

## 2026-09-09 - P0 Complete

- Root mvn -B test red: 19 tests, 18 assertion failures, 1 placeholder error.
- Green after minimal validation: 19 tests, 0 failures/errors.
- Exact textual 3.1.0 only; distinct MISSING_VERSION, UNSUPPORTED_VERSION and INVALID_JSON. Invalid roots, duplicate keys and trailing JSON are rejected.
- New core POM and corrected parent description; no legacy IR. P2 is authorized next, independently of compilation integration.


## 2026-09-09 - P2 First Snapshot Skill Delivered

- Test-first evidence: 5 new generator tests failed (2 assertions, 3 placeholder errors), with all 19 P0 tests still passing. Implementation then passed all 24. Two additional cases brought the total to 26; the missing catalog entry for unreferenced schemas failed before the fix. Final root `mvn -B test`: 26 passed, 0 failures/errors.
- Uses existing account/business bytes without starting the service. One Skill contains 4 operation files, 7 schema files, 2 document contexts, a catalog, source metadata and trusted SKILL.md (16 files).
- Tests compare complete operation/schema JSON, assert overrides, document/service identity, recursive/shared/multi-level and escaped local pointers, dangling/external reference failures, unsafe identities, case-safe filenames, input/schema-file bounds and link targets. Source text is excluded from SKILL.md; JSON code-fence escaping preserves data.
- Review artifact: `smartdoc-agent-core/target/smartdoc/springdoc-multi-package-api/`; root `mvn test` recreates it through a fixture test. Generated target files are not committed or installed.
- P2 remains in progress: path-item references, dynamic/rebased schema references are explicitly rejected; fuller reusable-example/link semantics, malformed structure coverage and all processing limits need further tests before whole-stage acceptance. These unsupported cases are absent from the two requested snapshots.
- No P3 or P4 started. Source freshness, safe publication, ordinary/repeated compile integration and actual consuming-agent acceptance remain unverified.
- P0 commit e623fa9 was created; its first GitHub push failed on a port 443 connection timeout. Normal push is retried with this milestone.
- Skill frontmatter and entrypoint were manually inspected. The skill-creator quick_validate.py could not run because bundled Python lacks PyYAML; a temporary dependency-install attempt did not complete and was stopped. This does not replace the passing Java content/link tests.
