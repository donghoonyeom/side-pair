package sidepair.domain.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseUpdatedTimeEntity;
import sidepair.domain.feed.exception.FeedException;
import sidepair.domain.member.Member;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedApplicant extends BaseUpdatedTimeEntity {

    private static final int CONTENT_MAX_LENGTH = 1000;

    @Column(length = 1200)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    public FeedApplicant(final String content, final Member member) {
        if (content != null) {
            validate(content);
        }
        this.content = content;
        this.member = member;
    }

    private void validate(final String content) {
        validateContentLength(content);
    }

    private void validateContentLength(final String content) {
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new FeedException(String.format("신청서는 최대 %d글자까지 입력할 수 있습니다.", CONTENT_MAX_LENGTH));
        }
    }

    public void updateFeed(final Feed feed) {
        if (this.feed == null) {
            this.feed = feed;
        }
    }

    public boolean isNotSameFeed(final Feed feed) {
        return this.feed == null || !this.feed.equals(feed);
    }

    public String getContent() {
        return content;
    }

    public Member getMember() {
        return member;
    }
}
