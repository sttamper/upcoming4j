package io.github.sttamper.upcoming4j.service

import io.github.sttamper.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project

class LatestCreatedTagService {

  private final Project project
  private static final String GIT_TAG_REGEX = /^v\d+\.\d+\.\d+$/

  LatestCreatedTagService(Project project) {
    this.project = project
  }

  String retrieve() throws Upcoming4jException {
    project.logger.lifecycle("RETRIEVE THE LATEST CREATED TAG" )
    def gitFetchCommand = ["bash", "-c", "git fetch --tags --prune"]
    project.logger.lifecycle("fetch git tags: ${gitFetchCommand.join(' ')}" )
    project.providers.exec {
      commandLine(gitFetchCommand)
    }.standardOutput.asText.get()

    def gitForEachRefCommand = [
        "bash",
        "-c",
        "git for-each-ref --sort=-creatordate --format='%(refname:short)' refs/tags | head -n 1"
    ]
    project.logger.lifecycle("get the most recent tag by creation time: ${gitForEachRefCommand.join(' ')}")
    String latestGitTagRaw = project.providers.exec {
      commandLine(gitForEachRefCommand)
    }.standardOutput.asText.get()

    String latestGitTag = latestGitTagRaw?.trim() ?: ""
    if (!latestGitTag) {
      throw new Upcoming4jException("Cannot retrieve last tag. No git tags found in the repository.")
    }

    if (!isTagFormatValid(latestGitTag)) {
      throw new Upcoming4jException(
          "Cannot retrieve last tag. Invalid git tag format '${latestGitTag}'. Expected format is vX.Y.Z (example: v1.2.3)"
      )
    }

    project.logger.lifecycle("Latest Git tag: ${latestGitTag}")
    return latestGitTag
  }

  private boolean isTagFormatValid(String tag) {
    tag.matches(GIT_TAG_REGEX)
  }
}