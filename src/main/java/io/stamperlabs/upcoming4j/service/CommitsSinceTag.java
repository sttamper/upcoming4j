package io.stamperlabs.upcoming4j.service;

import io.stamperlabs.upcoming4j.exception.Upcoming4jException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.gradle.api.Project;

public class CommitsSinceTag {

  private final String gitTag;
  private final ProcessBuilder processBuilder;
  private final Project project;
  private static final Integer SUCCESS_EXIT_CODE = 0;

  public CommitsSinceTag(ProcessBuilder processBuilder, Project project, String gitTag) {
    this.processBuilder = processBuilder;
    this.project = project;
    this.gitTag = gitTag;
  }

  public List<String> retrieve() throws Upcoming4jException {
    var logger = project.getLogger();
    logger.lifecycle("Retrieving commits since tag: {}", gitTag);
    logger.lifecycle("Git command: {}", String.join(" ", processBuilder.command()));
    Process process = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    try {
      process = processBuilder.start();
      inputStreamReader = new InputStreamReader(process.getInputStream());
      bufferedReader = new BufferedReader(inputStreamReader);
      var commits = bufferedReader.lines().toList();
      commits.forEach(commit -> logger.lifecycle("Commit --> {}", commit));
      int exitCode = process.waitFor();
      if (exitCode != SUCCESS_EXIT_CODE) {
        throw new RuntimeException("Git command failed with exit code " + exitCode);
      }
      return commits;
    } catch (IOException e) {
      throw new Upcoming4jException(
          "Failed to execute `git log` to determine the commit history for tag. "
              + "Is Git installed and available on your PATH?",
          e);
    } catch (InterruptedException e) {
      throw new Upcoming4jException("git log was interrupted", e);
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
          logger.warn("Failed to close BufferedReader", e);
        }
      }
    }
  }
}
