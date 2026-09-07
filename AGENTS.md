# Project Instructions

## Mandatory First Read

Before analyzing, planning, or modifying code, read these files when they exist:

1. `docs/codex/PROJECT_CONTEXT.md`
2. `docs/codex/PROJECT_STRUCTURE.md`
3. `docs/codex/MODULES.md`
4. `docs/codex/TASKS.md`
5. `docs/codex/DECISIONS.md`

Do not scan the whole repository before reading these files. Use targeted `rg` searches afterward.

## Documentation Skill

The repository documentation skill is located at `.agents/skills/ai-project-docs/SKILL.md`.

Use `ai-project-docs` when the documentation pack is missing or stale, or when work changes project structure, module boundaries, commands, task status, or technical decisions. Ordinary code changes do not require running the full skill when the documentation remains accurate.

## Development Rules

- Write tests before implementation and keep the red to green sequence.
- Keep source and test files below approximately 800-1000 lines; split files by responsibility before they become larger.
- Prefer decisions that are verifiable and maintainable.

## Documentation Updates

When a change affects documented facts, update the relevant `docs/codex/` files in the same task:

- Always update `TASKS.md` when task status changes.
- Update `DECISIONS.md` when a technical decision is proposed, accepted, rejected, or superseded.
- Update `MODULES.md` or `PROJECT_STRUCTURE.md` when responsibilities or paths change.
- Update `PROJECT_CONTEXT.md` when goals, users, workflows, commands, status, or constraints change.

## Verification

Run checks appropriate to the change and report them. If verification cannot run, explain why.
