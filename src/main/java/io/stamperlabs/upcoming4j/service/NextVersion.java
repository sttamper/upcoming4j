package io.stamperlabs.upcoming4j.service;

import io.stamperlabs.upcoming4j.exception.Upcoming4jException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.api.Project;

public class NextVersion {

  private final Project project;
  private static final String TAG_PREFIX = "v";

  public NextVersion(Project project) throws Upcoming4jException {
    this.project = project;
  }

  public String compute(String currentTag, List<String> commitHistory) {
    var logger = project.getLogger();

    String normalizedTag = normalizeCurrentTag(currentTag);
    if (commitHistory == null || commitHistory.isEmpty()) {
      logger.lifecycle("No commits to evaluate. Version remains {}", normalizedTag);
      return normalizedTag;
    }

    Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    Matcher matcher = pattern.matcher(normalizedTag);

    if (!matcher.matches()) {
      throw new Upcoming4jException(
          "Current tag '" + normalizedTag + "' is not semantic version format (X.Y.Z)");
    }

    int major = Integer.parseInt(matcher.group(1));
    int minor = Integer.parseInt(matcher.group(2));
    int patch = Integer.parseInt(matcher.group(3));

    boolean majorBump = false;
    boolean minorBump = false;
    boolean patchBump = false;

    for (String msg : commitHistory) {
      String lower = msg.toLowerCase();

      if (lower.contains("breaking change") || lower.contains("!:")) {
        majorBump = true;
      } else if (lower.startsWith("feat")) {
        minorBump = true;
      } else if (lower.startsWith("fix")) {
        patchBump = true;
      }
    }

    if (majorBump) {
      major++;
      minor = 0;
      patch = 0;
    } else if (minorBump) {
      minor++;
      patch = 0;
    } else if (patchBump) {
      patch++;
    } else {
      logger.lifecycle("No conventional commits detected. Version remains the same.");
    }

    String nextVersion = major + "." + minor + "." + patch;
    logger.lifecycle("Computed next version: {}", nextVersion);
    return nextVersion;
  }

  private String normalizeCurrentTag(String currentTag) {
    if (currentTag == null || currentTag.isBlank()) {
      return currentTag;
    }
    return currentTag.startsWith(TAG_PREFIX) ? currentTag.substring(1) : currentTag;
  }
}
