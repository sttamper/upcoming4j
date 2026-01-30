package io.github.sttamper.upcoming4j.service

import io.github.sttamper.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import spock.lang.Specification

class NextSemanticVersionServiceSpec extends Specification {

  private Project projectMock
  private Logger loggerMock
  private NextSemanticVersionService service

  def setup() {
    projectMock = Mock(Project)
    loggerMock = Mock(Logger)
    projectMock.logger >> loggerMock
    service = new NextSemanticVersionService(projectMock)
  }

  def "compute should bump major version for BREAKING CHANGE commit"() {
    given:
    List<String> commitHistory = ["feat: add feature", "fix: bug", "BREAKING CHANGE: rewrite API"]
    String currentTag = "v1.2.3"

    when:
    String nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "2.0.0"

    and:
    1 * loggerMock.lifecycle("COMPUTE NEXT VERSION. COMMIT HISTORY SIZE: 3, TAG: v1.2.3")
    1 * loggerMock.lifecycle("Next version: 2.0.0")
  }

  def "compute should bump minor version for feat commit"() {
    given:
    List<String> commitHistory = ["feat: add feature"]
    String currentTag = "v1.2.3"

    when:
    String nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "1.3.0"

    and:
    1 * loggerMock.lifecycle("COMPUTE NEXT VERSION. COMMIT HISTORY SIZE: 1, TAG: v1.2.3")
    1 * loggerMock.lifecycle("Next version: 1.3.0")
  }

  def "compute should bump patch version for fix commit"() {
    given:
    List<String> commitHistory = ["fix: bug in module"]
    String currentTag = "v1.2.3"

    when:
    String nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "1.2.4"

    and:
    1 * loggerMock.lifecycle("COMPUTE NEXT VERSION. COMMIT HISTORY SIZE: 1, TAG: v1.2.3")
    1 * loggerMock.lifecycle("Next version: 1.2.4")
  }

  def "compute should keep version the same if no conventional commits"() {
    given:
    List<String> commitHistory = ["docs: update README", "chore: clean up"]
    String currentTag = "v1.2.3"

    when:
    String nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "1.2.3"

    and:
    1 * loggerMock.lifecycle("COMPUTE NEXT VERSION. COMMIT HISTORY SIZE: 2, TAG: v1.2.3")
    1 * loggerMock.lifecycle("No conventional commits detected.")
    1 * loggerMock.lifecycle("Next version remains the same: '1.2.3'")
  }

  def "compute should return same version if commit history is empty"() {
    given:
    List<String> commitHistory = []
    String currentTag = "v1.2.3"

    when:
    String nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "1.2.3"

    and:
    1 * loggerMock.lifecycle("COMPUTE NEXT VERSION. COMMIT HISTORY SIZE: 0, TAG: v1.2.3")
    1 * loggerMock.lifecycle("No commits to evaluate since tag: 'v1.2.3'.")
    1 * loggerMock.lifecycle("Next version remains the same: '1.2.3'")
  }

  def "compute should return starter version if current tag is null or empty"() {
    when:
    String nextVersionNull = service.compute([], null)

    then:
    nextVersionNull == "0.0.1"
    1 * loggerMock.lifecycle("COMPUTE NEXT VERSION. COMMIT HISTORY SIZE: 0, TAG: null")
    1 * loggerMock.lifecycle("No previous tag found. Starting from version 0.0.1")

    when:
    String nextVersionEmpty = service.compute([], "")

    then:
    nextVersionEmpty == "0.0.1"
    1 * loggerMock.lifecycle("COMPUTE NEXT VERSION. COMMIT HISTORY SIZE: 0, TAG: ")
    1 * loggerMock.lifecycle("No previous tag found. Starting from version 0.0.1")
  }

  def "compute should return starter version if current tag is NONE_TAG"() {
    given:
    String currentTag = LatestCreatedTagService.NONE_TAG

    when:
    String nextVersion = service.compute([], currentTag)

    then:
    nextVersion == "0.0.1"

    and:
    1 * loggerMock.lifecycle("COMPUTE NEXT VERSION. COMMIT HISTORY SIZE: 0, TAG: ${LatestCreatedTagService.NONE_TAG}")
    1 * loggerMock.lifecycle("No previous tag found. Starting from version 0.0.1")
  }

  def "compute should throw exception if current tag (after stripping v) is not semantic version format"() {
    given:
    List<String> commitHistory = ["feat: something"]
    String currentTag = "version1.2"

    when:
    service.compute(commitHistory, currentTag)

    then:
    Upcoming4jException ex = thrown(Upcoming4jException)
    ex.message == "Cannot compute next version. 'ersion1.2' is not in semantic version format"
  }
}