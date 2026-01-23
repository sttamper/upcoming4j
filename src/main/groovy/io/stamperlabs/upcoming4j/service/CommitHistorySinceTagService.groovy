package io.stamperlabs.upcoming4j.service

import io.stamperlabs.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project

class CommitHistorySinceTagService {
  private final Project project
  private static final Integer EXIT_CODE_SUCCESS = 0

  CommitHistorySinceTagService(Project project) {
    this.project = project
  }

  List<String> retrieve(String tag) throws Upcoming4jException {
    project.logger.lifecycle("Retrieve commit history since tag: ${tag}")

    def gitLogCommand = ["bash", "-c", "git log ${tag}..HEAD --pretty=format:%s"]
    project.logger.lifecycle("Git log command: ${gitLogCommand.join(' ')}")

    def logResultExec = project.providers.exec {
      commandLine(gitLogCommand)
      ignoreExitValue = true
    }.execResult.get()

    if (logResultExec.exitValue != EXIT_CODE_SUCCESS) {
      throw new Upcoming4jException(
          "Cannot retrieve commit history since tag '${tag}', command execution failed: exit_code: ${logResultExec.exitValue}"
      )
    }

    def commitMessagesRaw = logResultExec.standardOutput.asText.get().trim()
    if (!commitMessagesRaw) {
      project.logger.lifecycle("No commits found since tag: ${tag}")
      return []
    }

    def commitMessages = commitMessagesRaw.readLines()  // safer than split('\n')
    project.logger.lifecycle("Git log command succeeded, ${commitMessages.size()} commits found since tag: ${tag}")

    return commitMessages
  }
}