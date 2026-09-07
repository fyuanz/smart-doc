# Decisions

## 2026-09-07 - Initialize Git with a main primary branch

Status: Accepted

Context:

- The project had no Git repository or repository-level ignore rules.

Decision:

- Initialize the repository with `main` as the primary branch.
- Ignore Maven build output, Java compiler artifacts, IDE metadata, local environment files, logs, temporary files, and operating-system metadata.
- Do not ignore JAR files globally because JAR inputs and fixtures are part of the SmartDoc-Agent domain.

Consequences:

- Source, documentation, schemas, and potential JAR fixtures remain trackable.
- Generated Maven `target/` directories and machine-local files stay out of commits.

Alternatives considered:

- Ignore all `*.jar` files. Rejected because that could hide intentional test fixtures or sample inputs.

## 2026-09-07 - Separate mandatory project guidance from the documentation Skill

Status: Accepted

Context:

- A `SKILL.md` stored directly under `docs/` is not in Codex's repository Skill discovery path.
- Implicit Skill activation depends on task matching and should not be the only mechanism for mandatory instructions.

Decision:

- Store the reusable documentation workflow at `.agents/skills/ai-project-docs/`.
- Store always-on first-read and development rules in the repository-root `AGENTS.md`.
- Use the Skill to create or refresh documentation, while ordinary development follows `AGENTS.md` and updates only documents whose facts changed.

Consequences:

- Codex can discover the Skill from the repository root and subdirectories.
- Every task receives the first-read instructions without forcing the complete documentation workflow to run.
- Templates and UI metadata remain self-contained inside the Skill directory.

Alternatives considered:

- Keep `docs/SKILL.md` and rely on agents to search for it. Rejected because the location is not automatically discoverable as a repository Skill.
- Force the complete Skill on every task. Rejected because it would add unnecessary documentation gates to routine code changes.

## 2026-09-07 - Preserve the confirmed SmartDoc-Agent architecture

Status: Accepted

Context:

- `docs/smartdoc-agent-design.md` records the reviewed product and architecture direction.

Decision:

- Use Java 17 and Maven modules.
- Use bytecode and optional source inputs normalized into deterministic IR.
- Use build-time knowledge generation with local runtime retrieval and a probe fallback.

Consequences:

- New implementation work must preserve determinism in the IR and distinguish implemented modules from planned modules.

Alternatives considered:

- Refer to `docs/smartdoc-agent-design.md` for the detailed ADR alternatives and rationale.
