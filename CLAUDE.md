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

## Formatting

All code must follow the rules in `.editorconfig`. The most important ones for this repo:

- **2-space indentation**, no tabs
- **CRLF line endings**
- **Max line length:** 180 characters
- **Insert final newline** in every file

Format new and edited files accordingly before committing.

## Documentation

- **Architecture:** [docs/arc42.md](docs/arc42.md)
