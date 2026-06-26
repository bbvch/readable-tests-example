package blog.readabletests;

import blog.readabletests.model.BlogPost;
import blog.readabletests.model.Label;
import blog.readabletests.model.User;

import java.time.Clock;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;

import static blog.readabletests.model.Label.Classification.LEISURE;
import static blog.readabletests.model.Label.Classification.WORK;

public class BlogPostRanker {
    private static final LocalTime EVENING_START = LocalTime.of(18, 0);
    private final Clock clock;

    public BlogPostRanker(Clock clock) {
        this.clock = clock;
    }

    public List<BlogPost> rankPosts(List<BlogPost> posts, User user) {
        var preferredLabelClassification = LocalTime.now(clock).isBefore(EVENING_START) ? WORK : LEISURE;
        var interests = Optional.ofNullable(user.getPreferences().getInterests()).orElse(Collections.emptySet());
        return posts.stream()
                    .sorted(matchingLabelComparator(label -> label.classification() == preferredLabelClassification))
                    .sorted(matchingLabelComparator(interests::contains))
                    .filter(notMuted(user))
                    .toList();
    }

    private Comparator<BlogPost> matchingLabelComparator(Predicate<Label> predicate) {
        return (p1, p2) -> {
            var preferP1 = p1.metaData().labels().stream().anyMatch(predicate);
            var preferP2 = p2.metaData().labels().stream().anyMatch(predicate);
            return preferP1 ? (preferP2 ? 0 : -1) : (preferP2 ? 1 : 0);
        };
    }

    private Predicate<BlogPost> notMuted(User user) {
        var muted = user.getPreferences().getMuted();
        if (muted == null || muted.isEmpty()) {
            return _ -> true;
        }
        var effectiveMuted = new HashSet<>(muted);
        if (user.getPreferences().getInterests() != null) {
            effectiveMuted.removeAll(user.getPreferences().getInterests());
        }
        return post -> post.metaData()
                           .labels()
                           .stream()
                           .noneMatch(effectiveMuted::contains);
    }
}
