# Project Structure

## Directory Map

```text
.
|-- AGENTS.md
|-- .gitignore
|-- .agents/
|   `-- skills/ai-project-docs/
|       |-- SKILL.md
|       |-- agents/openai.yaml
|       `-- assets/
|-- docs/
|   |-- smartdoc-agent-design.md
|   `-- codex/
|       |-- PROJECT_CONTEXT.md
|       |-- PROJECT_STRUCTURE.md
|       |-- MODULES.md
|       |-- TASKS.md
|       `-- DECISIONS.md
|-- smartdoc-agent-core/
|   |-- pom.xml
|   `-- src/main/
|       |-- java/com/smartdoc/agent/core/ir/
|       `-- resources/schema/ir-schema.json
|-- project/
`-- pom.xml
```

## Important Files

| Path | Purpose |
| --- | --- |
| `AGENTS.md` | Always-on repository instructions for AI development. |
| `.gitignore` | Excludes Maven output, IDE metadata, local environment files, logs, temporary files, and OS metadata. |
| `.agents/skills/ai-project-docs/SKILL.md` | Discoverable workflow for creating and maintaining the AI documentation pack. |
| `docs/smartdoc-agent-design.md` | Product requirements and architecture source of truth. |
| `pom.xml` | Java 17 Maven parent project and module list. |
| `smartdoc-agent-core/pom.xml` | Core module build configuration. |
| `smartdoc-agent-core/src/main/resources/schema/ir-schema.json` | IR JSON Schema contract. |

## Generated Or Ignored Directories

| Path | Notes |
| --- | --- |
| `**/target/` | Maven build output; do not treat it as source. |
| `.idea/`, `.vscode/`, `.settings/` | Local IDE configuration. |
| `.env`, `.env.*` | Local environment and secret files; `.env.example` remains trackable. |
| `*.log`, `*.tmp` | Local logs and temporary files. |

## Caution Areas

- Preserve the IR contract and its JSON Schema together.
- Treat planned modules in the design document as plans until their directories and Maven modules actually exist.
- Do not infer requirements that are absent from the design document or current code.

## Where To Add New Work

- Current IR contract work belongs under `smartdoc-agent-core/src/main/java/com/smartdoc/agent/core/ir/` and its schema resource.
- Future module paths should follow `docs/smartdoc-agent-design.md` and be added to the parent `pom.xml` when implemented.
