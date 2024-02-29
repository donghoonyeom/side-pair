package sidepair.feed.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import sidepair.feed.domain.vo.FeedTagName;
import sidepair.feed.exception.FeedException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedTags {
    private static final int MAX_COUNT = 5;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @JoinColumn(name = "feed_id", updatable = false, nullable = false)
    @BatchSize(size = 20)
    private final Set<FeedTag> values = new HashSet<>();

    public FeedTags(final List<FeedTag> feedTags) {
        validate(feedTags);
        values.addAll(new HashSet<>(feedTags));
    }

    private void validate(final List<FeedTag> roadmapTags) {
        validateCount(roadmapTags);
        validateDuplicatedName(roadmapTags);
    }

    private void validateCount(final List<FeedTag> roadmapTags) {
        if (roadmapTags.size() > MAX_COUNT) {
            throw new FeedException(
                    String.format("태그의 개수는 최대 %d개까지 가능합니다.", MAX_COUNT));
        }
    }

    private void validateDuplicatedName(final List<FeedTag> feedTags) {
        final Set<FeedTagName> nonDuplicatedNames = feedTags.stream()
                .map(FeedTag::getName)
                .collect(Collectors.toSet());
        if (feedTags.size() != nonDuplicatedNames.size()) {
            throw new FeedException("태그 이름은 중복될 수 없습니다.");
        }
    }

    public void addAll(final FeedTags tags) {
        this.values.addAll(new HashSet<>(tags.values));
    }

    public Set<FeedTag> getValues() {
        return new HashSet<>(values);
    }
}
