package io.stamperlabs.upcoming4j.service;

import io.stamperlabs.upcoming4j.exception.Upcoming4jException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.gradle.api.Project;

public class LatestGitTagService {

  private final ProcessBuilder gitFetchProcessBuilder;
  private final ProcessBuilder gitForEachRefProcessBuilder;
  private final Project project;
  private static final Integer SUCCESS_EXIT_CODE = 0;
  private static final String FALLBACK_VERSION = "0.0.1";

  public LatestGitTagService(
      ProcessBuilder gitFetchProcessBuilder,
      ProcessBuilder gitForEachRefProcessBuilder,
      Project project) {
    this.gitFetchProcessBuilder = gitFetchProcessBuilder;
    this.gitForEachRefProcessBuilder = gitForEachRefProcessBuilder;
    this.project = project;
  }

  public String retrieve() throws Upcoming4jException {
    var logger = project.getLogger();
    logger.lifecycle("Retrieve the latest Git tag");
    logger.lifecycle("Fetch git tags: {}", String.join(" ", gitFetchProcessBuilder.command()));

    try {
      var fetchProcess = gitFetchProcessBuilder.start();
      int fetchExitCode = fetchProcess.waitFor();

      if (fetchExitCode != SUCCESS_EXIT_CODE) {
        throw new Upcoming4jException(
            "Git fetch failed (exit code "
                + fetchExitCode
                + "). "
                + "Make sure Git is installed and the repository is accessible.");
      }
    } catch (IOException e) {
      throw new Upcoming4jException(
          "Failed to execute `git fetch`. Is Git installed and on your PATH?", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Upcoming4jException("Git fetch was interrupted", e);
    }

    logger.lifecycle(
        "For each ref tags: {}", String.join(" ", gitForEachRefProcessBuilder.command()));

    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;

    try {
      var process = gitForEachRefProcessBuilder.start();

      inputStreamReader = new InputStreamReader(process.getInputStream());
      bufferedReader = new BufferedReader(inputStreamReader);

      var line = bufferedReader.readLine();
      int exitCode = process.waitFor();

      if (exitCode != SUCCESS_EXIT_CODE) {
        throw new Upcoming4jException(
            "Failed to determine the latest Git tag. "
                + "`git for-each-ref` exited with code "
                + exitCode
                + ". "
                + "Make sure the repository has tags and Git is working correctly.");
      }

      if (line == null || line.isBlank()) {
        logger.lifecycle("No Git tags found. Using fallback version {}", FALLBACK_VERSION);
        return FALLBACK_VERSION;
      }

      var latestTag = line.trim();
      logger.lifecycle("Latest Git tag found: {}", latestTag);
      return latestTag;

    } catch (IOException e) {
      throw new Upcoming4jException(
          "Failed to execute `git for-each-ref` to determine the latest tag. "
              + "Is Git installed and available on your PATH?",
          e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Upcoming4jException("Git tag discovery was interrupted", e);
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
