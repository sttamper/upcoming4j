package io.github.sttamper.upcoming4j.service

import io.github.sttamper.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOutput
import spock.lang.Specification

class LatestCreatedTagServiceSpec extends Specification {

  Project projectMock
  Logger loggerMock
  ProviderFactory providersMock
  LatestCreatedTagService service

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
    // Mock git fetch exec
    ExecOutput fetchExecMock = Mock()
    ExecOutput.StandardStreamContent fetchOutputMock = Mock()
    Provider<String> fetchProviderMock = Mock()
    fetchProviderMock.get() >> "" // git fetch output is not relevant
    fetchOutputMock.asText >> fetchProviderMock
    fetchExecMock.standardOutput >> fetchOutputMock

    // Mock git for-each-ref exec
    ExecOutput forEachExecMock = Mock()
    ExecOutput.StandardStreamContent forEachOutputMock = Mock()
    Provider<String> forEachProviderMock = Mock()
    forEachProviderMock.get() >> "v1.2.3" // valid latest tag
    forEachOutputMock.asText >> forEachProviderMock
    forEachExecMock.standardOutput >> forEachOutputMock

    // Sequential exec calls
    providersMock.exec(_) >>> [fetchExecMock, forEachExecMock]

    when:
    def latestTag = service.retrieve()

    then:
    latestTag == "v1.2.3"

    and:
    1 * loggerMock.lifecycle("Retrieve the latest created git tag.")
    1 * loggerMock.lifecycle("Git fetch command: bash -c git fetch --tags --prune")
    1 * loggerMock.lifecycle("Git fetch command succeeded")
    1 * loggerMock.lifecycle("Git for each command: bash -c git for-each-ref --sort=-creatordate --format='%(refname:short)' refs/tags | head -n 1")
    1 * loggerMock.lifecycle("Git for each command succeeded. Latest Git tag: v1.2.3")
  }

  def "retrieve should throw exception for invalid git tag format"() {
    given:
    ExecOutput fetchExecMock = Mock()
    ExecOutput.StandardStreamContent fetchOutputMock = Mock()
    Provider<String> fetchProviderMock = Mock()
    fetchProviderMock.get() >> ""
    fetchOutputMock.asText >> fetchProviderMock
    fetchExecMock.standardOutput >> fetchOutputMock

    ExecOutput forEachExecMock = Mock()
    ExecOutput.StandardStreamContent forEachOutputMock = Mock()
    Provider<String> forEachProviderMock = Mock()
    forEachProviderMock.get() >> "invalid_tag" // invalid tag
    forEachOutputMock.asText >> forEachProviderMock
    forEachExecMock.standardOutput >> forEachOutputMock

    providersMock.exec(_) >>> [fetchExecMock, forEachExecMock]

    when:
    service.retrieve()

    then:
    def ex = thrown(Upcoming4jException)
    ex.message == "Invalid git tag format 'invalid_tag'. Expected format is vX.Y.Z (example: v1.2.3)"
  }

  def "retrieve should throw exception if no tags found"() {
    given:
    ExecOutput fetchExecMock = Mock()
    ExecOutput.StandardStreamContent fetchOutputMock = Mock()
    Provider<String> fetchProviderMock = Mock()
    fetchProviderMock.get() >> ""
    fetchOutputMock.asText >> fetchProviderMock
    fetchExecMock.standardOutput >> fetchOutputMock

    ExecOutput forEachExecMock = Mock()
    ExecOutput.StandardStreamContent forEachOutputMock = Mock()
    Provider<String> forEachProviderMock = Mock()
    forEachProviderMock.get() >> "" // no tags at all
    forEachOutputMock.asText >> forEachProviderMock
    forEachExecMock.standardOutput >> forEachOutputMock

    providersMock.exec(_) >>> [fetchExecMock, forEachExecMock]

    when:
    service.retrieve()

    then:
    def ex = thrown(Upcoming4jException)
    ex.message == "No git tags found in the repository"
  }
}