package blog.readabletests;

import blog.readabletests.model.Label;
import blog.readabletests.model.Preferences;
import blog.readabletests.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static blog.readabletests.TestPosts.*;
import static blog.readabletests.model.Label.Classification.*;
import static org.assertj.core.api.Assertions.assertThat;

class BlogPostRankerTest1 {
  private static final Clock MORNING = Clock.fixed(LocalDateTime.parse("2026-01-01T09:00:00")
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant(),
                                                   ZoneId.systemDefault());
  private static final Clock EVENING = Clock.fixed(LocalDateTime.parse("2026-01-01T19:00:00")
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant(),
                                                   ZoneId.systemDefault());
  private static final List<Label> MUTED_LABELS = List.of(new Label("muted1", NEUTRAL),
                                                          new Label("muted2", NEUTRAL),
                                                          new Label("neutral", NEUTRAL));

  private User user;
  private Preferences preferences;
  private BlogPostRanker ranker;

  @BeforeEach
  void setUp() {
    preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);
    ranker = new BlogPostRanker(MORNING);
  }

  @Test
  void rankPosts() {
    // Arrange
    var posts = List.of(POST1, POST2, POST3, POST4, POST5);

    // Act
    var rankedPosts = ranker.rankPosts(posts, user);

    // Assert
    assertThat(rankedPosts).hasSize(5);
    assertThat(rankedPosts
                   .getFirst()
                   .metaData()
                   .labels())
        .map(Label::name)
        .contains("work");

    // Arrange
    var eveningRanker = new BlogPostRanker(EVENING);

    // Act
    rankedPosts = eveningRanker.rankPosts(posts, user);

    // Assert
    assertThat(rankedPosts
                   .getFirst()
                   .metaData()
                   .labels())
        .map(Label::name)
        .contains("leisure");

    // Arrange
    posts = List.of(POST1, POST2, POST6, POST7, POST8, POST9);
    preferences.setInterests(Set.of(new Label("this-is-what-i-want", NEUTRAL),
                                    new Label("interesting-leisure", LEISURE),
                                    new Label("interesting-work", WORK)));

    // Act
    rankedPosts = ranker.rankPosts(posts, user);

    // Assert
    assertThat(rankedPosts).hasSize(6);
    assertThat(rankedPosts.stream()
                          .map(post -> post.metaData().labels())
                          .flatMap(Set::stream)
                          .map(Label::name))
        .containsExactly("interesting-work",
                         "this-is-what-i-want",
                         "interesting-leisure",
                         "boring-work",
                         "boring-leisure");
  }

  @Test
  void rankPosts_muted() {
    // Arrange
    var posts = List.of(POST1, POST3, POST10, POST8, POST11);
    preferences.setMuted(MUTED_LABELS);
    preferences.setInterests(Set.of(new Label("neutral", NEUTRAL)));

    // Act
    var rankedPosts = ranker.rankPosts(posts, user);

    // Assert
    assertThat(rankedPosts)
        .hasSize(3)
        .allSatisfy(post ->
                        assertThat(post.metaData().labels())
                            .map(Label::name)
                            .doesNotContain("muted1", "muted2"));
  }
}