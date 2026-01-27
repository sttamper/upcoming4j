package com.sttamper.upcoming4j.service

import com.sttamper.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import spock.lang.Specification

class NextSemanticVersionServiceSpec extends Specification {

  Project projectMock
  Logger loggerMock
  NextSemanticVersionService service

  def setup() {
    projectMock = Mock(Project)
    loggerMock = Mock(Logger)
    projectMock.logger >> loggerMock
    service = new NextSemanticVersionService(projectMock)
  }

  def "compute should bump major version for BREAKING CHANGE commit"() {
    given:
    def commitHistory = ["feat: add feature", "fix: bug", "BREAKING CHANGE: rewrite API"]
    def currentTag = "v1.2.3"

    when:
    def nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "2.0.0"
    1 * loggerMock.lifecycle("Computing next semantic version based on commit history and current tag.")
    1 * loggerMock.lifecycle("Computed next version: 2.0.0")
  }

  def "compute should bump minor version for feat commit"() {
    given:
    def commitHistory = ["feat: add feature"]
    def currentTag = "v1.2.3"

    when:
    def nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "1.3.0"
    1 * loggerMock.lifecycle("Computing next semantic version based on commit history and current tag.")
    1 * loggerMock.lifecycle("Computed next version: 1.3.0")
  }

  def "compute should bump patch version for fix commit"() {
    given:
    def commitHistory = ["fix: bug in module"]
    def currentTag = "v1.2.3"

    when:
    def nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "1.2.4"
    1 * loggerMock.lifecycle("Computing next semantic version based on commit history and current tag.")
    1 * loggerMock.lifecycle("Computed next version: 1.2.4")
  }

  def "compute should keep version the same if no conventional commits"() {
    given:
    def commitHistory = ["docs: update README", "chore: clean up"]
    def currentTag = "v1.2.3"

    when:
    def nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "1.2.3"
    1 * loggerMock.lifecycle("Computing next semantic version based on commit history and current tag.")
    1 * loggerMock.lifecycle("No conventional commits detected. Version remains the same.")
    1 * loggerMock.lifecycle("Computed next version: 1.2.3")
  }

  def "compute should return same version if commit history is empty"() {
    given:
    def commitHistory = []
    def currentTag = "v1.2.3"

    when:
    def nextVersion = service.compute(commitHistory, currentTag)

    then:
    nextVersion == "v1.2.3"
    1 * loggerMock.lifecycle("Computing next semantic version based on commit history and current tag.")
    1 * loggerMock.lifecycle("No commits to evaluate since 'v1.2.3'. Version remains the same.")
  }

  def "compute should throw exception if current tag is null or empty"() {
    when:
    service.compute([], null)

    then:
    def ex = thrown(Upcoming4jException)
    ex.message == "Current tag is null or empty."

    when:
    service.compute([], "")

    then:
    def ex2 = thrown(Upcoming4jException)
    ex2.message == "Current tag is null or empty."
  }

  def "compute should throw exception if current tag is not semantic version format"() {
    given:
    def commitHistory = ["feat: something"]
    def currentTag = "version1.2"

    when:
    service.compute(commitHistory, currentTag)

    then:
    def ex = thrown(Upcoming4jException)
    ex.message == "Current tag 'version1.2' is not in semantic version format"
  }
}