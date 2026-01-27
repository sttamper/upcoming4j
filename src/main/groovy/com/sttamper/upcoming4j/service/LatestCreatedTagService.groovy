package com.sttamper.upcoming4j.service

import com.sttamper.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project

class LatestCreatedTagService {

  private final Project project
  private static final String GIT_TAG_REGEX = /^v\d+\.\d+\.\d+$/

  LatestCreatedTagService(Project project) {
    this.project = project
  }

  String retrieve() throws Upcoming4jException {
    project.logger.lifecycle("Retrieve the latest created git tag.")

    def gitFetchCommand = ["bash", "-c", "git fetch --tags --prune"]
    project.logger.lifecycle("Git fetch command: ${gitFetchCommand.join(' ')}")

    project.providers.exec {
      commandLine(gitFetchCommand)
    }.standardOutput.asText.get()

    project.logger.lifecycle("Git fetch command succeeded")

    def gitForEachRefCommand = [
        "bash",
        "-c",
        "git for-each-ref --sort=-creatordate --format='%(refname:short)' refs/tags | head -n 1"
    ]
    project.logger.lifecycle("Git for each command: ${gitForEachRefCommand.join(' ')}")

    String latestGitTagRaw = project.providers.exec {
      commandLine "bash", "-c",
          "git for-each-ref --sort=-creatordate --format='%(refname:short)' refs/tags | head -n 1"
    }.standardOutput.asText.get()

    String latestGitTag = latestGitTagRaw?.trim() ?: ""
    if (!latestGitTag) {
      throw new Upcoming4jException("No git tags found in the repository")
    }

    if (!isTagFormatValid(latestGitTag)) {
      throw new Upcoming4jException(
          "Invalid git tag format '${latestGitTag}'. Expected format is vX.Y.Z (example: v1.2.3)"
      )
    }

    project.logger.lifecycle("Git for each command succeeded. Latest Git tag: ${latestGitTag}")

    return latestGitTag
  }

  private boolean isTagFormatValid(String tag) {
    tag.matches(GIT_TAG_REGEX)
  }
}