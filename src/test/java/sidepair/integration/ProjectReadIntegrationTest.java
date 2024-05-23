package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static sidepair.integration.fixture.AuthenticationAPIFixture.로그인;
import static sidepair.integration.fixture.CommonFixture.BEARER_TOKEN_FORMAT;
import static sidepair.integration.fixture.FeedAPIFixture.신청서를_생성하고_아이디를_반환한다;
import static sidepair.integration.fixture.FeedAPIFixture.프로젝트_참가_허용;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드를_아이디로_조회하고_응답객체를_반환한다;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;
import static sidepair.integration.fixture.MemberAPIFixture.회원가입;
import static sidepair.integration.fixture.ProjectAPIFixture.기본_프로젝트_생성;
import static sidepair.integration.fixture.ProjectAPIFixture.사용자가_참여한_프로젝트_중_프로젝트_진행_상태에_따라_목록을_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.사용자의_모든_프로젝트_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.사용자의_특정_프로젝트_정보를_조회한다;
import static sidepair.integration.fixture.ProjectAPIFixture.삼십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.오늘;
import static sidepair.integration.fixture.ProjectAPIFixture.이십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_이름;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_제한_인원;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_회고_횟수;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_노드_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_아이디로_프로젝트를_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_아이디와_토큰으로_프로젝트_정보를_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_투두리스트_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_투두리스트_추가;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트를_생성하고_아이디를_반환한다;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트를_시작한다;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트의_사용자_정보를_전체_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트의_사용자_정보를_정렬_기준없이_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.피드_아이디로_프로젝트_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.회고글_등록;
import static sidepair.integration.fixture.ProjectAPIFixture.회고글_전체_조회_요청;

import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.response.FeedProjectResponses;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.PositionType;
import sidepair.service.dto.mamber.response.MemberProjectForListResponse;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.project.ProjectMemberSortTypeDto;
import sidepair.service.dto.project.request.MemoirRequest;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;
import sidepair.service.dto.project.request.ProjectTodoRequest;
import sidepair.service.dto.project.response.MemoirResponse;
import sidepair.service.dto.project.response.ProjectCertifiedResponse;
import sidepair.service.dto.project.response.ProjectFeedNodeDetailResponse;
import sidepair.service.dto.project.response.ProjectFeedNodeResponse;
import sidepair.service.dto.project.response.ProjectFeedNodesResponse;
import sidepair.service.dto.project.response.ProjectMemberResponse;
import sidepair.service.dto.project.response.ProjectMemoirResponse;
import sidepair.service.dto.project.response.ProjectResponse;
import sidepair.service.dto.project.response.ProjectTodoResponse;

class ProjectReadIntegrationTest extends InitIntegrationTest {

