package sidepair.feed.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedContents {
    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            mappedBy = "feed")
    @Column(nullable = false)
    private final List<FeedContent> values = new ArrayList<>();

    public FeedContents(final List<FeedContent> contents) {
        this.values.addAll(contents);
    }

    public void add(final FeedContent content) {
        this.values.add(content);
    }

    public Optional<FeedContent> findLastRoadmapContent() {
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.get(values.size() - 1));
    }

    public List<FeedContent> getValues() {
        return values;
    }
}
