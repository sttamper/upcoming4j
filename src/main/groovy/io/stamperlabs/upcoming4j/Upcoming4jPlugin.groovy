package io.stamperlabs.upcoming4j


import io.stamperlabs.upcoming4j.service.CommitHistorySinceTagService
import io.stamperlabs.upcoming4j.service.LatestCreatedTagService
import org.gradle.api.Plugin
import org.gradle.api.Project

class Upcoming4jPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def log = project.logger

        project.ext.computeNextVersion = {
            def latestCreatedTagService = new LatestCreatedTagService(project)
            def currentTag = latestCreatedTagService.retrieve()
            def commitHistorySinceTagService = new CommitHistorySinceTagService(project)
            def commits = commitHistorySinceTagService.retrieve(currentTag)

            if (!currentTag) {
                log.lifecycle("No current tag provided. Using default 0.0.0")
                currentTag = "0.0.0"
            }

            currentTag = currentTag.startsWith('v') ? currentTag.substring(1) : currentTag

            if (!commits || commits.isEmpty()) {
                log.lifecycle("No commits to evaluate since '${currentTag}'. Version remains the same.")
                return currentTag
            }

            def matcher = currentTag =~ /(\d+)\.(\d+)\.(\d+)/
            if (!matcher) throw new IllegalArgumentException("Current tag '${currentTag}' is not semantic version format (X.Y.Z)")

            int major = matcher[0][1] as int
            int minor = matcher[0][2] as int
            int patch = matcher[0][3] as int

            boolean majorBump = false, minorBump = false, patchBump = false
            commits.each { msg ->
                def lower = msg.toLowerCase()
                if (lower.contains("breaking change")) majorBump = true
                else if (lower.startsWith("feat")) minorBump = true
                else if (lower.startsWith("fix")) patchBump = true
            }

            if (majorBump) { major += 1; minor = 0; patch = 0 }
            else if (minorBump) { minor += 1; patch = 0 }
            else if (patchBump) { patch += 1 }
            else log.lifecycle("No conventional commits detected. Version remains the same.")

            def nextVersion = "${major}.${minor}.${patch}"
            log.lifecycle("Computed next version: ${nextVersion}")
            return nextVersion
        }
    }
}