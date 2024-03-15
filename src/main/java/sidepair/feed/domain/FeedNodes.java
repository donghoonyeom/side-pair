package sidepair.feed.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.feed.exception.FeedException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedNodes {

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            mappedBy = "feedContent")
    private final List<FeedNode> values = new ArrayList<>();

    public FeedNodes(final List<FeedNode> feedNodes) {
        validateTitleDistinct(feedNodes);
        this.values.addAll(new ArrayList<>(feedNodes));
    }

    private void validateTitleDistinct(final List<FeedNode> feedNodes) {
        final int distinctNameCount = feedNodes.stream()
                .map(FeedNode::getTitle)
                .collect(Collectors.toSet())
                .size();
        if (feedNodes.size() != distinctNameCount) {
            throw new FeedException("한 피드에 같은 이름의 노드가 존재할 수 없습니다.");
        }
    }

    public void add(final FeedNode feedNode) {
        this.values.add(feedNode);
        validateTitleDistinct(values);
    }

    public void addAll(final FeedNodes feedNodes) {
        this.values.addAll(new ArrayList<>(feedNodes.values));
        validateTitleDistinct(values);
    }

    public void updateAllFeedContent(final FeedContent content) {
        for (final FeedNode feedNode : values) {
            updateFeedContent(feedNode, content);
        }
    }

    private void updateFeedContent(final FeedNode feedNode, final FeedContent content) {
        if (feedNode.isNotSameFeedContent(content)) {
            feedNode.updateFeedContent(content);
        }
    }

    public Optional<FeedNode> findById(final Long feedNodeId) {
        return values.stream()
                .filter(it -> it.getId().equals(feedNodeId))
                .findAny();
    }

    public Optional<FeedNode> findByTitle(final String title) {
        return values.stream()
                .filter(it -> it.getTitle().equals(title))
                .findAny();
    }

    public int size() {
        return values.size();
    }

    public List<FeedNode> getValues() {
        return new ArrayList<>(values);
    }
}
