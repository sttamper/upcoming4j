package com.sttamper.upcoming4j.service

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOutput
import spock.lang.Specification

class CommitHistorySinceTagServiceSpec extends Specification {

  Project projectMock
  Logger loggerMock
  ProviderFactory providersMock
  CommitHistorySinceTagService service

  def setup() {
    projectMock = Mock(Project)
    loggerMock = Mock(Logger)
    providersMock = Mock(ProviderFactory)
    projectMock.logger >> loggerMock
    projectMock.providers >> providersMock
    service = new CommitHistorySinceTagService(projectMock)
  }

  def "retrieve should return multiple commit messages since tag"() {
    given:
    def tag = "v1.0.0"

    ExecOutput execOutputMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent standardStreamContentMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> stringProviderMock = Mock(Provider<String>)
    stringProviderMock.get() >> "feat: hello world\nfix: bug 123\nchore: update deps"
    standardStreamContentMock.asText >> stringProviderMock
    execOutputMock.standardOutput >> standardStreamContentMock
    providersMock.exec(_) >> execOutputMock

    when:
    def result = service.retrieve(tag)

    then:
    result == ["feat: hello world", "fix: bug 123", "chore: update deps"]

    and:
    1 * loggerMock.lifecycle("Retrieve commit history since tag: ${tag}")
    1 * loggerMock.lifecycle("Git log command: bash -c git log ${tag}..HEAD --pretty=format:%s")
    1 * loggerMock.lifecycle("Git log command succeeded, 3 commits found since tag: ${tag}")
  }

  def "retrieve should return empty list if no commits found"() {
    given:
    def tag = "v1.0.0"

    ExecOutput execOutputMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent standardStreamContentMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> stringProviderMock = Mock(Provider<String>)
    stringProviderMock.get() >> ""   // Simulate no commits
    standardStreamContentMock.asText >> stringProviderMock
    execOutputMock.standardOutput >> standardStreamContentMock
    providersMock.exec(_) >> execOutputMock

    when:
    def result = service.retrieve(tag)

    then:
    result == []

    and:
    1 * loggerMock.lifecycle("Retrieve commit history since tag: ${tag}")
    1 * loggerMock.lifecycle("Git log command: bash -c git log ${tag}..HEAD --pretty=format:%s")
    1 * loggerMock.lifecycle("No commits found since tag: ${tag}")
  }

  def "retrieve should handle single commit correctly"() {
    given:
    def tag = "v1.0.0"

    ExecOutput execOutputMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent standardStreamContentMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> stringProviderMock = Mock(Provider<String>)
    stringProviderMock.get() >> "feat: single commit"
    standardStreamContentMock.asText >> stringProviderMock
    execOutputMock.standardOutput >> standardStreamContentMock
    providersMock.exec(_) >> execOutputMock

    when:
    def result = service.retrieve(tag)

    then:
    result == ["feat: single commit"]

    and:
    1 * loggerMock.lifecycle("Retrieve commit history since tag: ${tag}")
    1 * loggerMock.lifecycle("Git log command: bash -c git log ${tag}..HEAD --pretty=format:%s")
    1 * loggerMock.lifecycle("Git log command succeeded, 1 commits found since tag: ${tag}")
  }
}