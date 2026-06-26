package blog.readabletests;

import blog.readabletests.model.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static blog.readabletests.model.Label.Classification.*;
import static org.assertj.core.api.Assertions.assertThat;

class BlogPostRankerTest4 {
  private static final Clock MORNING = Clock.fixed(LocalDateTime.parse("2026-01-01T09:00:00")
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant(),
                                                   ZoneId.systemDefault());
  private static final Clock EVENING = Clock.fixed(LocalDateTime.parse("2026-01-01T19:00:00")
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant(),
                                                   ZoneId.systemDefault());
  public static final Author SOME_AUTHOR = new Author("Some Author",
                                                      "Born somewhere, Some got into writing after being bitten by a spider.");

  @Test
  void ranking_should_prefer_posts_with_interesting_labels_if_user_has_interests() {
    var posts = List.of(
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH, SOME_AUTHOR, Collections.emptySet())),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH, SOME_AUTHOR, Collections.emptySet())),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("this-is-what-i-want", NEUTRAL))))
    );
    var preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    preferences.setInterests(Set.of(new Label("this-is-what-i-want", NEUTRAL)));
    var user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);
    BlogPostRanker ranker = new BlogPostRanker(MORNING);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts.getFirst()
                          .metaData()
                          .labels())
        .map(Label::name)
        .contains("this-is-what-i-want");
  }

  @Test
  void ranking_should_prefer_posts_with_work_labels_in_the_morning() {
    var posts = List.of(
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("neutral", NEUTRAL)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("leisure", LEISURE)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("work", WORK))))
    );
    var preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    var user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);
    BlogPostRanker ranker = new BlogPostRanker(MORNING);

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
    var posts = List.of(
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("neutral", NEUTRAL)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("leisure", LEISURE)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("work", WORK))))
    );
    var preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    var user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);
    BlogPostRanker ranker = new BlogPostRanker(EVENING);

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
    var interestingLeisureLabel = new Label("interesting-leisure", LEISURE);
    var interestingWorkLabel = new Label("interesting-work", WORK);
    var posts = List.of(
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("boring-leisure", LEISURE)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("boring-work", WORK)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(interestingLeisureLabel))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH, SOME_AUTHOR, Set.of(interestingWorkLabel)))
    );
    BlogPostRanker ranker = new BlogPostRanker(MORNING);

    var preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    preferences.setInterests(Set.of(interestingLeisureLabel, interestingWorkLabel));
    var user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);

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
    var posts = List.of(
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH, SOME_AUTHOR, Collections.emptySet())),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("l1", NEUTRAL)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("l3", NEUTRAL),
                                                 new Label("l4", NEUTRAL))))
    );
    var preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    preferences.setMuted(List.of());
    var user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);
    BlogPostRanker ranker = new BlogPostRanker(MORNING);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts).hasSameSizeAs(posts);
  }

  @Test
  void ranking_should_remove_posts_with_muted_labels_if_they_are_not_interesting() {
    var muted1 = new Label("muted1", NEUTRAL);
    var muted2 = new Label("muted2", NEUTRAL);
    var posts = List.of(
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH, SOME_AUTHOR, Collections.emptySet())),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("l1", NEUTRAL)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH, SOME_AUTHOR, Set.of(muted1))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("l3", NEUTRAL),
                                                 new Label("l4", NEUTRAL)))),
        new BlogPost("Some topic",
                     "This is a generic blog post.",
                     new BlogPostMetaData(Instant.EPOCH,
                                          SOME_AUTHOR,
                                          Set.of(new Label("l5", NEUTRAL), muted2)))
    );
    var preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    preferences.setMuted(List.of(muted1, muted2));
    var user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);
    BlogPostRanker ranker = new BlogPostRanker(MORNING);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts).hasSize(3)
                           .allSatisfy(post ->
                                           assertThat(post.metaData().labels())
                                               .map(Label::name)
                                               .doesNotContain(muted1.name(), muted2.name()));
  }

  @Test
  void ranking_should_keep_posts_with_muted_labels_if_the_same_label_is_interesting() {
    var conflicted = new Label("conflicted", NEUTRAL);
    var posts = List.of(new BlogPost("Some topic",
                                     "This is a generic blog post.",
                                     new BlogPostMetaData(Instant.EPOCH,
                                                          SOME_AUTHOR,
                                                          Set.of(conflicted))));
    var preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    preferences.setInterests(Set.of(conflicted));
    preferences.setMuted(List.of(conflicted));
    var user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);
    BlogPostRanker ranker = new BlogPostRanker(MORNING);

    var rankedPosts = ranker.rankPosts(posts, user);

    assertThat(rankedPosts).hasSize(1);
  }
}