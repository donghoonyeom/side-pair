package sidepair.domain.feed;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import sidepair.domain.feed.exception.FeedException;

@Embeddable
@NoArgsConstructor
public class FeedNodeImages {
    private static final int MAX_SIZE = 2;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "feed_node_id", nullable = false, updatable = false)
    private final List<FeedNodeImage> values = new ArrayList<>();

    public FeedNodeImages(final List<FeedNodeImage> images) {
        validateSize(images);
        this.values.addAll(new ArrayList<>(images));
    }

    private void validateSize(final List<FeedNodeImage> images) {
        if (images.size() > MAX_SIZE) {
            throw new FeedException("하나의 노드에 사진은 최대 2개까지 가능합니다.");
        }
    }

    public void addAll(final FeedNodeImages images) {
        this.values.addAll(new ArrayList<>(images.values));
        validateSize(this.values);
    }

    public void add(final FeedNodeImage feedNodeImage) {
        this.values.add(feedNodeImage);
        validateSize(this.values);
    }

    public List<FeedNodeImage> getValues() {
        return new ArrayList<>(values);
    }
}
