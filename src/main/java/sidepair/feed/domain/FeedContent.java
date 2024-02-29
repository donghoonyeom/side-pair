package sidepair.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sidepair.feed.exception.FeedException;
import sidepair.global.domain.BaseUpdatedTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FeedContent extends BaseUpdatedTimeEntity {
    private static final int CONTENT_MAX_LENGTH = 2000;

    @Column(length = 2200)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Embedded
    private final FeedNodes nodes = new FeedNodes();

    public FeedContent(final String content) {
        this(null, content);
    }

    public FeedContent(final Long id, final String content) {
        validate(content);
        this.id = id;
        this.content = content;
    }

    private void validate(final String content) {
        if (content == null) {
            return;
        }
        validateContentLength(content);
    }

    private void validateContentLength(final String content) {
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new FeedException(String.format("개시물 본문의 길이는 최대 %d글자입니다.", CONTENT_MAX_LENGTH));
        }
    }

    public void addNodes(final FeedNodes nodes) {
        this.nodes.addAll(nodes);
        nodes.updateAllFeedContent(this);
    }

    public boolean isNotSameFeed(final Feed feed) {
        return this.feed == null || !this.feed.equals(feed);
    }

    public void updateFeed(final Feed feed) {
        if (this.feed == null) {
            this.feed = feed;
        }
    }

    public int nodesSize() {
        return nodes.size();
    }

    public Optional<FeedNode> findRoadmapNodeById(final Long id) {
        return nodes.findById(id);
    }

    public Optional<FeedNode> findRoadmapNodeByTitle(final String title) {
        return nodes.findByTitle(title);
    }

    public String getContent() {
        return content;
    }

    public FeedNodes getNodes() {
        return nodes;
    }

    public Feed getFeed() {
        return feed;
    }
}
