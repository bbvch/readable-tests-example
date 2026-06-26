package blog.readabletests;

import blog.readabletests.model.*;
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

class BlogPostRankerTest3 {
  private static final Clock MORNING = Clock.fixed(LocalDateTime.parse("2026-01-01T09:00:00")
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant(),
                                                   ZoneId.systemDefault());
  private static final Clock EVENING = Clock.fixed(LocalDateTime.parse("2026-01-01T19:00:00")
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant(),
                                                   ZoneId.systemDefault());
  private static final List<Label> MUTED_LABELS =
      List.of(new Label("muted1", NEUTRAL), new Label("muted2", NEUTRAL));

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
  void ranking_should_prefer_posts_with_interesting_labels_if_user_has_interests() {
    var posts = List.of(POST1, POST1, POST2);
    preferences.setInterests(Set.of(new Label("this-is-what-i-want", NEUTRAL)));

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts.getFirst()
                          .metaData()
                          .labels())
        .map(Label::name)
        .contains("this-is-what-i-want");
  }

  @Test
  void ranking_should_prefer_posts_with_work_labels_in_the_morning() {
    var posts = List.of(POST3, POST4, POST5);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts
                   .getFirst()
                   .metaData()
                   .labels())
        .map(Label::name)
        .contains("work");
  }

  @Test
  void ranking_should_prefer_posts_with_leisure_labels_in_the_evening() {
    var posts = List.of(POST3, POST4, POST5);
    ranker = new BlogPostRanker(EVENING);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts
                   .getFirst()
                   .metaData()
                   .labels())
        .map(Label::name)
        .contains("leisure");
  }

  @Test
  void ranking_should_assign_higher_priority_to_user_interests_than_to_timebased_classification_if_user_has_interests() {
    var posts = List.of(POST6, POST7, POST8, POST9);
    preferences.setInterests(Set.of(new Label("interesting-leisure", LEISURE),
                                    new Label("interesting-work", WORK)));

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts.stream()
                          .map(post -> post.metaData().labels())
                          .flatMap(Set::stream)
                          .map(Label::name))
        .containsExactly("interesting-work",
                         "interesting-leisure",
                         "boring-work",
                         "boring-leisure");
  }

  @Test
  void ranking_should_return_all_posts_if_user_has_no_muted_labels() {
    var posts = List.of(POST1, POST2, POST3, POST4);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts).hasSameSizeAs(posts);
  }

  @Test
  void ranking_should_remove_posts_with_muted_labels_if_they_are_not_interesting() {
    var posts = List.of(POST1, POST3, POST10, POST8, POST11);
    preferences.setMuted(MUTED_LABELS);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts).hasSize(3)
                           .allSatisfy(post ->
                                           assertThat(post.metaData().labels())
                                               .map(Label::name)
                                               .doesNotContain("muted1", "muted2"));
  }

  @Test
  void ranking_should_keep_posts_with_muted_labels_if_the_same_label_is_interesting() {
    var conflicted = new Label("neutral", NEUTRAL);
    var posts = List.of(POST3);
    preferences.setInterests(Set.of(conflicted));
    preferences.setMuted(List.of(conflicted));

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts).hasSize(1);
  }
}