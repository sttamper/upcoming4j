# Upcoming4j Gradle Plugin

`Upcoming4j` is a lightweight Gradle plugin that computes the next semantic version of a project based on its Git commit history,
following the Conventional Commits specification. The plugin analyzes commits since the last release,
and determines whether the next version should be a major, minor, or patch increment, 
without requiring manual version updates or complex branching rules.

`Upcoming4j` is **_read-only by design_**: it does not create tags, publish artifacts, or push releases.
Instead, it focuses on doing one thing well: _**computing the next version number**_,
so it can be safely combined with release tools like JReleaser, CI pipelines, or custom workflows.


Tested with Gradle 8.14.3, `Upcoming4j` is [configuration cache](https://docs.gradle.org/8.14.3/userguide/configuration_cache.html) friendly, 
making it fully compatible with future versions, since this is the [preferred execution mode](https://blog.gradle.org/road-to-configuration-cache?_gl=1*1xwmelg*_gcl_au*MTk4NDg5NjQyMi4xNzY4OTE4OTQxLjU3MDM5NDguMTc2OTA0MjI4Ny4xNzY5MDQ1ODI2*_ga*Njg4NTY4MDQxLjE3Njg5MTg5NDE.*_ga_7W7NC6YNPT*czE3NjkyNjA5OTEkbzEzJGcxJHQxNzY5MjYxODM1JGo1OSRsMCRoMA..#preferred-mode-of-execution)
from Gradle 9 onwards.

### Prerequisites

`Upcoming4j` relies on Git metadata to compute the next project version. 
Before using the plugin, make sure the following:

- [**git**](https://git-scm.com/) is installed and available on your system.
- The project is initialized as a Git repository.
- Git tags follow Semantic Versioning using the format `vX.Y.Z` (for example, v1.2.3).
- Commit messages follow the [**Conventional Commits**](https://www.conventionalcommits.org/en/v1.0.0/) spec.

### Features

- Parses Git commit history using [**Conventional Commits**](https://www.conventionalcommits.org/en/v1.0.0/)
- Detects:
    - **Breaking changes** → major
    - **Features** → minor
    - **Fixes** → patch
- Computes the next semantic version
- Exposes the computed version to Gradle builds.
Configuration cache friendly → fully compatible with Gradle’s configuration cache, ensuring smooth builds in future Gradle versions

### Installation

* Add the plugin to the plugins block in your `build.gradle `file:

  ```groovy
  plugins {
     id 'io.github.sttamper.upcoming4j' version '0.0.4'
  }
  ```

### Usage

- The computed version is available as a project property **`nx`**. 
  Add it in the `build.gradle` file:

  ```groovy
  version = nx()
  ```

- Reload Gradle project to apply the changes.
