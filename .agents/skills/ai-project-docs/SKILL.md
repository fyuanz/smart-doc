---
name: ai-project-docs
description: Create, repair, or refresh the repository's AI-readable project documentation pack. Use for project onboarding, missing docs/codex files, documentation drift, or changes to project structure, module boundaries, commands, tasks, and technical decisions. Do not use for ordinary code changes when the existing documentation remains accurate.
---

# AI Project Docs

Use this skill to establish or repair the small project documentation pack that future AI development work reads before changing code.

Repository-wide, always-on behavior belongs in `AGENTS.md`. This skill handles creation and maintenance of the documentation pack; it is not a mandatory gate for every coding task.

## Target Files

Create or maintain exactly these core files for small projects:

- `AGENTS.md`
- `docs/codex/PROJECT_CONTEXT.md`
- `docs/codex/PROJECT_STRUCTURE.md`
- `docs/codex/MODULES.md`
- `docs/codex/TASKS.md`
- `docs/codex/DECISIONS.md`

Use the templates in `assets/` as the starting structure when creating new files:

- `assets/AGENTS.template.md`
- `assets/PROJECT_CONTEXT.template.md`
- `assets/PROJECT_STRUCTURE.template.md`
- `assets/MODULES.template.md`
- `assets/TASKS.template.md`
- `assets/DECISIONS.template.md`

## Workflow

1. Classify the project as new or existing and identify which target documents need work.
2. For an existing project, read only the nearest `AGENTS.md`, `README*`, package/build config, and existing `docs/codex/` files before broader search.
3. Do not scan the whole repository by default. Use targeted `rg` searches only when needed to identify structure, modules, commands, or technology.
4. For a new or empty project, ask concise questions for missing facts instead of inventing requirements.
5. Create or update only the target files affected by the request, using confirmed facts.
6. Mark uncertain information as `Unknown` or `Needs confirmation`.
7. Verify that all links and referenced paths exist, then summarize the updated documents.

## Existing Project Mode

Use this mode when the workspace already contains source code, build files, or an application structure.

- Start from project instructions and obvious entry points, not a full repository scan.
- Prefer `rg --files -g 'AGENTS.md' -g 'README*' -g 'package.json' -g 'pyproject.toml' -g 'go.mod' -g 'Cargo.toml' -g 'pom.xml' -g 'build.gradle*' -g 'docs/codex/**'`.
- Inspect top-level directory names only after instruction and config files are read.
- Use targeted searches for module names, route names, framework config, or commands.
- Record only facts supported by files read in this turn.
- If the architecture is unclear, write the uncertainty into the docs and ask the user for confirmation.

## New Project Mode

Use this mode when the workspace is empty, newly created, or lacks meaningful source files.

- Do not create application code as part of a documentation-only request.
- Ask for missing requirements before finalizing docs: project goal, target users, core features, preferred stack, runtime, storage, and deployment expectations.
- If the user wants defaults, choose conservative mainstream defaults and mark them as proposed decisions in `DECISIONS.md`.
- Create the documentation pack before beginning implementation unless the user explicitly requests both in one task.

## Document Standards

### AGENTS.md

Define how future Codex sessions should work in this repo:

- first-read files
- no full-repo scanning rule
- planning gate before code edits
- documentation update rule
- verification expectations

### PROJECT_CONTEXT.md

Capture the high-level project context:

- project purpose
- target users
- core workflows
- current status
- key commands
- known constraints

### PROJECT_STRUCTURE.md

Capture the directory map:

- top-level directories
- important files
- generated or ignored directories
- directories requiring caution
- intended place for future features

### MODULES.md

Capture module boundaries:

- module name
- responsibility
- main paths
- dependencies
- public contracts
- notes for future AI work

### TASKS.md

Capture active work:

- current phase
- active tasks
- next tasks
- acceptance criteria
- blockers

### DECISIONS.md

Capture technical decisions as short decision records:

```md
## YYYY-MM-DD - Decision title

Status: Proposed | Accepted | Rejected

Context:
Decision:
Consequences:
Alternatives considered:
```

## Update Rule

Whenever code changes affect project structure, module responsibility, commands, task status, or technical decisions, update the relevant files in the same task.

At the end of a task that changes documented facts, update:

- `docs/codex/TASKS.md`
- `docs/codex/DECISIONS.md` if a technical decision changed
- `docs/codex/MODULES.md` or `docs/codex/PROJECT_STRUCTURE.md` if boundaries or paths changed
