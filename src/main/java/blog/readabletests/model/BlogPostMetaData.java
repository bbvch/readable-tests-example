package blog.readabletests.model;

import java.time.Instant;
import java.util.Set;

public record BlogPostMetaData(Instant publishedAt, Author author, Set<Label> labels) {
}
