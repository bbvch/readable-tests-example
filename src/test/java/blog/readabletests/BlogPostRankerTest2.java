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

class BlogPostRankerTest2 {
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
  void ranking_with_interest() {
    var posts = List.of(POST1, POST2, POST6, POST7, POST8, POST9);
    preferences.setInterests(Set.of(new Label("this-is-what-i-want", NEUTRAL)));

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts.getFirst()
                          .metaData()
                          .labels())
        .map(Label::name)
        .contains("this-is-what-i-want");
  }

  @Test
  void ranking_work() {
    var posts = List.of(POST1, POST2, POST3, POST4, POST5);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts
                   .getFirst()
                   .metaData()
                   .labels())
        .map(Label::name)
        .contains("work");
  }

  @Test
  void ranking_leisure() {
    var posts = List.of(POST1, POST2, POST3, POST4, POST5);
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
  void ranking_interest_work_leisure() {
    var posts = List.of(POST1, POST6, POST7, POST8, POST9); // => given some posts
    preferences.setInterests(Set.of(new Label("interesting-leisure", LEISURE),
                                    new Label("interesting-work", WORK))); // => and some interests in work and leisure labels

    var rankedPosts = ranker.rankPosts(posts, user); // => ranking

    assertThat(rankedPosts.stream()
                          .map(post -> post.metaData().labels())
                          .flatMap(Set::stream)
                          .map(Label::name))
        .containsExactly("interesting-work",
                         "interesting-leisure",
                         "boring-work",
                         "boring-leisure"); // => should order posts by interest, then time
  }

  @Test
  void ranking_count_posts() {
    var posts = List.of(POST1, POST2, POST3, POST4, POST5); // => given some posts and default settings

    var rankedPosts = ranker.rankPosts(posts, user); // => ranking

    assertThat(rankedPosts).hasSize(5); // => should not lose any posts
  }

  @Test
  void ranking_muted() {
    var posts = List.of(POST1, POST3, POST10, POST8, POST11);
    preferences.setMuted(MUTED_LABELS); // => if they are muted

    var rankedPosts = ranker.rankPosts(posts, user); // => ranking

    assertThat(rankedPosts).hasSize(3) // => should filter out labels
                           .allSatisfy(post ->
                                           assertThat(post.metaData().labels())
                                               .map(Label::name)
                                               .doesNotContain("muted1", "muted2"));
  }

  @Test
  void ranking_conflicted() {
    var conflicted = new Label("neutral", NEUTRAL);
    var posts = List.of(POST3);
    preferences.setInterests(Set.of(conflicted));
    preferences.setMuted(List.of(conflicted));

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts).hasSize(1);
  }
}