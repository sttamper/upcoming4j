package io.github.sttamper.upcoming4j.service

import org.gradle.api.Project

class CommitHistorySinceTagService {
  private final Project project

  CommitHistorySinceTagService(Project project) {
    this.project = project
  }

  List<String> retrieve(String tag) {
    project.logger.lifecycle("RETRIVE COMMIT HISTORY SINCE TAG: ${tag}")

    if (tag == LatestCreatedTagService.NONE_TAG) {
      project.logger.lifecycle("No previous tag found. Returning empty commit history.")
      return []
    }

    def gitLogCommand = ["bash", "-c", "git log ${tag}..HEAD --pretty=format:%s"]
    project.logger.lifecycle("get commit messages: ${gitLogCommand.join(' ')}")

    String commitMessagesRaw = project.providers.exec {
      commandLine(gitLogCommand)
    }.standardOutput.asText.get()

    String commitMessages = commitMessagesRaw?.trim() ?: ""

    if (!commitMessages) {
      project.logger.lifecycle("No commits found since tag: ${tag}")
      return []
    }

    def commitMessageList = commitMessages.readLines()
    project.logger.lifecycle("${commitMessageList.size()} commits found since tag: ${tag}")

    return commitMessageList
  }
}