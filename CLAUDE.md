# CLAUDE.md for gradle-release-notes-plugin

## Build & Test Commands

```bash
# Run full build (includes tests and static analysis)
./gradlew build

# Run tests only
./gradlew test

# Static analysis only
./gradlew detekt

# Coverage report
./gradlew koverHtmlReport
```

## Green Build Requirement

`gradle.yml` runs the full build on **every push to every branch** (there is no `branches: [main]` filter), so every commit is already validated by CI automatically. Follow these rules regardless:

- **Run `./gradlew build` before pushing** to catch failures locally instead of waiting on CI.
- **Never use `[no ci]`** to bypass a failing build — fix the underlying issue instead.
- The `release` job only runs on `main` and requires the preceding `build` job to succeed.
- **Exception for time-limited agent sessions:** see "Running Gradle in Time-Limited Agent Sessions" below.

## Running Gradle in Time-Limited Agent Sessions

This applies whenever a single command execution is time-limited (e.g. the Claude Code GitHub Action, whose Bash tool has a hard execution ceiling of a few minutes per call). A full `./gradlew build` (tests, static analysis, coverage) can exceed that ceiling and gets killed at an inconsistent point from run to run — do not rely on it in this context.

- **Never background a Gradle invocation** (no `run_in_background` / async execution). A build that keeps running after its tool call is considered "timed out" produces results that cannot be trusted or awaited.
- **Scope the check to what you touched** for a fast, reliable local signal, e.g. `./gradlew test` or `./gradlew detekt` instead of the whole `build`.
- **Push the branch and open the PR**, then rely on the `gradle.yml` workflow (triggered automatically on every push) as the authoritative full-build gate. Poll with `gh pr checks` (short, non-blocking calls) to confirm it goes green rather than reproducing the whole build locally.

## Task Completion

A task is only complete once:
1. The relevant tests/checks for what you touched are green (see above), and CI on the pushed branch is green or at least running.
2. Changes are committed and pushed.
3. A pull request is open via `gh pr create` — check first with `gh pr list --head <branch>` whether one already exists for the branch; if so, use `gh pr edit` instead of opening a second one.

Never end a task with "PR still to be opened" as an open item, and never end silently after a partial change. If a step fails (e.g. `git push` or `gh pr create`), retry (e.g. with a different branch name); if it still fails, state the failure and the reason explicitly in a comment. If, after investigation, no code change turns out to be needed, say so explicitly in a comment instead of ending without any response.

## Incremental Commits

Don't wait until the very end and bundle everything into one commit: commit and push after each self-contained step. As soon as the first meaningful commit is pushed, open a draft pull request (`gh pr create --draft`) and keep updating its description as work progresses; only take it out of draft once everything is done. This way, even an interrupted run leaves a visible trace instead of ending without one.

## Formatting

All code must follow the rules in `.editorconfig`. The most important ones for this repo:

- **2-space indentation**, no tabs
- **CRLF line endings**
- **Max line length:** 180 characters
- **Insert final newline** in every file

Format new and edited files accordingly before committing.

## Documentation

- **Architecture:** [docs/arc42.md](docs/arc42.md)
