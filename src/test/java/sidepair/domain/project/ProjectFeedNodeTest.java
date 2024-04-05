package sidepair.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.project.exeption.ProjectException;
import sidepair.domain.project.vo.Period;

class ProjectFeedNodeTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void 정상적으로_프로젝_피드_노드를_생성한다(final int daysToAdd) {
        //given
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = startDate.plusDays(daysToAdd);

        //when
        //then
        assertDoesNotThrow(() -> new ProjectFeedNode(new Period(startDate, endDate), daysToAdd + 1,
                new FeedNode("title", "content")));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, -3, -4, -5, -6, -7})
    void 프로젝_노드의_인증_횟수가_음수일때_예외를_던진다(final int checkCount) {
        //given
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = startDate.plusDays(0);

        //when
        //then
        assertThatThrownBy(() -> new ProjectFeedNode(new Period(startDate, endDate), checkCount,
                new FeedNode("title", "content")))
                .isInstanceOf(ProjectException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7})
    void 프로젝_피드_노드를_생성할때_기간보다_인증_횟수가_크면_예외를_던진다(final int checkCount) {
        //given
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now();

        //when
        //then
        assertThatThrownBy(() -> new ProjectFeedNode(new Period(startDate, endDate), checkCount,
                new FeedNode("title", "content")))
                .isInstanceOf(ProjectException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7})
    void 프로젝_피드_노드를_생성할때_시작날짜가_오늘보다_전일_경우_예외를_던진다(final long daysToSubtract) {
        //given
        final LocalDate startDate = LocalDate.now().minusDays(daysToSubtract);
        final LocalDate endDate = startDate.plusDays(7);
        final int checkCount = 7;

        //when
        //then
        assertThatThrownBy(() -> new ProjectFeedNode(new Period(startDate, endDate), checkCount,
                new FeedNode("title", "content")))
                .isInstanceOf(ProjectException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7})
    void 프로젝트_피드_노드를_생성할때_시작날짜가_종료날짜보다_후일_경우_예외를_던진다(final long daysToSubtract) {
        //given
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = startDate.minusDays(daysToSubtract);
        final int checkCount = 0;

        //when
        //then
        assertThatThrownBy(() -> new ProjectFeedNode(new Period(startDate, endDate), checkCount,
                new FeedNode("title", "content")))
                .isInstanceOf(ProjectException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2300-07-01", "2300-07-06", "2300-07-15"})
    void 노드가_진행_중인_날짜면_true를_반환한다(final LocalDate date) {
        final ProjectFeedNode projectFeedNode = new ProjectFeedNode(
                new Period(LocalDate.of(2300, 7, 1),
                        LocalDate.of(2300, 7, 15)),
                7, new FeedNode("제목", "내용"));

        assertThat(projectFeedNode.isDayOfNode(date)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2300-06-10", "2300-06-30", "2300-07-16", "2301-07-03"})
    void 노드가_진행_중인_날짜가_아니면_false를_반환한다(final LocalDate date) {
        final ProjectFeedNode projectFeedNode = new ProjectFeedNode(
                new Period(LocalDate.of(2300, 7, 1),
                        LocalDate.of(2300, 7, 15)),
                7, new FeedNode("제목", "내용"));

        assertThat(projectFeedNode.isDayOfNode(date)).isFalse();
    }
}