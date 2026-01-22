# Upcoming4j Gradle Plugin

`Upcoming4j` is a lightweight Gradle plugin that computes the next semantic version of a project based on its Git commit history, 
following the Conventional Commits specification.

The plugin analyzes commits since the last release and determines whether the next version should be a major, minor, or patch 
increment, without requiring manual version updates or complex branching rules.

`Upcoming4j` is **_read-only by design_**: it does not create tags, publish artifacts, or push releases. 
Instead, it focuses on doing one thing well (calculating the next version number), 
so it can be safely combined with release tools like JReleaser, CI pipelines, or custom workflows.

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
   id 'com.example.upcoming4j' version '0.0.1'
}
```

### Usage
The computed version is exposed as a project extra property called `nextVersion`, 
which you can assign to your project on `build.gradle` file:

```groovy
version = project.ext.nextVersion
```

>Note: The plugin executes during Gradle’s configuration phase, 
>so the `nextVersion` property is available immediately for the rest of your build script. 
>This allows you to use it in other configurations, tasks, or plugins that depend on the project version.
