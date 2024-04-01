package sidepair.domain.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.feed.exception.FeedException;
import sidepair.domain.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedNode extends BaseEntity {
    private static final int TITLE_MIN_LENGTH = 1;
    private static final int TITLE_MAX_LENGTH = 40;
    private static final int CONTENT_MIN_LENGTH = 1;
    private static final int CONTENT_MAX_LENGTH = 2000;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 2200, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_content_id", nullable = false)
    private FeedContent feedContent;

    @Embedded
    private final FeedNodeImages feedNodeImages = new FeedNodeImages();

    public FeedNode(final String title, final String content) {
        this(null, title, content);
    }

    public FeedNode(final Long id, final String title, final String content) {
        validate(title, content);
        this.id = id;
        this.title = title;
        this.content = content;
    }

    private void validate(final String title, final String content) {
        validateTitleLength(title);
        validateContentLength(content);
    }

    private void validateTitleLength(final String title) {
        if (title.length() < TITLE_MIN_LENGTH || title.length() > TITLE_MAX_LENGTH) {
            throw new FeedException(
                    String.format("피드 노드의 제목의 길이는 최소 %d글자, 최대 %d글자입니다.", TITLE_MIN_LENGTH, TITLE_MAX_LENGTH));
        }
    }

    private void validateContentLength(final String content) {
        if (content.length() < CONTENT_MIN_LENGTH || content.length() > CONTENT_MAX_LENGTH) {
            throw new FeedException(
                    String.format("피드 노드의 설명의 길이는 최소 %d글자, 최대 %d글자입니다.", CONTENT_MIN_LENGTH, CONTENT_MAX_LENGTH));
        }
    }

    public void addImages(final FeedNodeImages feedNodeImages) {
        this.feedNodeImages.addAll(feedNodeImages);
    }

    public boolean isNotSameFeedContent(final FeedContent feedContent) {
        return this.feedContent == null || !this.feedContent.equals(feedContent);
    }

    public void updateFeedContent(final FeedContent feedContent) {
        if (this.feedContent == null) {
            this.feedContent = feedContent;
        }
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public FeedContent getFeedContent() {
        return feedContent;
    }

    public FeedNodeImages getFeedNodeImages() {
        return feedNodeImages;
    }
}
