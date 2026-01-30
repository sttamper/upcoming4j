package io.github.sttamper.upcoming4j.service

import io.github.sttamper.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOutput
import spock.lang.Specification

class LatestCreatedTagServiceSpec extends Specification {

  private Project projectMock
  private Logger loggerMock
  private ProviderFactory providersMock
  private LatestCreatedTagService service

  def setup() {
    projectMock = Mock(Project)
    loggerMock = Mock(Logger)
    providersMock = Mock(ProviderFactory)

    projectMock.logger >> loggerMock
    projectMock.providers >> providersMock

    service = new LatestCreatedTagService(projectMock)
  }

  def "retrieve should return latest valid git tag"() {
    given:
    // Mock: git fetch --tags --prune
    ExecOutput fetchExecMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent fetchStdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> fetchTextProviderMock = Mock(Provider)
    fetchTextProviderMock.get() >> ""
    fetchStdoutMock.asText >> fetchTextProviderMock
    fetchExecMock.standardOutput >> fetchStdoutMock

    // Mock: git for-each-ref ... | head -n 1
    ExecOutput forEachExecMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent forEachStdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> forEachTextProviderMock = Mock(Provider)
    forEachTextProviderMock.get() >> "v1.2.3\n"
    forEachStdoutMock.asText >> forEachTextProviderMock
    forEachExecMock.standardOutput >> forEachStdoutMock

    // First exec() call returns fetch, second returns for-each-ref
    providersMock.exec(_) >>> [fetchExecMock, forEachExecMock]

    when:
    String latestTag = service.retrieve()

    then:
    latestTag == "v1.2.3"

    and:
    1 * loggerMock.lifecycle("RETRIEVE THE LATEST CREATED TAG")
    1 * loggerMock.lifecycle("fetch git tags: bash -c git fetch --tags --prune")
    1 * loggerMock.lifecycle(
        "get the most recent tag by creation time: bash -c git for-each-ref --sort=-creatordate --format='%(refname:short)' refs/tags | head -n 1"
    )
    1 * loggerMock.lifecycle("Latest Git tag: v1.2.3")
  }

  def "retrieve should throw exception for invalid git tag format"() {
    given:
    ExecOutput fetchExecMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent fetchStdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> fetchTextProviderMock = Mock(Provider)
    fetchTextProviderMock.get() >> ""
    fetchStdoutMock.asText >> fetchTextProviderMock
    fetchExecMock.standardOutput >> fetchStdoutMock

    ExecOutput forEachExecMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent forEachStdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> forEachTextProviderMock = Mock(Provider)
    forEachTextProviderMock.get() >> "invalid_tag"
    forEachStdoutMock.asText >> forEachTextProviderMock
    forEachExecMock.standardOutput >> forEachStdoutMock

    providersMock.exec(_) >>> [fetchExecMock, forEachExecMock]

    when:
    service.retrieve()

    then:
    Upcoming4jException ex = thrown(Upcoming4jException)
    ex.message ==
        "Cannot retrieve last tag. Invalid git tag format 'invalid_tag'. Expected format is vX.Y.Z (example: v1.2.3)"
  }

  def "retrieve should return NONE_TAG if no tags found"() {
    given:
    ExecOutput fetchExecMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent fetchStdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> fetchTextProviderMock = Mock(Provider)
    fetchTextProviderMock.get() >> ""
    fetchStdoutMock.asText >> fetchTextProviderMock
    fetchExecMock.standardOutput >> fetchStdoutMock

    ExecOutput forEachExecMock = Mock(ExecOutput)
    ExecOutput.StandardStreamContent forEachStdoutMock = Mock(ExecOutput.StandardStreamContent)
    Provider<String> forEachTextProviderMock = Mock(Provider)
    forEachTextProviderMock.get() >> "   \n"  // trims to empty
    forEachStdoutMock.asText >> forEachTextProviderMock
    forEachExecMock.standardOutput >> forEachStdoutMock

    providersMock.exec(_) >>> [fetchExecMock, forEachExecMock]

    when:
    String latestTag = service.retrieve()

    then:
    latestTag == LatestCreatedTagService.NONE_TAG

    and:
    1 * loggerMock.lifecycle("No git tags found in the repository. default tag to: ${LatestCreatedTagService.NONE_TAG}")
  }
}