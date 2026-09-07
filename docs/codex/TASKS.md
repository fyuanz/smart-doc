# Tasks

## Current Phase

Foundation and unified IR contract.

## Completed Tasks

| Task | Acceptance Criteria |
| --- | --- |
| Establish AI project documentation | Root `AGENTS.md`, discoverable repository Skill, and all core `docs/codex/` files exist. |
| Establish initial IR contract | Java IR records and `ir-schema.json` exist in `smartdoc-agent-core`. |
| Verify project initialization baseline | `mvn test` completes successfully for the parent project and `smartdoc-agent-core`; the absence of test sources is documented. |
| Initialize version control | A Git repository exists, the primary branch is `main`, and Maven build output is ignored while project sources remain trackable. |
| Create initial repository snapshot | The initialized project, AI documentation, IR contract, and Git ignore rules are captured in the first commit on `main`. |

## Active Tasks

None confirmed.

## Next Tasks

- Implement the ASM bytecode parser using test-first development.
- Add source parsing as an optional channel.
- Define the complete L3 `smartdoc.json` Schema before implementing its generator.

## Blockers

- No blocker is recorded.

## Known Gaps

- There are no automated test sources yet. The next implementation task must begin with a failing test.

## Last Updated

2026-09-07.
