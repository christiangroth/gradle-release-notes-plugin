# arc42 Architecture Documentation

**gradle-release-notes-plugin** — a Gradle plugin for automated release notes management.

---

## 1. Introduction and Goals

The plugin automates the creation, aggregation, and versioning of release notes within a Gradle project. It bridges the gap between individual feature branches (each producing a snippet) and a final, formatted release notes document.

**Primary goals:**

| Goal | Description |
|------|-------------|
| Snippet-based workflow | Developers create small text snippets per branch |
| Automated aggregation | Snippets are merged into a single release notes file |
| Semantic versioning | Version bumps are derived from snippet types automatically |
| Template-driven | All output formats are fully customisable via templates |
| Multi-config support | One project can maintain multiple independent release notes documents |

---

## 2. Constraints

- Requires a **git repository** (uses [grgit](https://github.com/ajoberstar/grgit) to read the current branch)
- Project version is expected to live in `gradle.properties` as `version=X.Y.Z`
- Integrates with the [net.researchgate.release](https://github.com/researchgate/gradle-release) plugin for the release lifecycle
- Minimum Java 17 / Gradle 8+

---

## 3. Context and Scope

```mermaid
graph LR
    Dev["Developer"] -->|"runs releasenotesCreate*"| Plugin["gradle-release-notes-plugin"]
    Plugin -->|"reads/writes snippets"| SnippetDir["snippets/"]
    Plugin -->|"reads templates"| TemplateDir["templates/"]
    Plugin -->|"generates"| OutputFile["RELEASE_NOTES.md"]
    Plugin -->|"reads branch"| Git["Git repository"]
    ReleasePlugin["net.researchgate.release"] -->|"triggers afterReleaseBuild"| Plugin
    Plugin -->|"bumps version in"| GradleProps["gradle.properties"]
```

---

## 4. Solution Strategy

- **File-based pipeline**: snippets are plain text files classified by their filename postfix (`-feature`, `-bugfix`, `-highlight`, `-updateNotice`)
- **Template engine**: simple string replacement (`{version}`, `{date}`, `{features}`, …) — no external template engine dependency
- **Lifecycle hooks**: the plugin wires itself into existing Gradle tasks (`assemble`, `clean`, `unSnapshotVersion`, `afterReleaseBuild`) when they are present

---

## 5. Building Blocks

### 5.1 Top-level decomposition

```mermaid
classDiagram
    class ReleasenotesPlugin {
        +apply(project)
    }
    class ReleasenotesExtension {
        +mainBranch: String
        +skipReleaseNotesOnBranchPrefixes: List
        +configure(provider)
    }
    class ReleasenotesConfiguration {
        +name: String
        +outputPath: String
        +snippetsPath: String
        +templatesPath: String
        +init(projectDir, buildDir) ReleaseNotesProcessor
    }
    class ReleaseNotesProcessor {
        +createFolderStructure()
        +createTemplatesFiles()
        +createBugfix(branch)
        +createFeature(branch)
        +createHighlight(branch)
        +createUpdateNotice(branch)
        +buildReleasenotes(...)
        +copyBuiltReleaseNotesToSources()
        +deleteSnippets()
        +hasFeatureSnippets() Boolean
        +hasUpdateNoticeSnippets() Boolean
    }
    class ProjectVersion {
        +major: Int
        +minor: Int
        +patch: Int
        +addition: String
        +ticketId: String?
        +compareTo(other) Int
        +toString() String
        +invoke(String, String?) ProjectVersion?
    }
    class ReleasenoteSnippetType {
        <<enumeration>>
        FEATURE
        BUGFIX
        HIGHLIGHT
        UPDATENOTICE
    }

    ReleasenotesPlugin --> ReleasenotesExtension : creates
    ReleasenotesPlugin --> ProjectVersion : uses
    ReleasenotesExtension "1" *-- "many" ReleasenotesConfiguration
    ReleasenotesConfiguration --> ReleaseNotesProcessor : creates via init()
    ReleaseNotesProcessor --> ReleasenoteSnippetType : classifies files by
```

---

## 6. Runtime View

### 6.1 Snippet creation (developer workflow)

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant Gradle as Gradle
    participant Plugin as ReleasenotesPlugin
    participant Proc as ReleaseNotesProcessor
    participant FS as File System
    participant Git as Git

    Dev->>Gradle: ./gradlew releasenotesCreateFeature
    Gradle->>Plugin: execute task
    Plugin->>Git: read current branch name
    Git-->>Plugin: "feature/my-cool-thing"
    Plugin->>Proc: createFeature("my-cool-thing")
    Proc->>FS: read feature template (or use default)
    Proc->>FS: write snippets/my-cool-thing-feature.md
    FS-->>Dev: snippet file ready for editing
```

### 6.2 Release notes generation

```mermaid
sequenceDiagram
    participant Gradle as Gradle
    participant Plugin as ReleasenotesPlugin
    participant Proc as ReleaseNotesProcessor
    participant FS as File System

    Gradle->>Plugin: execute releasenotesGenerate
    Plugin->>Proc: buildReleasenotes(branch, version)
    Proc->>FS: list snippet files in snippets/
    Proc->>Proc: renderSnippets(FEATURE, header, footer)
    Proc->>Proc: renderSnippets(BUGFIX, header, footer)
    Proc->>Proc: renderSnippets(HIGHLIGHT, header, footer)
    Proc->>Proc: renderSnippets(UPDATENOTICE, header, footer)
    Proc->>Proc: apply next-version template
    Proc->>FS: write build/releasenotes/<name>/RELEASE_NOTES.md
    Note over Proc,FS: prepends new version block to existing content
```

### 6.3 Release lifecycle integration

```mermaid
sequenceDiagram
    participant Release as net.researchgate.release
    participant Plugin as ReleasenotesPlugin

    Release->>Plugin: unSnapshotVersion
    Plugin->>Plugin: releasenotesVersionBump
    Note over Plugin: major bump if UPDATENOTICE snippets exist
    Note over Plugin: minor bump if FEATURE snippets exist
    Note over Plugin: otherwise patch (handled by release plugin)
    Release->>Release: tag & push
    Release->>Plugin: afterReleaseBuild
    Plugin->>Plugin: releasenotesCopyToSources
    Plugin->>Plugin: releasenotesDeleteSnippets
```

---

## 7. Deployment View

The plugin is published to **GitHub Packages** (Maven registry) and consumed like any other Gradle plugin:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/christiangroth/gradle-release-notes-plugin")
        }
    }
}

// build.gradle.kts
plugins {
    id("de.chrgroth.gradle.release-notes") version "1.0.1"
}
```

---

## 8. Cross-cutting Concepts

### Version parsing

The plugin parses semantic versions using a regex (`([0-9]+)(?:.([0-9]+))?(?:.([0-9]+))?([0-9.a-zA-Z-+_]*)`).  
The `addition` field captures suffixes like `-SNAPSHOT` or `-RC1`.

### Snippet type detection

Files in the snippets folder are classified by their filename postfix:

| Postfix | Type | Version impact |
|---------|------|----------------|
| `-feature` | `FEATURE` | minor bump |
| `-bugfix` | `BUGFIX` | patch (no bump) |
| `-highlight` | `HIGHLIGHT` | patch (no bump) |
| `-updateNotice` | `UPDATENOTICE` | major bump |

### Template variables

| Variable | Replaced with |
|----------|---------------|
| `{version}` | Project version string |
| `{date}` | Current date (configurable format) |
| `{features}` | Rendered feature snippets |
| `{bugfixes}` | Rendered bugfix snippets |
| `{highlights}` | Rendered highlight snippets |
| `{updateNotices}` | Rendered update notice snippets |
| `{gitbranch}` | Last segment of the current branch name |

---

## 9. Architecture Decisions

| Decision | Rationale |
|----------|-----------|
| Plain-text snippets | No tooling required; works with any diff tool and code review |
| File-based classification | Avoids a database or metadata; snippets are self-describing |
| Build-dir staging | Generated output is never committed until explicitly copied back |
| No external template engine | Keeps dependencies minimal; simple `String.replace` is sufficient |
| Version stored in `gradle.properties` | Follows Gradle conventions; compatible with the release plugin |

---

## 10. Quality Requirements

| Quality | Measure |
|---------|---------|
| Correctness | Unit tests for `ProjectVersion` and `ReleaseNotesProcessor` |
| Static analysis | Detekt with `warningsAsErrors = true` |
| Code coverage | Kover enforces ≥ 40 % line coverage |
| Maintainability | Kotlin idioms; explicit null handling; named constants |
