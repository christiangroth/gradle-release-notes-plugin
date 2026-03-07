package de.chrgroth.gradle.plugins.releasenotes

import de.chrgroth.gradle.plugins.releasenotes.ProjectVersion.Companion.toProjectVersion
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProjectVersionTest {

  @Test
  fun `parses standard version string`() {
    val version = "1.2.3".toProjectVersion()
    assertThat(version).isNotNull
    assertThat(version!!.major).isEqualTo(1)
    assertThat(version.minor).isEqualTo(2)
    assertThat(version.patch).isEqualTo(3)
    assertThat(version.addition).isEmpty()
    assertThat(version.ticketId).isNull()
  }

  @Test
  fun `parses version with snapshot suffix`() {
    val version = "1.2.3-SNAPSHOT".toProjectVersion()
    assertThat(version).isNotNull
    assertThat(version!!.major).isEqualTo(1)
    assertThat(version.minor).isEqualTo(2)
    assertThat(version.patch).isEqualTo(3)
    assertThat(version.addition).isEqualTo("-SNAPSHOT")
  }

  @Test
  fun `parses version with ticket id`() {
    val version = "2.0.1".toProjectVersion(ticketId = "TICKET-42")
    assertThat(version).isNotNull
    assertThat(version!!.major).isEqualTo(2)
    assertThat(version.ticketId).isEqualTo("TICKET-42")
  }

  @Test
  fun `returns null for invalid version string`() {
    val version = "not-a-version".toProjectVersion()
    assertThat(version).isNull()
  }

  @Test
  fun `toString without ticket id`() {
    val version = ProjectVersion(major = 1, minor = 2, patch = 3, addition = "-SNAPSHOT", ticketId = null)
    assertThat(version.toString()).isEqualTo("1.2.3-SNAPSHOT")
  }

  @Test
  fun `toString with ticket id`() {
    val version = ProjectVersion(major = 1, minor = 2, patch = 3, addition = "", ticketId = "FEAT-7")
    assertThat(version.toString()).isEqualTo("1.2.3-FEAT-7")
  }

  @Test
  fun `compareTo returns zero for equal versions`() {
    val v1 = ProjectVersion(1, 0, 0, "", null)
    val v2 = ProjectVersion(1, 0, 0, "", null)
    assertThat(v1.compareTo(v2)).isEqualTo(0)
  }

  @Test
  fun `compareTo distinguishes major version`() {
    val lower = ProjectVersion(1, 5, 5, "", null)
    val higher = ProjectVersion(2, 0, 0, "", null)
    assertThat(lower.compareTo(higher)).isLessThan(0)
    assertThat(higher.compareTo(lower)).isGreaterThan(0)
  }

  @Test
  fun `compareTo distinguishes minor version`() {
    val lower = ProjectVersion(1, 2, 0, "", null)
    val higher = ProjectVersion(1, 3, 0, "", null)
    assertThat(lower.compareTo(higher)).isLessThan(0)
    assertThat(higher.compareTo(lower)).isGreaterThan(0)
  }

  @Test
  fun `compareTo distinguishes patch version`() {
    val lower = ProjectVersion(1, 0, 0, "", null)
    val higher = ProjectVersion(1, 0, 1, "", null)
    assertThat(lower.compareTo(higher)).isLessThan(0)
    assertThat(higher.compareTo(lower)).isGreaterThan(0)
  }

  @Test
  fun `invoke factory creates same result as toProjectVersion`() {
    val v1 = ProjectVersion("3.4.5", null)
    val v2 = "3.4.5".toProjectVersion()
    assertThat(v1).isEqualTo(v2)
  }
}
