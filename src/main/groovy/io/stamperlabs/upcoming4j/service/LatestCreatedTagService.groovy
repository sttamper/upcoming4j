package io.stamperlabs.upcoming4j.service

import io.stamperlabs.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project

class LatestCreatedTagService {
  private Project project
  private static final Integer EXIT_CODE_SUCCESS = 0
  private static final String GIT_TAG_REGEX = /^v\d+\.\d+\.\d+$/

  LatestCreatedTagService(Project project) {
    this.project = project
  }

  public String retrieve() throws Upcoming4jException {
    project.logger.lifecycle("Retrieve the latest created git tag.")
    def gitFetchCommand = ["bash", "-c", "git", "fetch", "--tags", "--prune"]
    project.logger.lifecycle("Git fetch command: ${gitFetchCommand.join(' ')}")

    def fetchResult = project.providers.exec {
      commandLine(gitFetchCommand)
      ignoreExitValue = true
    }.execResult.get()

    if (fetchResult.exitValue != EXIT_CODE_SUCCESS) {
      throw new Upcoming4jException("Cannot fetch tags from remote, command execution failed: exit_code: ${fetchResult.exitValue}")
    }

    project.logger.lifecycle("Git fetch command succeeded")

    def gitForEachRefCommand = ["bash", "-c", "git for-each-ref --sort=-creatordate --format='%(refname:short)' refs/tags | head -n 1"]
    project.logger.lifecycle("Git for each command: ${gitForEachRefCommand.join(' ')}")

    def forEachResultExec = project.providers.exec {
      commandLine(gitForEachRefCommand)
      ignoreExitValue = true
    }.execResult.get()

    if (forEachResultExec.exitValue != EXIT_CODE_SUCCESS) {
      throw new Upcoming4jException("Cannot retrieve latest Git tag, command execution failed: exit_code: ${forEachResultExec.exitValue}")
    }

    def latestGitTag = forEachResultExec.standardOutput.asText.get().trim()

    if (!this.isTagFormatValid(latestGitTag)) {
      throw new Upcoming4jException(
          "Invalid git tag format '${latestGitTag}'. Expected format is vX.Y.Z (example: v1.2.3)"
      )
    }

    project.logger.lifecycle("Git for each command succeded. Latest Git tag: ${latestGitTag}")

    return latestGitTag
  }

  private boolean isTagFormatValid(String tag) {
    return tag.matches(GIT_TAG_REGEX);
  }
}
