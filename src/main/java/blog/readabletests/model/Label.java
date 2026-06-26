package blog.readabletests.model;

public record Label(String name, Classification classification) {
    public enum Classification {
        WORK,
        LEISURE,
        NEUTRAL
    }
}
