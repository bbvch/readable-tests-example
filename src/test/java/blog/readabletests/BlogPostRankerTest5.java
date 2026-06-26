package blog.readabletests;

import blog.readabletests.model.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static blog.readabletests.model.Label.Classification.*;
import static org.assertj.core.api.Assertions.assertThat;

class BlogPostRankerTest5 {
  private static final Clock MORNING = Clock.fixed(LocalDateTime.parse("2026-01-01T09:00:00")
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant(),
                                                   ZoneId.systemDefault());
  private static final Clock EVENING = Clock.fixed(LocalDateTime.parse("2026-01-01T19:00:00")
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant(),
                                                   ZoneId.systemDefault());

  @Test
  void ranking_should_prefer_posts_with_interesting_labels_if_user_has_interests() {
    var interestingPost = somePost(label("this-is-what-i-want"));
    var posts = List.of(
        somePost(),
        somePost(),
        interestingPost
    );

    var rankedPosts = ranker().rankPosts(posts, userWithInterestIn(label("this-is-what-i-want")));

    assertThat(rankedPosts.getFirst()
                          .metaData()
                          .labels())
        .map(Label::name)
        .contains("this-is-what-i-want");
  }

  @Test
  void ranking_should_prefer_posts_with_work_labels_in_the_morning() {
    var posts = List.of(
        somePost(label("neutral", NEUTRAL)),
        somePost(label("leisure", LEISURE)),
        somePost(label("work", WORK))
    );

    var rankedPosts = ranker(MORNING).rankPosts(posts, userWithDefaultSettings());

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
        somePost(label("neutral", NEUTRAL)),
        somePost(label("leisure", LEISURE)),
        somePost(label("work", WORK))
    );

    var rankedPosts = ranker(EVENING).rankPosts(posts, userWithDefaultSettings());

    assertThat(rankedPosts.getFirst()
                          .metaData()
                          .labels())
        .map(Label::name)
        .contains("leisure");
  }

  @Test
  void ranking_should_assign_higher_priority_to_user_interests_than_to_timebased_classification_if_user_has_interests() {
    var interestingLeisureLabel = label("interesting-leisure", LEISURE);
    var interestingWorkLabel = label("interesting-work", WORK);
    var posts = List.of(
        somePost(label("boring-leisure", LEISURE)),
        somePost(label("boring-work", WORK)),
        somePost(interestingLeisureLabel),
        somePost(interestingWorkLabel)
    );

    var rankedPosts = ranker(MORNING).rankPosts(posts,
                                                userWithInterestIn(interestingLeisureLabel,
                                                                   interestingWorkLabel));

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
        somePost(),
        somePost(label("l1")),
        somePost(label("l3"), label("l4"))
    );
    var user = userWithDefaultSettings();
    user.getPreferences().setMuted(List.of());

    var rankedPosts = ranker().rankPosts(posts, user);

    assertThat(rankedPosts).hasSameSizeAs(posts);
  }

  @Test
  void ranking_should_remove_posts_with_muted_labels_if_they_are_not_interesting() {
    var muted1 = label("muted1");
    var muted2 = label("muted2");
    var posts = List.of(
        somePost(),
        somePost(label("l1")),
        somePost(muted1),
        somePost(label("l3"), label("l4")),
        somePost(label("l5"), muted2)
    );
    var user = userWithDefaultSettings();
    user.getPreferences().setMuted(List.of(muted1, muted2));

    var rankedPosts = ranker().rankPosts(posts, user);

    assertThat(rankedPosts).hasSize(3)
                           .allSatisfy(post ->
                                           assertThat(post.metaData().labels())
                                               .map(Label::name)
                                               .doesNotContain(muted1.name(), muted2.name()));
  }

  @Test
  void ranking_should_keep_posts_with_muted_labels_if_the_same_label_is_interesting() {
    var conflicted = label("conflicted");
    var posts = List.of(somePost(conflicted));
    var user = userWithInterestIn(conflicted);
    user.getPreferences().setMuted(List.of(conflicted));

    var rankedPosts = ranker().rankPosts(posts, user);

    assertThat(rankedPosts).hasSize(1);
  }

  private BlogPost somePost(Label... labels) {
    return new BlogPost("Some topic",
                        "This is a generic blog post.",
                        new BlogPostMetaData(Instant.EPOCH,
                                             new Author("Some Author",
                                                        "Born somewhere, Some got into writing after being bitten by a spider."),
                                             Arrays.stream(labels).collect(Collectors.toSet())));
  }

  private BlogPostRanker ranker() {
    return ranker(MORNING);
  }

  private BlogPostRanker ranker(Clock clock) {
    return new BlogPostRanker(clock);
  }

  private Label label(String name, Label.Classification classification) {
    return new Label(name, classification);
  }

  private Label label(String name) {
    return new Label(name, NEUTRAL);
  }

  private User userWithDefaultSettings() {
    var preferences = new Preferences();
    preferences.setChronologicalTimeline(false);
    var user = new User();
    user.setName("Lazy Defaulter");
    user.setPreferences(preferences);
    return user;
  }

  private User userWithInterestIn(Label... interests) {
    var user = userWithDefaultSettings();
    user.getPreferences().setInterests(Arrays.stream(interests).collect(Collectors.toSet()));
    return user;
  }
}