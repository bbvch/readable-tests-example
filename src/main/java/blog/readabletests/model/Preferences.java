package blog.readabletests.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Preferences {
    private boolean chronologicalTimeline;
    private Set<Label> interests;
    private Set<Label> muted;

    public void setChronologicalTimeline(boolean chronologicalTimeline) {
        this.chronologicalTimeline = chronologicalTimeline;
    }

    public boolean isChronologicalTimeline() {
        return chronologicalTimeline;
    }

    public void setInterests(Collection<Label> interests) {
        this.interests = new HashSet<>(interests);
    }

    public Set<Label> getInterests() {
        return interests;
    }

    public void setMuted(Collection<Label> muted) {
        this.muted = new HashSet<>(muted);
    }

    public Set<Label> getMuted() {
        return muted;
    }
}
