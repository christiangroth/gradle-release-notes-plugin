plugins {
  `java-gradle-plugin`
  `maven-publish`
  `kotlin-dsl`

  alias(libs.plugins.buildTimeTracker)
  alias(libs.plugins.versionCatalogUpdate)

  alias(libs.plugins.release)
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.grgit)
}

gradlePlugin {
  plugins {
    create("releaseNotes") {
      id = "de.chrgroth.gradle.release-notes"
      implementationClass = "de.chrgroth.gradle.plugins.releasenotes.ReleasenotesPlugin"
    }
  }
}

release {
  git {
    requireBranch = "main"
  }
}

// publish wird nach dem Tag-Push automatisch aufgerufen
tasks {
  afterReleaseBuild {
    dependsOn(publish)
  }
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/christiangroth/gradle-release-notes-plugin")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
}
