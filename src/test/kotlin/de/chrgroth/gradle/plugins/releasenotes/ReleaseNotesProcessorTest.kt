package de.chrgroth.gradle.plugins.releasenotes

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ReleaseNotesProcessorTest {

  @TempDir
  lateinit var tempDir: File

  private lateinit var outputFile: File
  private lateinit var snippetsFolder: File
  private lateinit var templatesFolder: File
  private lateinit var buildDir: File
  private lateinit var processor: ReleaseNotesProcessor

  @BeforeEach
  fun setUp() {
    outputFile = tempDir.resolve("RELEASE_NOTES.md")
    snippetsFolder = tempDir.resolve("snippets")
    templatesFolder = tempDir.resolve("templates")
    buildDir = tempDir.resolve("build")

    processor = ReleaseNotesProcessor(
      name = "test",
      outputFile = outputFile,
      snippetsFolder = snippetsFolder,
      templatesFolder = templatesFolder,
      bugfixesHeader = "### Bugfixes",
      bugfixesFooter = "",
      featuresHeader = "### Features",
      featuresFooter = "",
      highlightsHeader = "### Highlights",
      highlightsFooter = "",
      updateNoticesHeader = "### Update Notices",
      updateNoticesFooter = "",
      dateFormat = "yyyy-MM-dd",
      preserveWhitespace = false,
      buildDir = buildDir,
    )
  }

  @Test
  fun `createFolderStructure creates output file and snippets folder`() {
    processor.createFolderStructure()

    assertThat(outputFile).exists()
    assertThat(snippetsFolder).isDirectory()
  }

  @Test
  fun `createTemplatesFiles creates all template files with default content`() {
    templatesFolder.mkdirs()

    processor.createTemplatesFiles()

    assertThat(templatesFolder.resolve("bugfix.md")).exists()
    assertThat(templatesFolder.resolve("feature.md")).exists()
    assertThat(templatesFolder.resolve("highlight.md")).exists()
    assertThat(templatesFolder.resolve("update-notice.md")).exists()
    assertThat(templatesFolder.resolve("next-version.md")).exists()
  }

  @Test
  fun `createBugfix creates snippet file with branch name substituted`() {
    snippetsFolder.mkdirs()

    processor.createBugfix("feature/my-fix")

    val snippetFile = snippetsFolder.resolve("my-fix-bugfix.md")
    assertThat(snippetFile).exists()
    assertThat(snippetFile.readText()).contains("my-fix")
  }

  @Test
  fun `createFeature creates snippet file with branch name substituted`() {
    snippetsFolder.mkdirs()

    processor.createFeature("feature/cool-thing")

    val snippetFile = snippetsFolder.resolve("cool-thing-feature.md")
    assertThat(snippetFile).exists()
    assertThat(snippetFile.readText()).contains("cool-thing")
  }

  @Test
  fun `createHighlight creates snippet file`() {
    snippetsFolder.mkdirs()

    processor.createHighlight("feature/headline")

    val snippetFile = snippetsFolder.resolve("headline-highlight.md")
    assertThat(snippetFile).exists()
  }

  @Test
  fun `createUpdateNotice creates snippet file`() {
    snippetsFolder.mkdirs()

    processor.createUpdateNotice("feature/breaking")

    val snippetFile = snippetsFolder.resolve("breaking-updateNotice.md")
    assertThat(snippetFile).exists()
  }

  @Test
  fun `hasFeatureSnippets returns false when no snippets exist`() {
    snippetsFolder.mkdirs()

    assertThat(processor.hasFeatureSnippets()).isFalse()
  }

  @Test
  fun `hasFeatureSnippets returns true after creating a feature snippet`() {
    snippetsFolder.mkdirs()
    processor.createFeature("feature/new-thing")

    assertThat(processor.hasFeatureSnippets()).isTrue()
  }

  @Test
  fun `hasUpdateNoticeSnippets returns false when no snippets exist`() {
    snippetsFolder.mkdirs()

    assertThat(processor.hasUpdateNoticeSnippets()).isFalse()
  }

  @Test
  fun `hasUpdateNoticeSnippets returns true after creating an update notice snippet`() {
    snippetsFolder.mkdirs()
    processor.createUpdateNotice("feature/breaking-change")

    assertThat(processor.hasUpdateNoticeSnippets()).isTrue()
  }

  @Test
  fun `deleteSnippets removes snippets folder`() {
    snippetsFolder.mkdirs()
    processor.createFeature("feature/something")
    assertThat(snippetsFolder).exists()

    processor.deleteSnippets()

    assertThat(snippetsFolder).doesNotExist()
  }

  @Test
  fun `buildReleasenotes generates file with version and snippets`() {
    processor.createFolderStructure()
    processor.createFeature("feature/some-feature")

    processor.buildReleasenotes(
      skipReleaseNotesOnBranchPrefixes = emptyList(),
      branchName = "feature/some-feature",
      versionReplacement = "1.0.0",
    )

    val targetFile = buildDir.resolve("releasenotes/test/RELEASE_NOTES.md")
    assertThat(targetFile).exists()
    val content = targetFile.readText()
    assertThat(content).contains("1.0.0")
    assertThat(content).contains("### Features")
  }

  @Test
  fun `buildReleasenotes skips when no snippets and branch matches skip prefix`() {
    processor.createFolderStructure()

    processor.buildReleasenotes(
      skipReleaseNotesOnBranchPrefixes = listOf("dependabot/"),
      branchName = "dependabot/gradle-update",
      versionReplacement = "1.0.0",
    )

    val targetFile = buildDir.resolve("releasenotes/test/RELEASE_NOTES.md")
    assertThat(targetFile).doesNotExist()
  }

  @Test
  fun `buildReleasenotes throws when no snippets and branch not in skip list`() {
    processor.createFolderStructure()

    org.junit.jupiter.api.assertThrows<IllegalStateException> {
      processor.buildReleasenotes(
        skipReleaseNotesOnBranchPrefixes = emptyList(),
        branchName = "feature/no-snippets",
        versionReplacement = "1.0.0",
      )
    }
  }

  @Test
  fun `cleanupGeneratedFiles removes build output`() {
    processor.createFolderStructure()
    processor.createFeature("feature/something")
    processor.buildReleasenotes(
      skipReleaseNotesOnBranchPrefixes = emptyList(),
      branchName = "feature/something",
      versionReplacement = "1.0.0",
    )

    val targetFolder = buildDir.resolve("releasenotes/test")
    assertThat(targetFolder).exists()

    processor.cleanupGeneratedFiles()

    assertThat(targetFolder).doesNotExist()
  }

  @Test
  fun `copyBuiltReleaseNotesToSources copies generated file back to output`() {
    processor.createFolderStructure()
    processor.createFeature("feature/a-feature")
    processor.buildReleasenotes(
      skipReleaseNotesOnBranchPrefixes = emptyList(),
      branchName = "feature/a-feature",
      versionReplacement = "2.0.0",
    )

    processor.copyBuiltReleaseNotesToSources()

    assertThat(outputFile).exists()
    assertThat(outputFile.readText()).contains("2.0.0")
  }
}
