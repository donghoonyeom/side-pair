package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static sidepair.integration.fixture.FeedAPIFixture.피드_삭제;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드를_아이디로_조회하고_응답객체를_반환한다;
import static sidepair.integration.fixture.ProjectAPIFixture.십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.오늘;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_이름;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_제한_인원;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_회고_횟수;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트를_생성하고_아이디를_반환한다;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.project.ProjectStatus;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.persistence.feed.FeedRepository;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;
import sidepair.service.scheduler.FeedScheduler;

class FeedSchedulerIntegrationTest extends InitIntegrationTest {

    private static final LocalDate 현재부터_2개월_1일_전 = 오늘.minusMonths(2).minusDays(1);

    private final FeedScheduler feedScheduler;
    private final FeedRepository feedRepository;

    public FeedSchedulerIntegrationTest(final FeedScheduler feedScheduler,
                                           final FeedRepository feedRepository) {
        this.feedScheduler = feedScheduler;
        this.feedRepository = feedRepository;
    }

    @Test
    void 삭제된_상태의_피드를_삭제시_모든_프로젝트가_종료된지_2개월이_지났으면_정상적으로_삭제한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

            final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                    new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
                final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디1 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

            final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청2 = List.of(
                    new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
                final ProjectCreateRequest 프로젝트_생성_요청2 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청2);

        final Long 프로젝트_아이디2 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청2, 기본_로그인_토큰);

        피드_삭제(기본_피드_아이디, 기본_로그인_토큰);

            testTransactionService.프로젝트의_상태와_종료날짜를_변경한다(프로젝트_아이디1, ProjectStatus.COMPLETED, 현재부터_2개월_1일_전);
            testTransactionService.프로젝트의_상태와_종료날짜를_변경한다(프로젝트_아이디2, ProjectStatus.COMPLETED, 현재부터_2개월_1일_전);

        // when
        feedScheduler.deleteFeeds();

        // then
        assertThat(feedRepository.findAll()).hasSize(0);
    }

    @Test
    void 삭제된_상태의_피드_삭제시_종료되지_않은_프로젝트가_있으면_삭제되지_않는다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

            final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                    new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
                final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디1 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

            final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청2 = List.of(
                    new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
                final ProjectCreateRequest 프로젝트_생성_요청2 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청2);

        final Long 프로젝트_아이디2 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청2, 기본_로그인_토큰);

        피드_삭제(기본_피드_아이디, 기본_로그인_토큰);

            testTransactionService.프로젝트의_상태와_종료날짜를_변경한다(프로젝트_아이디1, ProjectStatus.COMPLETED, 현재부터_2개월_1일_전);

        // when
        feedScheduler.deleteFeeds();

        // then
        assertThat(feedRepository.findAll()).hasSize(1);
    }

    @Test
    void 삭제된_상태의_피드_삭제시_종료된지_3개월이_지나지_않은_프로젝트가_있으면_삭제되지_않는다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

            final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                    new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
                final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디1 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

            final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청2 = List.of(
                    new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
                final ProjectCreateRequest 프로젝트_생성_요청2 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청2);

        final Long 프로젝트_아이디2 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청2, 기본_로그인_토큰);

        피드_삭제(기본_피드_아이디, 기본_로그인_토큰);

            testTransactionService.프로젝트의_상태와_종료날짜를_변경한다(프로젝트_아이디1, ProjectStatus.COMPLETED, 현재부터_2개월_1일_전);
            testTransactionService.프로젝트의_상태와_종료날짜를_변경한다(프로젝트_아이디2, ProjectStatus.COMPLETED, 오늘);

        // when
        feedScheduler.deleteFeeds();

        // then
        assertThat(feedRepository.findAll()).hasSize(1);
    }
}
