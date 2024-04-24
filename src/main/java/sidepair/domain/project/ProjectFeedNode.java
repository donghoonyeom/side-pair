package sidepair.domain.project;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseEntity;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.project.exeption.ProjectException;
import sidepair.domain.project.vo.Period;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectFeedNode extends BaseEntity {

    private static final int MIN_CHECK_COUNT = 0;

    @Embedded
    private Period period;

    private Integer checkCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_node_id", nullable = false)
    private FeedNode feedNode;

    public ProjectFeedNode(final Period period, final Integer checkCount, final FeedNode feedNode) {
        this(null, period, checkCount, feedNode);
    }

    public ProjectFeedNode(final Long id, final Period period, final Integer checkCount,
                           final FeedNode feedNode) {
        validate(period, checkCount);
        this.id = id;
        this.period = period;
        this.checkCount = checkCount;
        this.feedNode = feedNode;
    }

    private void validate(final Period period, final Integer checkCount) {
        validateCheckCountPositive(checkCount);
        validateCheckCountWithDaysBetween(period, checkCount);
    }

    private void validateCheckCountPositive(final Integer checkCount) {
        if (checkCount < MIN_CHECK_COUNT) {
            throw new ProjectException("프로잭트 노드의 인증 횟수는 0보다 커야합니다.");
        }
    }

    private void validateCheckCountWithDaysBetween(final Period period, final int checkCount) {
        if (checkCount > period.getDayCount()) {
            throw new ProjectException("프로잭트 노드의 인증 횟수가 설정 기간보다 클 수 없습니다.");
        }
    }

    public boolean isEndDateEqualOrAfterOtherStartDate(final ProjectFeedNode other) {
        return this.period.isEndDateEqualOrAfterOtherStartDate(other.period);
    }

    public boolean isDayOfNode(final LocalDate date) {
        return period.contains(date);
    }

    public LocalDate getStartDate() {
        return period.getStartDate();
    }

    public LocalDate getEndDate() {
        return period.getEndDate();
    }

    public FeedNode getFeedNode() {
        return feedNode;
    }

    public int getCheckCount() {
        return checkCount;
    }
}
