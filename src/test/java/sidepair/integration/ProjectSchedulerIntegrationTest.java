package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static sidepair.integration.fixture.AuthenticationAPIFixture.로그인;
import static sidepair.integration.fixture.CommonFixture.BEARER_TOKEN_FORMAT;
import static sidepair.integration.fixture.CommonFixture.LOCATION;
import static sidepair.integration.fixture.FeedAPIFixture.신청서를_생성하고_아이디를_반환한다;
import static sidepair.integration.fixture.FeedAPIFixture.프로젝트_참가_허용;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드를_아이디로_조회하고_응답객체를_반환한다;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;
import static sidepair.integration.fixture.MemberAPIFixture.회원가입;
import static sidepair.integration.fixture.ProjectAPIFixture.기본_프로젝트_생성;
import static sidepair.integration.fixture.ProjectAPIFixture.사용자의_특정_프로젝트_정보를_조회한다;
import static sidepair.integration.fixture.ProjectAPIFixture.십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.오늘;
import static sidepair.integration.fixture.ProjectAPIFixture.이십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_이름;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_제한_인원;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_회고_횟수;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_생성;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트를_시작한다;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectStatus;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectPendingMemberRepository;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.PositionType;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;
import sidepair.service.scheduler.ProjectScheduler;

class ProjectSchedulerIntegrationTest extends InitIntegrationTest {

    private final ProjectScheduler projectScheduler;
    private final ProjectPendingMemberRepository projectPendingMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectSchedulerIntegrationTest(final ProjectScheduler projectScheduler,
                                            final ProjectPendingMemberRepository projectPendingMemberRepository,
                                            final ProjectMemberRepository projectMemberRepository) {
        this.projectScheduler = projectScheduler;
        this.projectPendingMemberRepository = projectPendingMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Test
    void 프로젝트이_시작되면_프로젝트_대기_사용자에서_프로젝트_사용자로_이동하고_대기_사용자에서는_제거된다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);
        final Project 프로젝트 = new Project(기본_프로젝트_아이디, null, null, null, null);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_프로젝트_아이디 ,신청서_아이디, 기본_로그인_토큰);

        // when
        projectScheduler.startProjects();

        // then
        assertAll(
                () -> assertThat(projectPendingMemberRepository.findAllByProject(프로젝트)).isEmpty(),
                () -> assertThat(projectMemberRepository.findAllByProject(프로젝트)).hasSize(2)
        );
    }

    @Test
    void 자정에_시작_날짜가_오늘_이전이면서_모집_중인_프로젝트들도_시작된다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);
        final Project 프로젝트 = new Project(기본_프로젝트_아이디, null, null, null, null);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_프로젝트_아이디 ,신청서_아이디, 기본_로그인_토큰);
        testTransactionService.프로젝트의_시작날짜를_변경한다(기본_프로젝트_아이디, 오늘.minusDays(10));

        // when
        projectScheduler.startProjects();

        // then
        assertAll(
                () -> assertThat(projectPendingMemberRepository.findAllByProject(프로젝트)).isEmpty(),
                () -> assertThat(projectMemberRepository.findAllByProject(프로젝트)).hasSize(2)
        );
    }

    @Test
    void 프로젝트의_시작날짜가_오늘보다_이후이면_아무일도_일어나지_않는다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);
        final Project 프로젝트 = new Project(기본_프로젝트_아이디, null, null, null, null);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_프로젝트_아이디 ,신청서_아이디, 기본_로그인_토큰);
        testTransactionService.프로젝트의_시작날짜를_변경한다(기본_프로젝트_아이디, 오늘.plusDays(1));

        // when
        projectScheduler.startProjects();

        // then
        assertAll(
                () -> assertThat(projectPendingMemberRepository.findAllByProject(프로젝트)).hasSize(2),
                () -> assertThat(projectMemberRepository.findAllByProject(프로젝트)).isEmpty()
        );
    }

    @Test
    void 프로젝트_종료시_종료_날짜가_어제인_프로젝트의_상태가_COMPLETED로_변경된다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_프로젝트_아이디 ,신청서_아이디, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // when
        testTransactionService.프로젝트의_종료날짜를_변경한다(기본_프로젝트_아이디, 오늘.minusDays(1));
        projectScheduler.endProjects();
        final MemberProjectResponse 요청_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // then
        assertThat(요청_응답값.status()).isEqualTo(ProjectStatus.COMPLETED.name());
    }

    @Test
    void 프로젝트_종료시_종료_날짜가_어제가_아니면_아무_일도_일어나지_않는다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_프로젝트_아이디 ,신청서_아이디, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // when
        projectScheduler.endProjects();
        final MemberProjectResponse 요청_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // then
        assertThat(요청_응답값.status()).isEqualTo(ProjectStatus.RUNNING.name());
    }

    @Test
    void 진행_중인_사용자_단일_프로젝트를_조회할_때_진행_중인_노드_기간이_아니면_빈_회고를_반환한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 십일_후에_시작하는_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_프로젝트_아이디 ,신청서_아이디, 기본_로그인_토큰);

        testTransactionService.프로젝트의_시작날짜를_변경한다(기본_프로젝트_아이디, 오늘);
        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // when
        final MemberProjectResponse 요청_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // then
        assertThat(요청_응답값.memoirs()).isEmpty();
    }

    @Test
    void 모집_중인_사용자_단일_프로젝트_조회_시_회고가_빈_응답을_반환한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_프로젝트_아이디, 신청서_아이디, 기본_로그인_토큰);

        //when
        final MemberProjectResponse 요청_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        //then
        assertThat(요청_응답값.memoirs()).isEmpty();
    }

    private Long 십일_후에_시작하는_프로젝트_생성(final String 액세스_토큰, final FeedResponse 피드_응답) {
        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 십일_후, 이십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.content().id(), 정상적인_프로젝트_이름,
                정상적인_프로젝트_제한_인원, 프로젝트_노드_별_기간_요청);
        final String Location_헤더 = 프로젝트_생성(프로젝트_생성_요청, 액세스_토큰).response().header(LOCATION);
        return Long.parseLong(Location_헤더.substring(14));
    }
}
