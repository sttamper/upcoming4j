package io.github.sttamper.upcoming4j.service

import io.github.sttamper.upcoming4j.exception.Upcoming4jException
import org.gradle.api.Project

class NextSemanticVersionService {

  private final Project project
  private static final String SEMVER_TAG_REGEX = /(\d+)\.(\d+)\.(\d+)/

  NextSemanticVersionService(Project project) {
    this.project = project
  }

  String compute(List<String> commitHistory, String currentTag) throws Upcoming4jException{
    project.logger.lifecycle("Computing next semantic version based on commit history and current tag.")
    if (!currentTag) {
      throw new Upcoming4jException("Current tag is null or empty.")
    }

    def normalizedTag = normalizeTag(currentTag)

    if (!commitHistory || commitHistory.isEmpty()) {
      project.logger.lifecycle("No commits to evaluate since '${currentTag}'. Version remains the same.")
      return currentTag
    }

    def matcher = normalizedTag =~ SEMVER_TAG_REGEX
    if (!matcher.find()) {
      throw new Upcoming4jException("Current tag '${currentTag}' is not in semantic version format")
    }

    int major = matcher[0][1] as int
    int minor = matcher[0][2] as int
    int patch = matcher[0][3] as int

    boolean majorBump = false, minorBump = false, patchBump = false
    commitHistory.each { msg ->
      def lower = msg.toLowerCase()
      if (lower.contains("breaking change")) majorBump = true
      else if (lower.startsWith("feat")) minorBump = true
      else if (lower.startsWith("fix")) patchBump = true
    }

    if (majorBump) { major += 1; minor = 0; patch = 0 }
    else if (minorBump) { minor += 1; patch = 0 }
    else if (patchBump) { patch += 1 }
    else project.logger.lifecycle("No conventional commits detected. Version remains the same.")

    def nextVersion = "${major}.${minor}.${patch}"
    project.logger.lifecycle("Computed next version: ${nextVersion}")
    return nextVersion
  }

  private String normalizeTag(String tag) {
    return tag.startsWith('v') ? tag.substring(1) : tag
  }
}
