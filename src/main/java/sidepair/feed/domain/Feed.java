package sidepair.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sidepair.feed.exception.FeedException;
import sidepair.global.domain.BaseCreatedTimeEntity;
import sidepair.member.domain.Member;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Feed extends BaseCreatedTimeEntity {
    private static final int TITLE_MIN_LENGTH = 1;
    private static final int TITLE_MAX_LENGTH = 40;
    private static final int INTRODUCTION_MIN_LENGTH = 1;
    private static final int INTRODUCTION_MAX_LENGTH = 150;
    private static final int REQUIRED_MIN_PERIOD = 0;
    private static final int REQUIRED_MAX_PERIOD = 1000;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 200, nullable = false)
    private String introduction;

    @Column(nullable = false)
    private Integer requiredPeriod;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 10, nullable = false)
    private FeedStatus status = FeedStatus.RECRUITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private FeedCategory category;

    @Embedded
    private FeedContents contents = new FeedContents();

    @Embedded
    private FeedTags tags = new FeedTags();

    public Feed(final String title, final String introduction, final int requiredPeriod,
                final Member creator, final FeedCategory category) {
        this(null, title, introduction, requiredPeriod, FeedStatus.RECRUITING, creator, category);
    }

    public Feed(final String title, final String introduction, final Integer requiredPeriod,
                final FeedStatus status, final Member creator, final FeedCategory category) {
        this(null, title, introduction, requiredPeriod, status, creator, category);
    }

    public Feed(final Long id, final String title, final String introduction, final Integer requiredPeriod,
                final Member creator, final FeedCategory category) {
        this(id, title, introduction, requiredPeriod, FeedStatus.RECRUITING, creator, category);
    }

    public Feed(final Long id, final String title, final String introduction, final Integer requiredPeriod,
                final FeedStatus status, final Member creator, final FeedCategory category) {
        validate(title, introduction, requiredPeriod);
        this.id = id;
        this.title = title;
        this.introduction = introduction;
        this.requiredPeriod = requiredPeriod;
        this.status = status;
        this.creator = creator;
        this.category = category;
    }

    private void validate(final String title, final String introduction, final int requiredPeriod) {
        validateTitleLength(title);
        validateIntroductionLength(introduction);
        validateRequiredPeriod(requiredPeriod);
    }

    private void validateTitleLength(final String title) {
        if (title.length() < TITLE_MIN_LENGTH || title.length() > TITLE_MAX_LENGTH) {
            throw new FeedException(
                    String.format("개시물 제목의 길이는 최소 %d글자, 최대 %d글자입니다.", TITLE_MIN_LENGTH, TITLE_MAX_LENGTH)
            );
        }
    }

    private void validateIntroductionLength(final String introduction) {
        if (introduction.length() < INTRODUCTION_MIN_LENGTH || introduction.length() > INTRODUCTION_MAX_LENGTH) {
            throw new FeedException(
                    String.format("개시물 소개글의 길이는 최소 %d글자, 최대 %d글자입니다.",
                            INTRODUCTION_MIN_LENGTH, INTRODUCTION_MAX_LENGTH
                    )
            );
        }
    }

    private void validateRequiredPeriod(final int requiredPeriod) {
        if (requiredPeriod < REQUIRED_MIN_PERIOD || requiredPeriod > REQUIRED_MAX_PERIOD) {
            throw new FeedException(
                    String.format("개시물 추천 소요 기간은 최소 %d일, 최대 %d일입니다.", REQUIRED_MIN_PERIOD, REQUIRED_MAX_PERIOD)
            );
        }
    }

    public void addContent(final FeedContent content) {
        contents.add(content);
        if (content.isNotSameFeed(this)) {
            content.updateFeed(this);
        }
    }

    public void addTags(final FeedTags tags) {
        this.tags.addAll(tags);
    }

    public boolean isCreator(final Member member) {
        return Objects.equals(creator.getId(), member.getId());
    }

    public void delete() {
        this.status = FeedStatus.DELETED;
    }

    public Optional<FeedContent> findLastFeedContent() {
        return this.contents.findLastRoadmapContent();
    }

    public boolean isDeleted() {
        return status == FeedStatus.DELETED;
    }

    public Member getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public FeedCategory getCategory() {
        return category;
    }

    public FeedContents getContents() {
        return contents;
    }

    public String getIntroduction() {
        return introduction;
    }

    public Integer getRequiredPeriod() {
        return requiredPeriod;
    }

    public FeedTags getTags() {
        return tags;
    }
}
