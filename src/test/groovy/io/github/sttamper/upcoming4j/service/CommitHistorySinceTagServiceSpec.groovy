package io.github.sttamper.upcoming4j.service

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOutput
import spock.lang.Specification

class CommitHistorySinceTagServiceSpec extends Specification {

  private Project projectMock
  private Logger loggerMock
  private ProviderFactory providersMock
  private CommitHistorySinceTagService service

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
    String tag = "v1.0.0"

    ExecOutput execOutputMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent stdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> textProviderMock = Mock(Provider)

    textProviderMock.get() >> "feat: hello world\nfix: bug 123\nchore: update deps"
    stdoutMock.asText >> textProviderMock
    execOutputMock.standardOutput >> stdoutMock

    providersMock.exec(_) >> execOutputMock

    when:
    List<String> result = service.retrieve(tag)

    then:
    result == ["feat: hello world", "fix: bug 123", "chore: update deps"]

    and:
    1 * loggerMock.lifecycle("RETRIVE COMMIT HISTORY SINCE TAG: ${tag}")
    1 * loggerMock.lifecycle("get commit messages: bash -c git log ${tag}..HEAD --pretty=format:%s")
    1 * loggerMock.lifecycle("3 commits found since tag: ${tag}")
  }

  def "retrieve should return empty list if no commits found"() {
    given:
    String tag = "v1.0.0"

    ExecOutput execOutputMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent stdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> textProviderMock = Mock(Provider)

    // Simulate no commits (also covers whitespace-only output after trim)
    textProviderMock.get() >> "   \n"
    stdoutMock.asText >> textProviderMock
    execOutputMock.standardOutput >> stdoutMock

    providersMock.exec(_) >> execOutputMock

    when:
    List<String> result = service.retrieve(tag)

    then:
    result == []

    and:
    1 * loggerMock.lifecycle("RETRIVE COMMIT HISTORY SINCE TAG: ${tag}")
    1 * loggerMock.lifecycle("get commit messages: bash -c git log ${tag}..HEAD --pretty=format:%s")
    1 * loggerMock.lifecycle("No commits found since tag: ${tag}")
  }

  def "retrieve should handle single commit correctly"() {
    given:
    String tag = "v1.0.0"

    ExecOutput execOutputMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent stdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> textProviderMock = Mock(Provider)

    textProviderMock.get() >> "feat: single commit"
    stdoutMock.asText >> textProviderMock
    execOutputMock.standardOutput >> stdoutMock

    providersMock.exec(_) >> execOutputMock

    when:
    List<String> result = service.retrieve(tag)

    then:
    result == ["feat: single commit"]

    and:
    1 * loggerMock.lifecycle("RETRIVE COMMIT HISTORY SINCE TAG: ${tag}")
    1 * loggerMock.lifecycle("get commit messages: bash -c git log ${tag}..HEAD --pretty=format:%s")
    1 * loggerMock.lifecycle("1 commits found since tag: ${tag}")
  }

  def "retrieve should return empty list and not execute git log when tag is NONE_TAG"() {
    given:
    String tag = LatestCreatedTagService.NONE_TAG

    when:
    List<String> result = service.retrieve(tag)

    then:
    result == []

    and:
    1 * loggerMock.lifecycle("RETRIVE COMMIT HISTORY SINCE TAG: ${tag}")
    1 * loggerMock.lifecycle("No previous tag found. Returning empty commit history.")
    0 * providersMock.exec(_)
  }
}