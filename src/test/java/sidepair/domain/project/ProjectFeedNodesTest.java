package sidepair.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.project.exeption.ProjectException;
import sidepair.domain.project.vo.Period;

class ProjectFeedNodesTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);
    private static final LocalDate TWENTY_DAY_LAYER = TODAY.plusDays(20);
    private static final LocalDate THIRTY_DAY_LATER = TODAY.plusDays(30);

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5, 100})
    void 정상적으로_프로젝트_노드들을_생성한다(final long daysToAdd) {
        //given
        final LocalDate firstStartDate = LocalDate.now();
        final LocalDate firstEndDate = firstStartDate.plusDays(daysToAdd);
        final LocalDate secondStartDate = firstEndDate.plusDays(daysToAdd);
        final LocalDate secondEndDate = secondStartDate.plusDays(daysToAdd);

        final ProjectFeedNode firstProjectFeedNode = new ProjectFeedNode(
                new Period(firstStartDate, firstEndDate), 0, null);
        final ProjectFeedNode secondProjectFeedNode = new ProjectFeedNode(
                new Period(secondStartDate, secondEndDate), 0, null);

        //when
        //then
        assertDoesNotThrow(
                () -> new ProjectFeedNodes(List.of(firstProjectFeedNode, secondProjectFeedNode)));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 2, 3, 4, 5, 100})
    void 프로젝트_노드들_생성_시_기간이_겹칠_경우_예외를_던진다(final long value) {
        //given
        final LocalDate firstStartDate = LocalDate.now();
        final LocalDate firstEndDate = firstStartDate.plusDays(value);
        final LocalDate secondStartDate = firstEndDate.minusDays(value);
        final LocalDate secondEndDate = secondStartDate.plusDays(value);

        final ProjectFeedNode firstProjectFeedNode = new ProjectFeedNode(
                new Period(firstStartDate, firstEndDate), 0, null);
        final ProjectFeedNode secondProjectFeedNode = new ProjectFeedNode(
                new Period(secondStartDate, secondEndDate), 0, null);

        //when
        //then
        assertThatThrownBy(() -> new ProjectFeedNodes(List.of(firstProjectFeedNode, secondProjectFeedNode)))
                .isInstanceOf(ProjectException.class);
    }

    @Test
    void 프로젝트_노드_생성_시_빈_리스트가_들어오면_정상적으로_생성된다() {
        //given
        //when
        //then
        assertDoesNotThrow(() -> new ProjectFeedNodes(Collections.emptyList()));
    }

    @Test
    void 프로젝트_피드_노드들_중_첫번째_시작날짜를_구한다() {
        // given
        // when
        final ProjectFeedNodes projectFeedNodes = new ProjectFeedNodes(new ArrayList<>(List.of(
                new ProjectFeedNode(new Period(TODAY, TEN_DAY_LATER), 1, null),
                new ProjectFeedNode(new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER), 1, null))
        ));

        // expect
        assertThat(projectFeedNodes.getProjectStartDate()).isEqualTo(TODAY);
    }

    @Test
    void 프로젝트_피드_노드들_중_마지막_종료날짜를_구한다() {
        // given
        // when
        final ProjectFeedNodes projectFeedNodes = new ProjectFeedNodes(new ArrayList<>(List.of(
                new ProjectFeedNode(new Period(TODAY, TEN_DAY_LATER), 1, null),
                new ProjectFeedNode(new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER), 1, null))
        ));

        // then
        assertThat(projectFeedNodes.getProjectEndDate()).isEqualTo(THIRTY_DAY_LATER);
    }

    @Test
    void 노드의_총_기간을_더한다() {
        // given
        final ProjectFeedNodes projectFeedNodes = 프로젝트_노드를_생성한다();

        // when
        final int totalPeriod = projectFeedNodes.addTotalPeriod();

        // then
        assertThat(totalPeriod)
                .isSameAs(31);
    }

    @Test
    void 해당_날짜에_진행하는_프로젝트_노드를_반환한다() {
        final ProjectFeedNodes projectFeedNodes = 프로젝트_노드를_생성한다();

        assertAll(
                () -> assertThat(projectFeedNodes.getNodeByDate(TODAY).get())
                        .isEqualTo(new ProjectFeedNode(new Period(TODAY, TEN_DAY_LATER),
                                10, new FeedNode("피드 제목 1", "피드 내용 1"))),
                () -> assertThat(projectFeedNodes.getNodeByDate(TEN_DAY_LATER).get())
                        .isEqualTo(new ProjectFeedNode(new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER),
                                10, new FeedNode("피드 제목 2", "피드 내용 2")))
        );
    }

    private ProjectFeedNodes 프로젝트_노드를_생성한다() {
        final ProjectFeedNode firstProjectFeedNode = new ProjectFeedNode(
                new Period(TODAY, TEN_DAY_LATER),
                10, new FeedNode("피드 제목 1", "피드 내용 1"));

        final ProjectFeedNode secondProjectFeedNode = new ProjectFeedNode(
                new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER),
                10, new FeedNode("피드 제목 2", "피드 내용 2"));

        return new ProjectFeedNodes(
                List.of(firstProjectFeedNode, secondProjectFeedNode));
    }
}
