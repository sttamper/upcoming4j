package io.stamperlabs.upcoming4j;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Upcoming4jPluginTest {

  @TempDir File tempDir;

  @Test
  void apply_registersNextVersionProvider() {
    // given: git repo marker
    File gitDir = new File(tempDir, ".git");
    assertTrue(gitDir.mkdir());

    Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();

    Upcoming4jPlugin plugin = new Upcoming4jPlugin();

    // when
    plugin.apply(project);

    // then
    Object value = project.getExtensions().getExtraProperties().get("nextVersion");

    assertNotNull(value);
    assertTrue(value instanceof Provider);
  }

  @Test
  void provider_throwsException_whenProjectIsNotGitRepository() {
    Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();

    Upcoming4jPlugin plugin = new Upcoming4jPlugin();
    plugin.apply(project);

    Object value = project.getExtensions().getExtraProperties().get("nextVersion");

    assertTrue(value instanceof Provider);

    @SuppressWarnings("unchecked")
    Provider<String> provider = (Provider<String>) value;

    assertThrows(GradleException.class, provider::get);
  }
}
