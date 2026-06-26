package blog.readabletests;

import blog.readabletests.model.BlogPost;
import blog.readabletests.model.Label;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;
import java.util.stream.Collectors;

public class BlogPostAssert extends AbstractAssert<BlogPostAssert, BlogPost> {
  public static BlogPostAssert assertThat(BlogPost blogPost) {
    return new BlogPostAssert(blogPost);
  }

  private BlogPostAssert(BlogPost blogPost) {
    super(blogPost, BlogPostAssert.class);
  }

  public BlogPostAssert hasLabel(String expectedLabel) {
    isNotNull();
    var labels = actual.metaData().labels();
    if (labels.isEmpty()) {
      failWithMessage("Expected blog post to have label '%s' but it had none", expectedLabel);
    }
    if (labels.stream().noneMatch(l -> Objects.equals(l.name(), expectedLabel))) {
      failWithMessage("Expected blog post to have label '%s' but it only had %s",
                      expectedLabel,
                      labels.stream().map(Label::name).collect(Collectors.joining("', '", "'", "'"))
      );
    }
    return this;
  }

  public BlogPostAssert doesNotHaveLabel(String notExpectedLabel) {
    isNotNull();
    if (actual.metaData()
              .labels()
              .stream()
              .anyMatch(l -> Objects.equals(l.name(), notExpectedLabel))) {
      failWithMessage("Expected blog post without label '%s' but label is present",
                      notExpectedLabel);
    }
    return this;
  }
}
