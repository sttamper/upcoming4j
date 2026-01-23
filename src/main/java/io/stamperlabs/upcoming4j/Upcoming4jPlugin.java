package io.stamperlabs.upcoming4j;

import io.stamperlabs.upcoming4j.process.GitProcessBuilderFactory;
import io.stamperlabs.upcoming4j.service.CommitsSinceTag;
import io.stamperlabs.upcoming4j.service.LatestGitTagService;
import io.stamperlabs.upcoming4j.service.NextVersion;
import java.io.File;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Upcoming4jPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    var nextVersionProvider =
        project.provider(
            () -> {
              this.checkGitProject(project);
              ProcessBuilder gitFetchProcessBuilder =
                  GitProcessBuilderFactory.fetchTags(project.getRootDir());
              ProcessBuilder gitForEachRefProcessBuilder =
                  GitProcessBuilderFactory.latestTag(project.getRootDir());

              var latestGitTagService =
                  new LatestGitTagService(
                      gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);
              String gitTag = latestGitTagService.retrieve();

              ProcessBuilder gitLogProcessBuilder =
                  GitProcessBuilderFactory.logSinceTag(project.getRootDir(), gitTag);
              var commitsSinceTagService =
                  new CommitsSinceTag(gitLogProcessBuilder, project, gitTag);
              var commitHistory = commitsSinceTagService.retrieve();

              var nextVersionService = new NextVersion(project);
              return nextVersionService.compute(gitTag, commitHistory);
            });

    project.getExtensions().getExtraProperties().set("nextVersion", nextVersionProvider);
  }

  private void checkGitProject(Project project) {
    if (!this.isAGitProject(project.getRootDir())) {
      project
          .getLogger()
          .error(
              "This project is not a Git repository. Please initialize Git with 'git init' before using Upcoming4j.");
      throw new org.gradle.api.GradleException(
          "Upcoming4j requires a Git repository. Initialize Git before applying this plugin.");
    }
  }

  private boolean isAGitProject(File rootDir) {
    var gitDir = new File(rootDir, ".git");
    return gitDir.exists();
  }
}
