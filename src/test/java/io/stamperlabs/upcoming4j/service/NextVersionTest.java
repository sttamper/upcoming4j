package io.stamperlabs.upcoming4j.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.stamperlabs.upcoming4j.exception.Upcoming4jException;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NextVersionTest {

  @Mock private Project project;

  @Mock private Logger logger;

  private NextVersion nextVersion;

  @BeforeEach
  void setUp() {
    when(project.getLogger()).thenReturn(logger);
    nextVersion = new NextVersion(project);
  }

  @Test
  void shouldReturnSameVersionWhenNoCommits() {
    String result = nextVersion.compute("1.2.3", List.of());

    assertEquals("1.2.3", result);
  }

  @Test
  void shouldReturnSameVersionWhenCommitHistoryIsNull() {
    String result = nextVersion.compute("1.2.3", null);

    assertEquals("1.2.3", result);
  }

  @Test
  void shouldBumpPatchVersionOnFixCommit() {
    String result = nextVersion.compute("1.2.3", List.of("fix: correct typo"));

    assertEquals("1.2.4", result);
  }

  @Test
  void shouldBumpMinorVersionOnFeatCommit() {
    String result = nextVersion.compute("1.2.3", List.of("feat: add new endpoint"));

    assertEquals("1.3.0", result);
  }

  @Test
  void shouldBumpMajorVersionOnBreakingChangeCommit() {
    String result = nextVersion.compute("1.2.3", List.of("feat!: change API contract"));

    assertEquals("2.0.0", result);
  }

  @Test
  void shouldPrioritizeMajorBumpOverMinorAndPatch() {
    String result =
        nextVersion.compute(
            "1.2.3",
            List.of(
                "fix: small bug", "feat: new feature", "BREAKING CHANGE: removed deprecated API"));

    assertEquals("2.0.0", result);
  }

  @Test
  void shouldNormalizeTagStartingWithV() {
    String result = nextVersion.compute("v1.2.3", List.of("fix: bug"));

    assertEquals("1.2.4", result);
  }

  @Test
  void shouldThrowExceptionWhenTagIsNotSemanticVersion() {
    assertThrows(Upcoming4jException.class, () -> nextVersion.compute("v1.2", List.of("fix: bug")));
  }
}
