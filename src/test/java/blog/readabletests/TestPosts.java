package blog.readabletests;

import blog.readabletests.model.Author;
import blog.readabletests.model.BlogPost;
import blog.readabletests.model.BlogPostMetaData;
import blog.readabletests.model.Label;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static blog.readabletests.model.Label.Classification.*;

public class TestPosts {
  public static final Author SOME_AUTHOR = new Author("Some Author",
      "Born somewhere, Some got into writing after being bitten by a spider.");

  public static final BlogPost POST1 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Collections.emptySet()));
  public static final BlogPost POST2 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("this-is-what-i-want", NEUTRAL))));
  public static final BlogPost POST3 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("neutral", NEUTRAL))));
  public static final BlogPost POST4 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("leisure", LEISURE))));
  public static final BlogPost POST5 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("work", WORK))));
  public static final BlogPost POST6 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("boring-leisure", LEISURE))));
  public static final BlogPost POST7 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("boring-work", WORK))));
  public static final BlogPost POST8 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("interesting-leisure", LEISURE))));
  public static final BlogPost POST9 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("interesting-work", WORK))));
  public static final BlogPost POST10 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("muted1", NEUTRAL))));
  public static final BlogPost POST11 = new BlogPost("Some topic",
      "This is a generic blog post.",
      new BlogPostMetaData(Instant.EPOCH,
          SOME_AUTHOR,
          Set.of(new Label("l5", NEUTRAL), new Label("muted2", NEUTRAL))));
}
