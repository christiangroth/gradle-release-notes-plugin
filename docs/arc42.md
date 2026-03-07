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

![Context diagram](https://kroki.io/plantuml/svg/eNplks1OwzAQhO9-ihVHpKR3DqhAAxdEq6Z35CZb18KxrfWmqG_P5qdtUk6xN9-MPKNdJtbEbeOUwwMDByBrjgy1JazYBq_Sj_VRk26gCk0MHj2XfHYIHaC9caiUrjgQrPCELkQkdSXhwZCuHWaEDnXCzAfGlEXXGusfQCfY9MepInkbI3Ja9P_L4bayM1fGJjotVgO0G6931Lb4LF7K4vtrvSvKvKl7dt1ybPndyrsn6IdlCRRDspLk3IMy-h8kj9QlZItpgPrpRoZpCnvknDChpupo5GH5mL_XbIfzmFxda4Msex77gCeg1icYZX1rb4Ti9KhGooNv5XQC1HVa_JIVFi4lTulJSxccrkVOwVtFwhn0SPdEV9fFYk_aV0c1SzWPwrJTBimBPjDSCL621tUzz1uVotm3jXxPopIlBOlpib6WPf0D7zv1Kw==)

---

## 4. Solution Strategy

- **File-based pipeline**: snippets are plain text files classified by their filename postfix (`-feature`, `-bugfix`, `-highlight`, `-updateNotice`)
- **Template engine**: simple string replacement (`{version}`, `{date}`, `{features}`, …) — no external template engine dependency
- **Lifecycle hooks**: the plugin wires itself into existing Gradle tasks (`assemble`, `clean`, `unSnapshotVersion`, `afterReleaseBuild`) when they are present

---

## 5. Building Blocks

### 5.1 Top-level decomposition

![Class diagram](https://kroki.io/plantuml/svg/eNp1VMFu4kAMvecrRpxgtyDtNYdtSwsFqWqjEqq9DokBl2RmNOOgotX--3pIUoYUDokm9rP9nsfOnSNpqSqLKCukc-INCpAOlCZwSVFtUIm_kRA_pTHFoW-s_oCMBtG_C_DJJ4FyqJuIUqIaW6mybSwWZFFtvNXt0DRBLz7otcEkFtb4CS4Wz-jIIzOt1ripLPiqe8zBXi770OAkfZVWsoSwqK7IVJRIOqei0Bgg17UTlKaQXn_HgQqpbcEj2huxqrDI-TSIRagpsToD57Tt8j331lwzC1xrqgsWyKWqjLzkwcmVtnSmWIALPeNqwz3rr44NDOxTfvkk3xwz3GwLfui7a2lyfjNBzM4CjxLDdvdHo1Edp81hzF4KxaV6oSubtTxzdhEsmkbXtq10DcGTPRZjrTmLagAhm0uotq9JfRnvYMO5-9A2FnN1nKISVfBlJPlxbL5knqMfmrPLx2wHNM9b220ttTTSQqr7mrbgr7vJQLpGeW7hnOz1Dvq14aZNxJBzurcsA1RVhtPciE0PBo5qppP7dPk24dN4-TSd_-HDbP40e-Yn5fMyebxPJy-v6fxhwtmiC9s7HP6-sqWxqK_eXQvrdDcWleuAT8l6v3rix3AoeqVUh971BY2ur27AtLMoX0zFHqU4ruEgugztyA37yVn80OAaOdHaL5NYHaI7UDn__v4DeHm9vg==)

---

## 6. Runtime View

### 6.1 Snippet creation (developer workflow)

![Snippet creation sequence](https://kroki.io/plantuml/svg/eNptkkFuwyAQRfecYuRVunCy96KK1MrdRVV9AorHCSoGBENT376DQ1pTdWPD8P6fz9jHSDJQmo3w_NZKe2kJnvETjfMYqupLkKPBqvSGBmVE6wjjq0lnbUFGuK3-A08rGJzCGF1YWd5UZNNrgzAskXBuMtEPdQpNQvwEhPax5IIO9ofzurxC2OR6CigJe36kgKLALCt5O8AvVIkQSMYPUarZVhMfsngElUJA7v0epFUXsHJmIz5utzbNdGtxmJdWOWdaumh7bjaO-a4Mqm2gXVPjD2KlmO6He_tiDDwRb1gKO55diggjTjIZ-qO5Bs1MtNp7pFjHaYvXfh4Fw_kCv7Ps7iKY8jfIvReYuBeOmlgsjmhH_le-ATY8xc0=)

### 6.2 Release notes generation

![Release notes generation sequence](https://kroki.io/plantuml/svg/eNqVktFrwjAQxt_zVxw-KdT1fQzRbWkVxImtsDeJ7alhaRKSdOp_v7N2o8JAfChtLr_77uO-jn0QLtSVYpbespBW6ACpE6XCm9IKFQqP2gT0S1XvpQbh4fr1H7hoQGcK9N64hqXDDdlLpELIzj5g1bsQScbYdTQMR602PAOesKgDgutYSFGjEwFZS1140id6W0tVdt32t07o4hDBNzovjR6whqSOJCNeSR_Aa2ktBtiRIw-k1xZ8_Ae38g51iS5rr_sJn-TrFY_ggILqEewMzXSDO22v6zSZfT7aNZ2l0zk9-aON6-X7JOeLj3z2dt-qsFadQeMpDNuNAQVkVbPt7uaOTlIqzb7jbjbxixYVjuIVn_NJxjc0l2dPVckut2BItBkVXVWsQ0tuPU08_kYEW2WKLwiGsqd4pN5DYXRAHdiYWPpdfwBeq_C_)

### 6.3 Release lifecycle integration

![Release lifecycle sequence](https://kroki.io/plantuml/svg/eNqNUstOwzAQvPsrVj0gOMAH9ID6ChIXqJqU-zbZxkaObXnXQP8el7oiSJXKyeN5rLwjz1gwShqsCvk0rQnoBCaO5CESE8ZW9yiUL5aQaQLIsDnhP4nCOS_Ea5t6447OE1KqqHD_WCiYQnK1w8DayxtFNt6pIo1NcTS22BZpCOpIgP-g-Gsd8N1H2GUVzB6269W8qV5em-dlBexMCCQM9GVYLoaNG4Wfqnmz3fwn50VT_DR5tYDSarjV6DpLHewO57dD-DHfjUs4wykI9nADIbG-WBLuhWIRFsnY7lpJSx8Oja99ii3xNfMqY6G6bKlm5Lr8E74Buxe9Jg==)

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
