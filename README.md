# gradle-release-notes-plugin

A Gradle plugin that automates snippet-based release notes management with semantic version bumping.

## Overview

Instead of maintaining a monolithic changelog by hand, each developer creates a small **snippet file** on their branch. When a release is cut, the plugin aggregates all snippets into a formatted release notes document and bumps the project version automatically.

```
snippets/
  TICKET-42-feature.md      ← new feature
  TICKET-99-bugfix.md       ← bug fix
  TICKET-7-updateNotice.md  ← breaking change → major bump
```

## Getting Started

### 1. Apply the plugin

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/christiangroth/gradle-release-notes-plugin")
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(System.getenv("GITHUB_ACTOR")).get()
                password = providers.gradleProperty("gpr.key").orElse(System.getenv("GITHUB_TOKEN")).get()
            }
        }
    }
}

// build.gradle.kts
plugins {
    id("de.chrgroth.gradle.release-notes") version "<version>"
}
```

### 2. Configure

```kotlin
releasenotes {
    mainBranch = "main"
    skipReleaseNotesOnBranchPrefixes = listOf("dependabot/", "chore/")

    configure {
        ReleasenotesConfiguration(
            name             = "default",
            outputPath       = "RELEASE_NOTES.md",
            snippetsPath     = "releasenotes/snippets",
            templatesPath    = "releasenotes/templates",
            bugfixesHeader   = "### Bug Fixes",
            bugfixesFooter   = "",
            featuresHeader   = "### Features",
            featuresFooter   = "",
            highlightsHeader = "### Highlights",
            highlightsFooter = "",
            updateNoticesHeader = "### ⚠️ Breaking Changes",
            updateNoticesFooter = "",
            dateFormat       = "yyyy-MM-dd",
            preserveWhitespace = false,
        )
    }
}
```

### 3. Initialise the folder structure

```bash
./gradlew releasenotesInit
```

### 4. Create snippet files while developing

```bash
# on branch feature/TICKET-42-new-search
./gradlew releasenotesCreateFeature     # → releasenotes/snippets/TICKET-42-new-search-feature.md

# on branch bugfix/TICKET-99-null-pointer
./gradlew releasenotesCreateBugfix      # → releasenotes/snippets/TICKET-99-null-pointer-bugfix.md

# breaking change
./gradlew releasenotesCreateUpdateNotice

# editorial highlight
./gradlew releasenotesCreateHighlight
```

Edit the generated snippet file to describe your change.

### 5. Generate release notes

```bash
./gradlew releasenotesGenerate
```

The output is staged in `build/releasenotes/<name>/` and prepended to the existing release notes file.

### 6. Copy back and clean up (automated via release plugin)

```bash
./gradlew releasenotesCopyToSources
./gradlew releasenotesDeleteSnippets
```

Both tasks are wired to `afterReleaseBuild` automatically when the **net.researchgate.release** plugin is present.

---

## Available Tasks

| Task | Description |
|------|-------------|
| `releasenotesInit` | Create folder structure and output file |
| `releasenotesCreateTemplates` | Materialise default templates for customisation |
| `releasenotesCreateFeature` | Create a feature snippet (minor version bump) |
| `releasenotesCreateBugfix` | Create a bugfix snippet (patch bump) |
| `releasenotesCreateHighlight` | Create an editorial highlight snippet |
| `releasenotesCreateUpdateNotice` | Create a breaking-change snippet (major bump) |
| `releasenotesVersionBump` | Bump version in `gradle.properties` based on snippet types |
| `releasenotesGenerate` | Aggregate snippets into a release notes document |
| `releasenotesCopyToSources` | Copy generated document back to the source tree |
| `releasenotesDeleteSnippets` | Remove all snippet files after release |
| `releasenotesCleanup` | Remove build-time generated files |

---

## Version Bumping

The plugin examines the snippet types and bumps the project version accordingly **before** the release plugin strips the `-SNAPSHOT` suffix:

| Snippet type present | Version change |
|----------------------|----------------|
| `UPDATENOTICE` | **major** (`1.2.3 → 2.0.0`) |
| `FEATURE` (no update notice) | **minor** (`1.2.3 → 1.3.0`) |
| Only bugfixes / highlights | **patch** — handled by the release plugin |

---

## Template Variables

Snippet template files support the `{gitbranch}` placeholder (replaced with the last segment of the current branch name).

The `next-version` template additionally supports:

| Variable | Description |
|----------|-------------|
| `{version}` | The release version string |
| `{date}` | Current date (format configurable via `dateFormat`) |
| `{features}` | Rendered feature section |
| `{bugfixes}` | Rendered bugfix section |
| `{highlights}` | Rendered highlights section |
| `{updateNotices}` | Rendered update notices section |

---

## Architecture

See [docs/arc42.md](docs/arc42.md) for a full arc42 architecture description including sequence diagrams.

---

## Development

```bash
# Build (includes tests, detekt, kover coverage check)
./gradlew build

# Tests only
./gradlew test

# Static analysis
./gradlew detekt

# Coverage report
./gradlew koverHtmlReport
```

---

## License

[MIT](LICENSE)
