package com.sttamper.upcoming4j.service

import org.gradle.api.Project

class CommitHistorySinceTagService {
  private final Project project

  CommitHistorySinceTagService(Project project) {
    this.project = project
  }

  List<String> retrieve(String tag) {
    project.logger.lifecycle("Retrieve commit history since tag: ${tag}")

    def gitLogCommand = ["bash", "-c", "git log ${tag}..HEAD --pretty=format:%s"]
    project.logger.lifecycle("Git log command: ${gitLogCommand.join(' ')}")


    String commitMessagesRaw = project.providers.exec {
      commandLine "bash", "-c", "git log ${tag}..HEAD --pretty=format:%s"
    }.standardOutput.asText.get()

    String commitMessages = commitMessagesRaw?.trim() ?: ""

    if (!commitMessages) {
      project.logger.lifecycle("No commits found since tag: ${tag}")
      return []
    }

    def commitMessageList = commitMessages.readLines()  // safer than split('\n')
    project.logger.lifecycle("Git log command succeeded, ${commitMessageList.size()} commits found since tag: ${tag}")

    return commitMessageList
  }
}