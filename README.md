# Upcoming4j Gradle Plugin

`Upcoming4j` is a lightweight Gradle plugin that computes the next semantic version of a project based on its Git commit history,
following the Conventional Commits specification.

The plugin analyzes commits since the last release and determines whether the next version should be a major, minor, or patch
increment, without requiring manual version updates or complex branching rules.

`Upcoming4j` is **_read-only by design_**: it does not create tags, publish artifacts, or push releases.
Instead, it focuses on doing one thing well: _**computing the next version number**_,
so it can be safely combined with release tools like JReleaser, CI pipelines, or custom workflows.

### Prerequisites

`Upcoming4j` relies on Git metadata to compute the next project version. Before using the plugin, ensure that:

- Git is installed and available on your system.
- The project is initialized as a Git repository.
- Git tags follow Semantic Versioning using the format `vX.Y.Z` (for example, v1.2.3).
- Commit messages follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) spec.

### Features

What `Upcoming4j` does:

- Parses Git commit history using [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- Detects:
    - **Breaking changes** → major
    - **Features** → minor
    - **Fixes** → patch
- Computes the next semantic version
- Exposes the computed version to Gradle builds.

### Installation

Installing the `Upcoming4j` plugin is straightforward. Simply add it to your `build.gradle` file:

```groovy
plugins {
   id 'io.stamperlabs.upcoming4j' version '0.0.1'
}
```

> Make sure to point to the latest version of the plugin.

### Usage
The computed version is exposed as a project extra property called `nextVersion`,
which you can assign to your project on `build.gradle` file:

```groovy
version = computeNextVersion()
```

Reload project's gradle configuration to apply the changes.
