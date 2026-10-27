# Repository agents and skills

They automate the reviews and the repetitive work that CI does not already
cover. Each *skill* is a workflow (`SKILL.md` + scripts); each *agent* is a
profile that runs a workflow with a restricted set of tools.

## What stayed

| Agent (`.github/agents/`) | Skill (`.github/skills/`) | When to use |
|---|---|---|
| `pre-push-reviewer` | `pre-push-review` | Review the local diff before a push: serious pattern and behavior regressions, nulls, broken API contracts, security, performance, and CI checks. |
| `change-reviewer` | `change-review` | Review a pull request and post inline comments (github.com or Enterprise). The CI job `change-review` generates the diff and the changed-line map. |
| `unit-test-writer` | `unit-test-generation` | Write JUnit 4 tests and hand-written fakes in the style already used in the project. |
| — | `debug` | Diagnose an Android failure from the terminal (device, logcat, instrumented results) when the IDE debugger is not available. |

Detekt, Kover and the test suites run in `.github/workflows/ci.yml`; local
validation is performed only by the `pre-push-review` workflow before a push.
The reviewer checks serious security and performance regressions directly;
it does not report cosmetic style issues. Narrow scans (accessibility,
double tap, routes, hardcoded strings, security, concurrency, merge
conflicts) were removed as separate scans: the app has three screens and
those rules already live in `copilot-instructions.md` and in the review skill.

## How to use
- **Agent:** in Copilot Chat, pick the agent and describe the request.
- **Skill:** invoke it by name, for example `/pre-push-review`,
  `/change-review <PR>`, `/unit-test-generation <file>` or `/debug`.
- Scripts can run directly, for example
  `.github/skills/debug/scripts/diagnose_android.sh`.

## Common rules
- Agents show their findings before editing and wait for confirmation.
- No workflow runs `git push` or creates a commit on its own.
- Temporary files live in `.review-tmp/` (gitignored). Delete the directory
  when the workflow finishes.
- Reviews posted on a pull request use `GITHUB_TOKEN` or the git credential.
  Nothing is written to disk.
