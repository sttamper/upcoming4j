package io.stamperlabs.upcoming4j

import io.stamperlabs.upcoming4j.service.CommitHistorySinceTagService
import io.stamperlabs.upcoming4j.service.LatestCreatedTagService
import io.stamperlabs.upcoming4j.service.NextSemanticVersionService
import org.gradle.api.Plugin
import org.gradle.api.Project

class Upcoming4jPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.logger.lifecycle("Initializing Upcoming4j Plugin")
        this.checkGitProject(project);
        project.ext.computeNextVersion = {
            def latestCreatedTagService = new LatestCreatedTagService(project)
            def currentTag = latestCreatedTagService.retrieve()
            def commitHistorySinceTagService = new CommitHistorySinceTagService(project)
            def commits = commitHistorySinceTagService.retrieve(currentTag)
            def nextSemanticVersionService = new NextSemanticVersionService(project)
            def nextSemVer = nextSemanticVersionService.compute(commits, currentTag)
            return nextSemVer
        }
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