    @Test
    void 프로젝트_아이디로_프로젝트_정보를_조회한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        // when
        final ProjectResponse 프로젝트_응답값 = 프로젝트_아이디로_프로젝트를_조회(기본_프로젝트_아이디)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(프로젝트_응답값.name()).isEqualTo(정상적인_프로젝트_이름);
    }

    @Test
    void 프로젝트_아이디와_사용자_아이디로_프로젝트_정보를_조회한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        // when
        final ProjectCertifiedResponse 프로젝트_응답값 = 프로젝트_아이디와_토큰으로_프로젝트_정보를_조회(기본_프로젝트_아이디, 기본_로그인_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(프로젝트_응답값.name()).isEqualTo(정상적인_프로젝트_이름);
    }

    @Test
    void 프로젝트_투두리스트를_조회한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final ProjectTodoRequest 프로젝트_투두_생성_요청 = new ProjectTodoRequest("content", 이십일_후, 삼십일_후);
        프로젝트_투두리스트_추가(기본_로그인_토큰, 기본_프로젝트_아이디, 프로젝트_투두_생성_요청);

        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // when
        final List<ProjectTodoResponse> 프로젝트_투두리스트_응답값 = 프로젝트_투두리스트_조회(기본_프로젝트_아이디, 기본_로그인_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(프로젝트_투두리스트_응답값.get(0).startDate())
                .isEqualTo(이십일_후);
    }

    @Test
    void 프로젝트_투두리스트_조회시_존재하지_않은_프로젝트일_경우() {
        // given
        // when
        final ErrorResponse 예외_응답 = 프로젝트_투두리스트_조회(1L, 기본_로그인_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(예외_응답)
                .isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트_투두리스트_조회시_참여하지_않은_사용자일_경우() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final ProjectTodoRequest 프로젝트_투두_생성_요청 = new ProjectTodoRequest("content", 이십일_후, 삼십일_후);
        프로젝트_투두리스트_추가(기본_로그인_토큰, 기본_프로젝트_아이디, 프로젝트_투두_생성_요청);

        final MemberJoinRequest 다른_사용자_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 다른_사용자_로그인_요청 = new LoginRequest(다른_사용자_회원_가입_요청.email(), 다른_사용자_회원_가입_요청.password());
        회원가입(다른_사용자_회원_가입_요청);
        final String 다른_사용자_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(다른_사용자_로그인_요청).accessToken());

        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // when
        final ErrorResponse 예외_응답 = 프로젝트_투두리스트_조회(기본_프로젝트_아이디, 다른_사용자_액세스_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(예외_응답)
                .isEqualTo(new ErrorResponse("프로젝트에 참여하지 않은 사용자입니다. projectId = " + 기본_프로젝트_아이디 +
                        " memberEmail = test2@email.com"));
    }

    @Test
    void 진행중인_사용자_단일_프로젝트를_조회한다() throws IOException {
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

        프로젝트_참가_허용(기본_피드_아이디, 신청서_아이디, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        final MemoirRequest 회고글_등록_요청 = new MemoirRequest("description");
        회고글_등록(기본_프로젝트_아이디, 회고글_등록_요청, 기본_로그인_토큰);
        회고글_등록(기본_프로젝트_아이디, 회고글_등록_요청, 팔로워_액세스_토큰);

        // when
        final MemberProjectResponse 요청_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // then
        final MemberProjectResponse 예상되는_응답값 = new MemberProjectResponse(정상적인_프로젝트_이름, "RUNNING", 기본_회원_아이디,
                2, 정상적인_프로젝트_제한_인원, 오늘, 십일_후, 피드_응답.content().id(),
                new ProjectFeedNodesResponse(false, false,
                        List.of(new ProjectFeedNodeResponse(피드_응답.content().nodes().get(0).id(),
                                "feed 1st week", 오늘, 십일_후, 정상적인_프로젝트_회고_횟수))),
                List.of(),
                List.of(new MemoirResponse(2L, "description", LocalDate.now()),
                        new MemoirResponse(1L, "description", LocalDate.now())));

        assertThat(요청_응답값)
                .usingRecursiveComparison()
                .isEqualTo(예상되는_응답값);
    }

    @Test
    void 프로젝트_시작_전에_사용자_단일_프로젝트_조회_시_회고글가_빈_응답을_반환한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        //when
        final MemberProjectResponse 요청_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        //then
        assertThat(요청_응답값.memoirs()).isEmpty();
    }

    @Test
    void 사용자의_모든_프로젝트_목록을_조회한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "두번째_피드 제목", "두번째_피드 소개글",
                "두번째_피드 본문", 30,
                List.of(new FeedNodeSaveRequest("두번째_피드 1주차", "두번째_피드 1주차 내용", null)), null);
        피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 두번째_피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 두번째_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 두번째_피드_응답);

        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);
        프로젝트를_시작한다(기본_로그인_토큰, 두번째_프로젝트_아이디);

        // when
        final List<MemberProjectForListResponse> 요청_응답값 = 사용자의_모든_프로젝트_조회(기본_로그인_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(요청_응답값.get(0).projectId()).isEqualTo(기본_프로젝트_아이디);
        assertThat(요청_응답값.get(1).projectId()).isEqualTo(두번째_프로젝트_아이디);
    }

    @Test
    void 사용자가_참여한_프로젝트_중_모집_중인_프로젝트_목록을_조회한다() throws IOException {
        // given
        final Long 첫번쨰_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 첫번째_피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(첫번쨰_피드_아이디);

        final Long 첫번째_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 첫번째_피드_응답);

        final Long 두번째_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 두번째_피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(두번째_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(두번째_피드_응답.content().nodes().get(0).id(), 10, 십일_후, 이십일_후));
        final ProjectCreateRequest 두번째_프로젝트_생성_요청 = new ProjectCreateRequest(두번째_피드_응답.content().id(), 정상적인_프로젝트_이름,
                6, 프로젝트_노드_별_기간_요청);

        final Long 두번째_프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(두번째_프로젝트_생성_요청, 기본_로그인_토큰);

        프로젝트를_시작한다(기본_로그인_토큰, 첫번째_프로젝트_아이디);
        프로젝트를_시작한다(기본_로그인_토큰, 두번째_프로젝트_아이디);

        // when
        final List<MemberProjectForListResponse> 요청_응답값 = 사용자가_참여한_프로젝트_중_프로젝트_진행_상태에_따라_목록을_조회(기본_로그인_토큰, "RECRUITING")
                .as(new TypeRef<>() {
                });

        // then
        assertThat(요청_응답값.get(0).projectId()).isEqualTo(두번째_프로젝트_아이디);
    }

    @Test
    void 사용자가_참여한_프로젝트_중_진행_중인_프로젝트_목록을_조회한다() throws IOException {
        // given
        final Long 첫번쨰_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 첫번째_피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(첫번쨰_피드_아이디);

        final Long 첫번째_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 첫번째_피드_응답);

        final Long 두번째_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 두번째_피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(두번째_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(두번째_피드_응답.content().nodes().get(0).id(), 10, 십일_후, 이십일_후));
        final ProjectCreateRequest 두번째_프로젝트_생성_요청 = new ProjectCreateRequest(두번째_피드_응답.content().id(), 정상적인_프로젝트_이름,
                6, 프로젝트_노드_별_기간_요청);

        final Long 두번째_프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(두번째_프로젝트_생성_요청, 기본_로그인_토큰);

        프로젝트를_시작한다(기본_로그인_토큰, 첫번째_프로젝트_아이디);
        프로젝트를_시작한다(기본_로그인_토큰, 두번째_프로젝트_아이디);

        // when
        final List<MemberProjectForListResponse> 요청_응답값 = 사용자가_참여한_프로젝트_중_프로젝트_진행_상태에_따라_목록을_조회(기본_로그인_토큰, "RUNNING")
                .as(new TypeRef<>() {
                });

        // then
        assertThat(요청_응답값.get(0).projectId()).isEqualTo(첫번째_프로젝트_아이디);
    }

    @Test
    void 프로젝트_노드를_조회한다() throws IOException {
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

        프로젝트_참가_허용(기본_피드_아이디, 신청서_아이디, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // when
        final List<ProjectFeedNodeDetailResponse> 프로젝트_노드_응답값 = 프로젝트_노드_조회(기본_프로젝트_아이디, 기본_로그인_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(프로젝트_노드_응답값.get(0).title()).isEqualTo("feed 1st week");
    }

    @Test
    void 프로젝트_노드_조회시_존재하지_않은_프로젝트일_경우_예외가_발생한다() {
        // given
        // when
        final ErrorResponse 예외_응답값 = 프로젝트_노드_조회(1L, 기본_로그인_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(예외_응답값)
                .isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트_노드_조회시_참여하지_않은_사용자일_경우_예외가_발생한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 다른_사용자_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        // when
        final ErrorResponse 예외_응답값 = 프로젝트_노드_조회(기본_프로젝트_아이디, 다른_사용자_액세스_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(예외_응답값)
                .isEqualTo(new ErrorResponse("프로젝트에 참여하지 않은 사용자입니다. projectId = 1 memberEmail = test2@email.com"));
    }

    @Test
    void 프로젝트의_인증피드를_전체_조회한다() throws IOException {
        // given
        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_피드_아이디, 신청서_아이디, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        final MemoirRequest 회고글_등록_요청1 = new MemoirRequest("description1");
        final MemoirRequest 회고글_등록_요청2 = new MemoirRequest("description2");

        회고글_등록(기본_프로젝트_아이디, 회고글_등록_요청1, 기본_로그인_토큰);
        회고글_등록(기본_프로젝트_아이디, 회고글_등록_요청2, 팔로워_액세스_토큰);

        //when
        final List<ProjectMemoirResponse> 회고글_전체_조회_요청에_대한_응답 = 회고글_전체_조회_요청(팔로워_액세스_토큰, 기본_프로젝트_아이디)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(회고글_전체_조회_요청에_대한_응답.get(0).memoir().description()).isEqualTo(회고글_등록_요청2.description());
        assertThat(회고글_전체_조회_요청에_대한_응답.get(1).memoir().description()).isEqualTo(회고글_등록_요청1.description());
    }

    @Test
    void 프로젝트의_인증피드를_전체_조회시_존재하지_않는_프로젝트인_경우_예외가_발생한다() throws IOException {
        // given
        //when
        final Long 존재하지_않는_프로젝트_아이디 = 1L;
        final ExtractableResponse<Response> 회고글_전체_조회_요청에_대한_응답 = 회고글_전체_조회_요청(기본_로그인_토큰, 존재하지_않는_프로젝트_아이디);

        // then
        final ErrorResponse 회고글_전체_조회_응답_바디 = jsonToClass(회고글_전체_조회_요청에_대한_응답.asString(), new TypeReference<>() {
        });
        assertThat(회고글_전체_조회_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(회고글_전체_조회_응답_바디).isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트의_인증피드를_전체_조회시_프로젝트에_참여하지_않은_사용자면_예외가_발생한다() throws IOException {
        // given
        final MemberJoinRequest 다른_회원_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 다른_회원_로그인_요청 = new LoginRequest(다른_회원_회원_가입_요청.email(), 다른_회원_회원_가입_요청.password());
        회원가입(다른_회원_회원_가입_요청);
        final String 다른_회원_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(다른_회원_로그인_요청).accessToken());

        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);
        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        final MemoirRequest 회고글_등록_요청1 = new MemoirRequest("description1");

        회고글_등록(기본_프로젝트_아이디, 회고글_등록_요청1, 기본_로그인_토큰);

        //when
        final ExtractableResponse<Response> 회고글_전체_조회_요청에_대한_응답 = 회고글_전체_조회_요청(다른_회원_액세스_토큰, 기본_프로젝트_아이디);

        // then
        final ErrorResponse 회고글_전체_조회_응답_바디 = jsonToClass(회고글_전체_조회_요청에_대한_응답.asString(), new TypeReference<>() {
        });
        assertThat(회고글_전체_조회_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(회고글_전체_조회_응답_바디).isEqualTo(new ErrorResponse("프로젝트에 참여하지 않은 회원입니다."));
    }

    @Test
    void 프로젝트의_사용자_정보를_달성률순으로_전체_조회한다() throws IOException {
        // given
        final MemberJoinRequest 팔로워1_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follow1", PositionType.BACKEND, DEFAULT_SKILLS);
        final MemberJoinRequest 팔로워2_회원_가입_요청 = new MemberJoinRequest("test3@email.com", "paswword2@",
                "follow2", PositionType.BACKEND, DEFAULT_SKILLS);
        final Long 팔로워1_아이디 = 회원가입(팔로워1_회원_가입_요청);
        final Long 팔로워2_아이디 = 회원가입(팔로워2_회원_가입_요청);

        final LoginRequest 팔로워1_로그인_요청 = new LoginRequest(팔로워1_회원_가입_요청.email(), 팔로워1_회원_가입_요청.password());
        final LoginRequest 팔로워2_로그인_요청 = new LoginRequest(팔로워2_회원_가입_요청.email(), 팔로워2_회원_가입_요청.password());

        final String 팔로워1_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워1_로그인_요청).accessToken());
        final String 팔로워2_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워2_로그인_요청).accessToken());

        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);
        final Long 신청서1_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워1_액세스_토큰, 기본_피드_아이디);
        final Long 신청서2_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워2_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_피드_아이디, 신청서1_아이디, 기본_로그인_토큰);
        프로젝트_참가_허용(기본_피드_아이디, 신청서2_아이디, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 기본_프로젝트_아이디);

        final MemoirRequest 회고글_등록_요청1 = new MemoirRequest("description1");

        회고글_등록(기본_프로젝트_아이디, 회고글_등록_요청1, 팔로워1_액세스_토큰);

        //when
        final List<ProjectMemberResponse> 프로젝트_사용자_응답 = 프로젝트의_사용자_정보를_전체_조회(기본_프로젝트_아이디, 기본_로그인_토큰,
                ProjectMemberSortTypeDto.PARTICIPATION_RATE.name()).as(new TypeRef<>() {
        });

        // then
        assertThat(프로젝트_사용자_응답.get(0).memberId()).isEqualTo(팔로워1_아이디);
        assertThat(프로젝트_사용자_응답.get(1).memberId()).isEqualTo(기본_회원_아이디);
        assertThat(프로젝트_사용자_응답.get(2).memberId()).isEqualTo(팔로워2_아이디);
    }

    @Test
    void 모집중인_프로젝트의_사용자_정보를_참가한_최신순으로_전체_조회한다() throws IOException {
        // given
        final MemberJoinRequest 팔로워1_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follow1", PositionType.BACKEND, DEFAULT_SKILLS);
        final MemberJoinRequest 팔로워2_회원_가입_요청 = new MemberJoinRequest("test3@email.com", "paswword2@",
                "follow2", PositionType.BACKEND, DEFAULT_SKILLS);
        final Long 팔로워1_아이디 = 회원가입(팔로워1_회원_가입_요청);
        final Long 팔로워2_아이디 = 회원가입(팔로워2_회원_가입_요청);

        final LoginRequest 팔로워1_로그인_요청 = new LoginRequest(팔로워1_회원_가입_요청.email(), 팔로워1_회원_가입_요청.password());
        final LoginRequest 팔로워2_로그인_요청 = new LoginRequest(팔로워2_회원_가입_요청.email(), 팔로워2_회원_가입_요청.password());

        final String 팔로워1_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워1_로그인_요청).accessToken());
        final String 팔로워2_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워2_로그인_요청).accessToken());

        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 신청서1_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워1_액세스_토큰, 기본_피드_아이디);
        final Long 신청서2_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워2_액세스_토큰, 기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        프로젝트_참가_허용(기본_프로젝트_아이디, 신청서1_아이디, 기본_로그인_토큰);
        프로젝트_참가_허용(기본_프로젝트_아이디, 신청서2_아이디, 기본_로그인_토큰);

        //when
        final List<ProjectMemberResponse> 프로젝트_사용자_응답 = 프로젝트의_사용자_정보를_전체_조회(기본_프로젝트_아이디, 기본_로그인_토큰,
                ProjectMemberSortTypeDto.JOINED_DESC.name()).as(new TypeRef<>() {
        });

        // then
        assertThat(프로젝트_사용자_응답.get(0).memberId()).isEqualTo(팔로워2_아이디);
        assertThat(프로젝트_사용자_응답.get(1).memberId()).isEqualTo(팔로워1_아이디);
        assertThat(프로젝트_사용자_응답.get(2).memberId()).isEqualTo(기본_회원_아이디);
    }

    @Test
    void 모집중인_프로젝트의_사용자_정보_조회시_정렬기준을_입력하지_않으면_참여한지_오래된순으로_정렬한다() throws IOException {
        // given
        final MemberJoinRequest 팔로워1_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follow1", PositionType.BACKEND, DEFAULT_SKILLS);
        final MemberJoinRequest 팔로워2_회원_가입_요청 = new MemberJoinRequest("test3@email.com", "paswword2@",
                "follow2", PositionType.BACKEND, DEFAULT_SKILLS);
        final Long 팔로워1_아이디 = 회원가입(팔로워1_회원_가입_요청);
        final Long 팔로워2_아이디 = 회원가입(팔로워2_회원_가입_요청);

        final LoginRequest 팔로워1_로그인_요청 = new LoginRequest(팔로워1_회원_가입_요청.email(), 팔로워1_회원_가입_요청.password());
        final LoginRequest 팔로워2_로그인_요청 = new LoginRequest(팔로워2_회원_가입_요청.email(), 팔로워2_회원_가입_요청.password());

        final String 팔로워1_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워1_로그인_요청).accessToken());
        final String 팔로워2_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워2_로그인_요청).accessToken());

        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 신청서1_아이디 = 신청서를_생성하고_아이디를_반환한다(피드_기본_신청서_생성_요청, 팔로워1_액세스_토큰, 기본_피드_아이디);
        final Long 신청서2_아이디 = 신청서를_생성하고_아이디를_반환한다(피드_기본_신청서_생성_요청, 팔로워2_액세스_토큰, 기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        프로젝트_참가_허용(기본_프로젝트_아이디, 신청서1_아이디, 기본_로그인_토큰);
        프로젝트_참가_허용(기본_프로젝트_아이디, 신청서2_아이디, 기본_로그인_토큰);

        //when
        final List<ProjectMemberResponse> 프로젝트_사용자_응답 = 프로젝트의_사용자_정보를_정렬_기준없이_조회(기본_프로젝트_아이디, 기본_로그인_토큰)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(프로젝트_사용자_응답.get(0).memberId()).isEqualTo(기본_회원_아이디);
        assertThat(프로젝트_사용자_응답.get(1).memberId()).isEqualTo(팔로워1_아이디);
        assertThat(프로젝트_사용자_응답.get(2).memberId()).isEqualTo(팔로워2_아이디);
    }

    @Test
    void 프로젝트의_사용자_정보_조회시_존재하지_않는_프로젝트이면_예외가_발생한다() {
        // given
        // when
        final ErrorResponse 예외_응답 = 프로젝트의_사용자_정보를_전체_조회(1L, 기본_로그인_토큰,
                ProjectMemberSortTypeDto.PARTICIPATION_RATE.name()).as(new TypeRef<>() {
        });

        // then
        assertThat(예외_응답.message()).isEqualTo("존재하지 않는 프로젝트입니다. projectId = 1");
    }

    @Test
    void 피드의_프로젝트를_조회한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 기본_프로젝트_아이디 = 기본_프로젝트_생성(기본_로그인_토큰, 피드_응답);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 10, 십일_후, 이십일_후));
        final ProjectCreateRequest 두번째_프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.content().id(), 정상적인_프로젝트_이름,
                6, 프로젝트_노드_별_기간_요청);

        final Long 두번째_프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(두번째_프로젝트_생성_요청, 기본_로그인_토큰);

        // when
        final FeedProjectResponses 피드_아이디로_프로젝트_목록_조회_응답1 = 피드_아이디로_프로젝트_조회(기본_로그인_토큰, 기본_피드_아이디)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_아이디로_프로젝트_목록_조회_응답1.responses().get(0).projectId()).isEqualTo(기본_프로젝트_아이디);
        assertThat(피드_아이디로_프로젝트_목록_조회_응답1.responses().get(1).projectId()).isEqualTo(두번째_프로젝트_아이디);
    }
}
