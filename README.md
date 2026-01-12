# PedroPathingPlus

**PedroPathingPlus** is an advanced pathing library for the FIRST Tech Challenge (FTC), built on top of the [Pedro Pathing](https://github.com/Pedro-Pathing/PedroPathing) library and integrating robust command-based structures.

> [!IMPORTANT]
> **STAY TUNED!**
> This repository is currently undergoing **rapid and constant updates**. Major improvements are planned, including the ability to **run entire autonomous routines directly from \`.pp\` files**.

---

## 🎨 Pedro Pathing Visualizer

Download the Visualizer: https://github.com/Mallen220/PedroPathingVisualizer/releases

---

## 📥 Installation

Most FTC teams will use the Gradle dependency line:

    implementation 'com.github.Mallen220:PedroPathingPlus:<version-or-branch>'

Replace `<version-or-branch>` with a tag (for example `v1.0.6`), a release, or `master-SNAPSHOT` for an up-to-date build.

### Repositories

Add JitPack to your repositories so Gradle or Maven can resolve the artifact.

- Gradle (Project-level `build.gradle` or `settings.gradle`):

```groovy
// Project build.gradle (Groovy)
allprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://mymaven.bylazar.com/releases") }
        maven { url = uri("https://repo.dairy.foundation/releases") }
        google()
        mavenCentral()
    }
}
```

```kotlin
// settings.gradle.kts or build.gradle.kts (Kotlin DSL)
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
        maven("https://mymaven.bylazar.com/releases")
        maven("https://repo.dairy.foundation/releases")
        google()
        mavenCentral()
    }
}
```

- Maven (`pom.xml` repositories):

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
  <repository>
    <id>bylazar</id>
    <url>https://mymaven.bylazar.com/releases</url>
  </repository>
  <repository>
    <id>dairy</id>
    <url>https://repo.dairy.foundation/releases</url>
  </repository>
</repositories>
```

### Gradle Dependencies

- Groovy DSL (Module: `app` `build.gradle`):

```groovy
dependencies {
    // PedroPathingPlus (most FTC teams)
    implementation 'com.github.Mallen220:PedroPathingPlus:<version-or-branch>'

    // Core / related libraries
    implementation 'com.pedropathing:ftc:2.0.0'
    implementation 'org.solverslib:core:0.3.3' // Will be removed in future versions for PedroPathingPlus native usage
    implementation 'org.solverslib:pedroPathing:0.3.3'
}
```

- Kotlin DSL (Module: `app` `build.gradle.kts`):

```kotlin
dependencies {
    implementation("com.github.Mallen220:PedroPathingPlus:<version-or-branch>")
    implementation("com.pedropathing:ftc:2.0.0")
    implementation("org.solverslib:core:0.3.3") // Will be removed in future versions for PedroPathingPlus native usage
    implementation("org.solverslib:pedroPathing:0.3.3")
}
```

### Maven Dependency

```xml
<dependency>
  <groupId>com.github.Mallen220</groupId>
  <artifactId>PedroPathingPlus</artifactId>
  <version>{version-or-branch}</version>
</dependency>
```

---

## ⚠️ Notes

- Use a specific tag or release for reproducible builds (for example `v1.0.6`). JitPack resolves artifacts as `com.github.Username:Repo:Tag` (e.g., `com.github.Mallen220:PedroPathingPlus:v1.0.6`) or branch/commit specifiers.
- If you see resolution issues (for example with `com.pedropathing:ftc:2.0.0`), ensure the required repositories are added and that your Gradle JVM matches the library/tooling requirements.
- For JitPack build output and troubleshooting, visit https://jitpack.io and paste the tag or commit for logs.

---

## 🚀 Upcoming Features

- Direct \`.pp\` execution
- Enhanced command integration
- Improved documentation

**Built by [Mallen220](https://github.com/Mallen220) & Contributors**