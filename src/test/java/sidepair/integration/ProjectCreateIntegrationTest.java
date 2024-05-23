package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static sidepair.integration.fixture.AuthenticationAPIFixture.로그인;
import static sidepair.integration.fixture.CommonFixture.BEARER_TOKEN_FORMAT;
import static sidepair.integration.fixture.FeedAPIFixture.신청서를_생성하고_아이디를_반환한다;
import static sidepair.integration.fixture.FeedAPIFixture.프로젝트_참가_허용;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드를_아이디로_조회하고_응답객체를_반환한다;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;
import static sidepair.integration.fixture.MemberAPIFixture.요청을_받는_사용자_자신의_정보_조회_요청;
import static sidepair.integration.fixture.MemberAPIFixture.회원가입;
import static sidepair.integration.fixture.ProjectAPIFixture.사용자의_특정_프로젝트_정보를_조회한다;
import static sidepair.integration.fixture.ProjectAPIFixture.십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.오늘;
import static sidepair.integration.fixture.ProjectAPIFixture.이십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_생성;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_이름;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_제한_인원;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_투두_컨텐츠;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_회고_횟수;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_나가기_요청;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_목록_조회_요청;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_생성;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_투두리스트_추가;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_투두리스트_추가후_아이디를_반환한다;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트_투두리스트를_체크한다;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트를_생성하고_아이디를_반환한다;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트를_시작한다;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트의_사용자_정보를_정렬_기준없이_조회;
import static sidepair.integration.fixture.ProjectAPIFixture.회고글_등록;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.feed.response.FeedProjectResponses;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.PositionType;
import sidepair.service.dto.mamber.response.MemberInformationResponse;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.project.ProjectFilterTypeDto;
import sidepair.service.dto.project.request.MemoirRequest;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;
import sidepair.service.dto.project.request.ProjectTodoRequest;
import sidepair.service.dto.project.response.ProjectMemberResponse;
import sidepair.service.dto.project.response.ProjectToDoCheckResponse;

class ProjectCreateIntegrationTest extends InitIntegrationTest {

    @Test
    void 정상적으로_프로젝트를_생성한다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        //when
        final ExtractableResponse<Response> 프로젝트_생성_응답 = 프로젝트_생성(프로젝트_생성_요청, 기본_로그인_토큰);

        //then
        assertThat(프로젝트_생성_응답.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(프로젝트_생성_응답.response().header("Location")).contains("/api/projects/");
    }

    @Test
    void 프로젝트_생성_시_컨텐츠_id가_빈값일_경우() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 피드_아이디가_빈값인_프로젝트_생성_요청 = new ProjectCreateRequest(null, 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        //when
        final ExtractableResponse<Response> 프로젝트_생성_응답 = 프로젝트_생성(피드_아이디가_빈값인_프로젝트_생성_요청, 기본_로그인_토큰);

        //then
        assertThat(프로젝트_생성_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        final List<ErrorResponse> 프로젝트_생성_응답_바디 = 프로젝트_생성_응답.as(new TypeRef<>() {
        });
        assertThat(프로젝트_생성_응답_바디).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(new ErrorResponse("피드 컨텐츠 아이디는 빈 값일 수 없습니다.")));
    }

    @Test
    void 프로젝트_생성_시_프로젝트_이름이_빈값일_경우() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_이름이_빈값인_프로젝트_생성_요청 = new ProjectCreateRequest(1L, null, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        //when
        final ExtractableResponse<Response> 프로젝트_생성_응답 = 프로젝트_생성(프로젝트_이름이_빈값인_프로젝트_생성_요청, 기본_로그인_토큰);

        //then
        final List<ErrorResponse> 프로젝트_생성_응답_바디 = 프로젝트_생성_응답.as(new TypeRef<>() {
        });

        assertThat(프로젝트_생성_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(프로젝트_생성_응답_바디).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(new ErrorResponse("프로젝트 이름을 빈 값일 수 없습니다.")));
    }

    @Test
    void 프로젝트_생성_시_프로젝트_제한_인원이_빈값일_경우() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_제한_인원이_빈값인_프로젝트_생성_요청 = new ProjectCreateRequest(1L, 정상적인_프로젝트_이름, null,
                프로젝트_노드_별_기간_요청);

        //when
        final ExtractableResponse<Response> 프로젝트_생성_응답 = 프로젝트_생성(프로젝트_제한_인원이_빈값인_프로젝트_생성_요청, 기본_로그인_토큰);

        //then
        final List<ErrorResponse> 프로젝트_생성_응답_바디 = 프로젝트_생성_응답.as(new TypeRef<>() {
        });

        assertThat(프로젝트_생성_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(프로젝트_생성_응답_바디).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(new ErrorResponse("프로젝트 제한 인원은 빈 값일 수 없습니다.")));
    }

    @Test
    void 프로젝트_생성_시_프로젝트_이름이_20자_초과인_경우() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final String 적절하지_않은_길이의_프로젝트_이름 = "a".repeat(21);
        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(기본_피드_아이디, 적절하지_않은_길이의_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        //when
        final ExtractableResponse<Response> 프로젝트_생성_응답 = 프로젝트_생성(프로젝트_생성_요청, 기본_로그인_토큰);

        //then
        assertThat(프로젝트_생성_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        final ErrorResponse errorResponse = 프로젝트_생성_응답.as(ErrorResponse.class);
        assertThat(errorResponse.message()).isEqualTo("프로젝트 이름의 길이가 적절하지 않습니다.");
    }

    @Test
    void 프로젝트_생성_시_노드_별_기간_수와_피드_노드의_수가_맞지_않을때() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = Collections.emptyList();
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(기본_피드_아이디, 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        //when
        final ExtractableResponse<Response> 프로젝트_생성_응답 = 프로젝트_생성(프로젝트_생성_요청, 기본_로그인_토큰);

        //then
        assertThat(프로젝트_생성_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        final ErrorResponse errorResponse = 프로젝트_생성_응답.as(ErrorResponse.class);
        assertThat(errorResponse.message()).isEqualTo("모든 노드에 대해 기간이 설정돼야 합니다.");
    }

    @Test
    void 프로젝트_생성_시_제한_인원이_6명_초과일때() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));

        final int 초과된_제한인원 = 10;
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 초과된_제한인원,
                프로젝트_노드_별_기간_요청);

        //when
        final ExtractableResponse<Response> 프로젝트_생성_응답 = 프로젝트_생성(프로젝트_생성_요청, 기본_로그인_토큰);

        //then
        assertThat(프로젝트_생성_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        final ErrorResponse errorResponse = 프로젝트_생성_응답.as(ErrorResponse.class);
        assertThat(errorResponse.message()).isEqualTo("제한 인원 수가 적절하지 않습니다.");
    }

    @Test
    void 프로젝트에_참가_요청을_보낸다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 피드_응답.feedId());

        // when
        final ExtractableResponse<Response> 프로젝트_참가_요청_응답 = 프로젝트_참가_허용(기본_피드_아이디, 신청서_아이디, 기본_로그인_토큰);

        //then
        assertThat(프로젝트_참가_요청_응답.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 존재하지_않는_프로젝트_아이디로_참가_요청을_보내면_예외가_발생한다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final Long 존재하지_않는_프로젝트_아이디 = 1L;
        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        //when
        final ExtractableResponse<Response> 프로젝트_참가_요청_응답 = 프로젝트_참가_허용(존재하지_않는_프로젝트_아이디, 신청서_아이디, 기본_로그인_토큰);

        //then
        final String 예외_메시지 = 프로젝트_참가_요청_응답.asString();

        assertAll(
                () -> assertThat(프로젝트_참가_요청_응답.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value()),
                () -> assertThat(예외_메시지).contains("프로젝트가 존재하지 않는 피드입니다.")
        );
    }

    @Test
    void 인원이_가득_찬_프로젝트에_참가_요청을_보내면_예외가_발생한다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(기본_피드_아이디, 정상적인_프로젝트_이름, 1,
                프로젝트_노드_별_기간_요청);
        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        회원가입(팔로워_회원_가입_요청);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        //when
        final ExtractableResponse<Response> 참가_요청에_대한_응답 = 프로젝트_참가_허용(기본_피드_아이디, 신청서_아이디, 기본_로그인_토큰);

        //then
        final String 예외_메시지 = 참가_요청에_대한_응답.asString();

        assertAll(
                () -> assertThat(참가_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
                () -> assertThat(예외_메시지).contains("제한 인원이 꽉 찬 프로젝트에는 멤버를 추가할 수 없습니다.")
        );
    }

    @Test
    void 회고글_등록을_요청한다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(기본_피드_아이디, 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);
        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);

        final MemoirRequest 회고글_등록_요청 = new MemoirRequest("회고글");

        //when
        final ExtractableResponse<Response> 회고글_등록_응답 = 회고글_등록(프로젝트_아이디, 회고글_등록_요청, 기본_로그인_토큰);

        //then
        assertThat(회고글_등록_응답.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 하루에_두_번_이상_회고글_등록을_요청하는_경우_실패한다() throws IOException {
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(기본_피드_아이디, 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);
        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);


        final MemoirRequest 회고글_등록_요청 = new MemoirRequest("회고글");
        회고글_등록(프로젝트_아이디, 회고글_등록_요청, 기본_로그인_토큰);

        //when
        final ExtractableResponse<Response> 회고글_등록_응답 = 회고글_등록(프로젝트_아이디, 회고글_등록_요청, 기본_로그인_토큰);
        final ErrorResponse 예외_메세지 = 회고글_등록_응답.as(ErrorResponse.class);

        //then
        final List<ProjectMemberResponse> 프로젝트_사용자_응답 = 프로젝트의_사용자_정보를_정렬_기준없이_조회(프로젝트_아이디, 기본_로그인_토큰).as(new TypeRef<>() {
        });

        assertAll(
                () -> assertThat(회고글_등록_응답.statusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST.value()),
                () -> assertThat(예외_메세지.message()).isEqualTo("이미 오늘 회고를 등록하였습니다."),
                () -> assertThat(프로젝트_사용자_응답.get(0).participationRate())
                        .isEqualTo(100 / (double) 정상적인_프로젝트_회고_횟수)
        );
    }

    @Test
    void 진행_중인_노드의_허용된_인증_횟수_이상_요청할_경우_실패한다() throws IOException {
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 1, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(기본_피드_아이디, 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);
        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);


        final MemoirRequest 회고글_등록_요청 = new MemoirRequest("회고글");
        회고글_등록(프로젝트_아이디, 회고글_등록_요청, 기본_로그인_토큰);

        //when
        final ExtractableResponse<Response> 회고글_등록_응답 = 회고글_등록(프로젝트_아이디, 회고글_등록_요청, 기본_로그인_토큰);

        final ErrorResponse 예외_메세지 = 회고글_등록_응답.as(ErrorResponse.class);

        //then
        assertAll(
                () -> assertThat(회고글_등록_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
                () -> assertThat(예외_메세지.message()).isEqualTo("이번 노드에는 최대 " + 1 + "번만 회고를 등록할 수 있습니다.")
        );
    }

    @Test
    void 정상적으로_프로젝트에_투두리스트를_추가한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);
        final Long 프로젝트_아이디 = 정상적인_프로젝트_생성(기본_로그인_토큰, 기본_피드_아이디, 피드_응답.content().nodes().get(0).id());

        final ProjectTodoRequest 프로젝트_투두리스트_추가_요청 = new ProjectTodoRequest(정상적인_프로젝트_투두_컨텐츠, 오늘, 십일_후);

        // when
        final ExtractableResponse<Response> 프로젝트_투두리스트_추가 = 프로젝트_투두리스트_추가(기본_로그인_토큰, 프로젝트_아이디, 프로젝트_투두리스트_추가_요청);

        // then
        assertThat(프로젝트_투두리스트_추가.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        final String header = 프로젝트_투두리스트_추가.response()
                .header(HttpHeaders.LOCATION);
        assertThat(header).contains("/api/projects/1/todos" + header.substring(21));
    }

    @Test
    void 프로젝트에_팔로워가_투두_리스트를_추가할때_예외를_던진다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final MemberJoinRequest 팔로워_회원가입_요청 = new MemberJoinRequest("test2@email.com", "password12!@#$%", "follower",
                PositionType.FRONTEND, DEFAULT_SKILLS);
        회원가입(팔로워_회원가입_요청);
        final String 팔로워_로그인_토큰 = String.format(BEARER_TOKEN_FORMAT,
                로그인(new LoginRequest(팔로워_회원가입_요청.email(), 팔로워_회원가입_요청.password())).accessToken());

        final Long 프로젝트_아이디 = 정상적인_프로젝트_생성(기본_로그인_토큰, 피드_응답.feedId(), 피드_응답.content().nodes().get(0).id());

        final ProjectTodoRequest 프로젝트_투두리스트_추가_요청 = new ProjectTodoRequest(정상적인_프로젝트_투두_컨텐츠, 오늘, 십일_후);

        // when
        final ExtractableResponse<Response> 프로젝트_투두리스트_추가 = 프로젝트_투두리스트_추가(팔로워_로그인_토큰, 프로젝트_아이디, 프로젝트_투두리스트_추가_요청);

        // then
        final ErrorResponse 프로젝트_투두리스트_추가_바디 = 프로젝트_투두리스트_추가.as(new TypeRef<>() {
        });

        assertThat(프로젝트_투두리스트_추가.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(프로젝트_투두리스트_추가_바디).isEqualTo(new ErrorResponse("프로젝트의 리더만 투두리스트를 추가할 수 있습니다."));
    }

    @Test
    void 종료된_프로젝트에_투두_리스트를_추가할때_예외를_던진다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 프로젝트_아이디 = 정상적인_프로젝트_생성(기본_로그인_토큰, 기본_피드_아이디, 피드_응답.content().nodes().get(0).id());
        testTransactionService.프로젝트를_완료시킨다(프로젝트_아이디);

        final ProjectTodoRequest 프로젝트_투두_리스트_추가_요청 = new ProjectTodoRequest(정상적인_프로젝트_투두_컨텐츠, 오늘, 십일_후);

        //when
        final ExtractableResponse<Response> 프로젝트_추가_응답 = 프로젝트_투두리스트_추가(기본_로그인_토큰, 프로젝트_아이디, 프로젝트_투두_리스트_추가_요청);

        //then
        final ErrorResponse 프로젝트_추가_응답_바디 = 프로젝트_추가_응답.as(new TypeRef<>() {
        });

        assertThat(프로젝트_추가_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(프로젝트_추가_응답_바디).isEqualTo(new ErrorResponse("이미 종료된 프로젝트입니다."));
    }

    @Test
    void 프로젝트_투두_리스트를_체크한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 프로젝트_아이디 = 정상적인_프로젝트_생성(기본_로그인_토큰, 기본_피드_아이디, 피드_응답.content().nodes().get(0).id());
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);
        final Long 투두_아이디 = 프로젝트_투두리스트_추가후_아이디를_반환한다(기본_로그인_토큰, 프로젝트_아이디);

        // when
        final ProjectToDoCheckResponse 프로젝트_투두리스트_체크_응답값 = 프로젝트_투두리스트를_체크한다(기본_로그인_토큰, 프로젝트_아이디, 투두_아이디)
                .as(new TypeRef<>() {
                });

        // then
        final ProjectToDoCheckResponse 예상하는_프로젝트_투두리스트_체크_응답값 = new ProjectToDoCheckResponse(true);
        assertThat(프로젝트_투두리스트_체크_응답값)
                .isEqualTo(예상하는_프로젝트_투두리스트_체크_응답값);
    }

    @Test
    void 프로젝트_투두리스트_체크를_해제한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 프로젝트_아이디 = 정상적인_프로젝트_생성(기본_로그인_토큰, 기본_피드_아이디, 피드_응답.content().nodes().get(0).id());
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);
        final Long 투두_아이디 = 프로젝트_투두리스트_추가후_아이디를_반환한다(기본_로그인_토큰, 프로젝트_아이디);

        프로젝트_투두리스트를_체크한다(기본_로그인_토큰, 프로젝트_아이디, 투두_아이디);

        // when
        final ProjectToDoCheckResponse 두번째_프로젝트_투두리스트_체크_응답값 = 프로젝트_투두리스트를_체크한다(기본_로그인_토큰, 프로젝트_아이디, 투두_아이디)
                .as(new TypeRef<>() {
                });

        // then
        final ProjectToDoCheckResponse 예상하는_프로젝트_투두리스트_체크_응답값 = new ProjectToDoCheckResponse(false);
        assertThat(두번째_프로젝트_투두리스트_체크_응답값)
                .isEqualTo(예상하는_프로젝트_투두리스트_체크_응답값);
    }

    @Test
    void 프로젝트_투두리스트_체크시_프로젝트가_존재하지_않으면_예외가_발생한다() {
        // given
        // when
        final ErrorResponse 에러_응답 = 프로젝트_투두리스트를_체크한다(기본_로그인_토큰, 1L, 1L)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(에러_응답)
                .isEqualTo(new ErrorResponse("프로젝트가 존재하지 않습니다. projectId = 1"));
    }

    @Test
    void 프로젝트_투두리스트_체크시_사용자가_없으면_예외가_발생한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final Long 프로젝트_아이디 = 정상적인_프로젝트_생성(기본_로그인_토큰, 기본_피드_아이디, 피드_응답.content().nodes().get(0).id());
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);
        final Long 투두_아이디 = 프로젝트_투두리스트_추가후_아이디를_반환한다(기본_로그인_토큰, 프로젝트_아이디);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        // when
        final ErrorResponse 에러_응답 = 프로젝트_투두리스트를_체크한다(팔로워_액세스_토큰, 프로젝트_아이디, 투두_아이디)
                .as(new TypeRef<>() {
                });

        // then
        assertThat(에러_응답)
                .isEqualTo(new ErrorResponse("프로젝트에 회원이 존재하지 않습니다. projectId = " + 프로젝트_아이디 +
                        " memberEmail = " + 팔로워_회원_가입_요청.email()));
    }

    @Test
    void 정상적으로_모집중인_프로젝트를_나간다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(프로젝트_아이디, 신청서_아이디, 팔로워_액세스_토큰);

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 기본_로그인_토큰);

        // then
        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 정상적으로_완료된_프로젝트를_나간다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 피드_응답.feedId());

        프로젝트_참가_허용(프로젝트_아이디, 신청서_아이디, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);

        testTransactionService.프로젝트를_완료시킨다(프로젝트_아이디);

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 기본_로그인_토큰);

        // then
        final MemberProjectResponse 사용자_프로젝트_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(팔로워_액세스_토큰, 프로젝트_아이디);
        final List<ProjectMemberResponse> 프로젝트_사용자_응답 = 프로젝트의_사용자_정보를_정렬_기준없이_조회(프로젝트_아이디, 팔로워_액세스_토큰).as(new TypeRef<>() {
        });
        final MemberInformationResponse 팔로워_사용자_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(팔로워_액세스_토큰).as(new TypeRef<>() {
        });

        final ProjectMemberResponse 예상하는_프로젝트_사용자_정보 = new ProjectMemberResponse(팔로워_사용자_정보.id(),
                팔로워_사용자_정보.nickname(), 팔로워_사용자_정보.profileImageUrl(), 0D,
                팔로워_사용자_정보.position(), 팔로워_사용자_정보.skills());

        assertThat(사용자_프로젝트_응답값.leaderId()).isEqualTo(팔로워_사용자_정보.id());
        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(프로젝트_사용자_응답).isEqualTo(List.of(예상하는_프로젝트_사용자_정보));
    }

    @Test
    void 프로젝트를_나갈때_존재하지_않는_프로젝트일_경우_예외가_발생한다() throws JsonProcessingException {
        //given
        final Long 존재하지_않는_프로젝트_아이디 = 1L;

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(존재하지_않는_프로젝트_아이디, 기본_로그인_토큰);

        // then
        final ErrorResponse 프로젝트_생성_응답_바디 = 프로젝트_나가기_요청에_대한_응답.as(new TypeRef<>() {
        });

        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(프로젝트_생성_응답_바디.message()).isEqualTo("존재하지 않는 프로젝트입니다. projectId = 1");
    }

    @Test
    void 모집중인_프로젝트를_나갈때_참여하지_않은_프로젝트일_경우_예외가_발생한다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        final Long 팔로워_아이디 = 회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 팔로워_액세스_토큰);

        // then
        final ErrorResponse 프로젝트_생성_응답_바디 = 프로젝트_나가기_요청에_대한_응답.as(new TypeRef<>() {
        });

        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(프로젝트_생성_응답_바디.message()).isEqualTo("프로젝트에 참여한 사용자가 아닙니다. memberId = " + 팔로워_아이디);
    }

    @Test
    void 완료된_프로젝트를_나갈때_참여하지_않은_프로젝트일_경우_예외가_발생한다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        final Long 팔로워_아이디 = 회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);

        testTransactionService.프로젝트를_완료시킨다(프로젝트_아이디);

        // when

        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 팔로워_액세스_토큰);

        // then
        final ErrorResponse 프로젝트_생성_응답_바디 = 프로젝트_나가기_요청에_대한_응답.as(new TypeRef<>() {
        });

        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(프로젝트_생성_응답_바디.message()).isEqualTo("프로젝트에 참여한 사용자가 아닙니다. memberId = " + 팔로워_아이디);
    }

    @Test
    void 프로젝트를_나갈때_프로젝트가_진행중이면_예외가_발생한다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 기본_로그인_토큰);

        // then
        final ErrorResponse 프로젝트_생성_응답_바디 = 프로젝트_나가기_요청에_대한_응답.as(new TypeRef<>() {
        });

        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(프로젝트_생성_응답_바디.message()).isEqualTo("진행중인 프로젝트에서는 나갈 수 없습니다.");
    }

    @Test
    void 모집중인_프로젝트를_나갈때_리더가_나가면_프로젝트가_삭제된다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워1_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follow1", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워1_로그인_요청 = new LoginRequest(팔로워1_회원_가입_요청.email(), 팔로워1_회원_가입_요청.password());
        회원가입(팔로워1_회원_가입_요청);
        final String 팔로워1_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워1_로그인_요청).accessToken());
        final Long 팔로워1_신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워1_액세스_토큰, 피드_응답.feedId());

        final MemberJoinRequest 팔로워2_회원_가입_요청 = new MemberJoinRequest("test3@email.com", "paswword2@",
                "follow2", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워2_로그인_요청 = new LoginRequest(팔로워2_회원_가입_요청.email(), 팔로워2_회원_가입_요청.password());
        회원가입(팔로워2_회원_가입_요청);
        final String 팔로워2_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워2_로그인_요청).accessToken());
        final Long 팔로워2_신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워2_액세스_토큰, 피드_응답.feedId());

        프로젝트_참가_허용(프로젝트_아이디, 팔로워1_신청서_아이디, 기본_로그인_토큰);
        프로젝트_참가_허용(프로젝트_아이디, 팔로워2_신청서_아이디, 기본_로그인_토큰);

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 기본_로그인_토큰);

        // then
        final ExtractableResponse<Response> 프로젝트_목록_조회_요청에_대한_응답 = 프로젝트_목록_조회_요청(1L, null, 2,
                ProjectFilterTypeDto.LATEST.name());
        final FeedProjectResponses 프로젝트_목록 = 프로젝트_목록_조회_요청에_대한_응답.as(new TypeRef<>() {
        });

        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(프로젝트_목록.responses()).hasSize(0);
    }

    @Test
    void 완료된_프로젝트를_나갈때_리더가_나가면_다음으로_들어온_사용자가_리더가_된다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워1_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follow1", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워1_로그인_요청 = new LoginRequest(팔로워1_회원_가입_요청.email(), 팔로워1_회원_가입_요청.password());
        회원가입(팔로워1_회원_가입_요청);
        final String 팔로워1_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워1_로그인_요청).accessToken());
        final Long 팔로워1_신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워1_액세스_토큰, 기본_피드_아이디);
        final MemberInformationResponse 팔로워1_사용자_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(팔로워1_액세스_토큰).as(new TypeRef<>() {
        });


        final MemberJoinRequest 팔로워2_회원_가입_요청 = new MemberJoinRequest("email3@email.com", "paswword2@",
                "follow2", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워2_로그인_요청 = new LoginRequest(팔로워2_회원_가입_요청.email(), 팔로워2_회원_가입_요청.password());
        회원가입(팔로워2_회원_가입_요청);
        final String 팔로워2_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워2_로그인_요청).accessToken());
        final Long 팔로워2_신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워2_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_피드_아이디, 팔로워1_신청서_아이디, 기본_로그인_토큰);
        프로젝트_참가_허용(기본_피드_아이디, 팔로워2_신청서_아이디, 기본_로그인_토큰);

        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);

        testTransactionService.프로젝트를_완료시킨다(프로젝트_아이디);

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 기본_로그인_토큰);

        // then
        final MemberProjectResponse 사용자_프로젝트_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(팔로워1_액세스_토큰, 프로젝트_아이디);

        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(사용자_프로젝트_응답값.leaderId()).isEqualTo(팔로워1_사용자_정보.id());
    }

    @Test
    void 모집중인_프로젝트를_나갈때_팔로워가_나가면_리더는_변하지_않는다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_피드_아이디, 신청서_아이디, 기본_로그인_토큰);

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 팔로워_액세스_토큰);

        // then
        final MemberProjectResponse 사용자_프로젝트_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(기본_로그인_토큰, 프로젝트_아이디);
        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(사용자_프로젝트_응답값.leaderId()).isEqualTo(기본_회원_아이디);
    }

    @Test
    void 완료된_프로젝트를_나갈때_팔로워가_나가면_리더는_변하지_않는다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final Long 신청서_아이디 = 신청서를_생성하고_아이디를_반환한다 (피드_기본_신청서_생성_요청, 팔로워_액세스_토큰, 기본_피드_아이디);

        프로젝트_참가_허용(기본_피드_아이디, 신청서_아이디, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);

        testTransactionService.프로젝트를_완료시킨다(프로젝트_아이디);

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 팔로워_액세스_토큰);

        // then
        final MemberProjectResponse 사용자_프로젝트_응답값 = 사용자의_특정_프로젝트_정보를_조회한다(기본_로그인_토큰, 프로젝트_아이디);
        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(사용자_프로젝트_응답값.leaderId()).isEqualTo(기본_회원_아이디);
    }

    @Test
    void 모집중인_프로젝트를_나갈때_남은_사용자가_없으면_프로젝트는_삭제된다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        // when
        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 기본_로그인_토큰);

        // then
        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        final ExtractableResponse<Response> 프로젝트_목록_조회_요청에_대한_응답 = 프로젝트_목록_조회_요청(1L, null, 1,
                ProjectFilterTypeDto.LATEST.name());
        final FeedProjectResponses 프로젝트_목록 = 프로젝트_목록_조회_요청에_대한_응답.as(new TypeRef<>() {
        });
        assertThat(프로젝트_목록.responses()).hasSize(0);
    }

    @Test
    void 완료된_프로젝트를_나갈때_남은_사용자가_없으면_프로젝트는_삭제된다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);
        프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);
        testTransactionService.프로젝트를_완료시킨다(프로젝트_아이디);

        // when

        final ExtractableResponse<Response> 프로젝트_나가기_요청에_대한_응답 = 프로젝트_나가기_요청(프로젝트_아이디, 기본_로그인_토큰);

        // then
        assertThat(프로젝트_나가기_요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        final ExtractableResponse<Response> 프로젝트_목록_조회_요청에_대한_응답 = 프로젝트_목록_조회_요청(1L, null, 1,
                ProjectFilterTypeDto.LATEST.name());
        final FeedProjectResponses 프로젝트_목록 = 프로젝트_목록_조회_요청에_대한_응답.as(new TypeRef<>() {
        });
        assertThat(프로젝트_목록.responses()).hasSize(0);
    }

    @Test
    void 프로젝트를_정상적으로_시작한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        // when
        final ExtractableResponse<Response> 프로젝트_시작_요청_응답 = 프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);

        // then
        final List<ProjectMemberResponse> 프로젝트_사용자_정보 = 프로젝트의_사용자_정보를_정렬_기준없이_조회(프로젝트_아이디, 기본_로그인_토큰).as(new TypeRef<>() {
        });
        final MemberInformationResponse 사용자_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(기본_로그인_토큰).as(new TypeRef<>() {
        });
        final ProjectMemberResponse 예상하는_프로젝트_사용자_정보 = new ProjectMemberResponse(사용자_정보.id(),
                사용자_정보.nickname(), 사용자_정보.profileImageUrl(), 0D, 사용자_정보.position(), 사용자_정보.skills());

        assertThat(프로젝트_시작_요청_응답.statusCode())
                .isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(프로젝트_사용자_정보)
                .isEqualTo(List.of(예상하는_프로젝트_사용자_정보));
    }

    @Test
    void 프로젝트를_시작하는_사용자가_프로젝트의_리더가_아니면_예외가_발생한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);

        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 다른_사용자_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        회원가입(다른_사용자_회원_가입_요청);
        final LoginRequest 다른_사용자_로그인_요청 = new LoginRequest(다른_사용자_회원_가입_요청.email(), 다른_사용자_회원_가입_요청.password());
        final String 다른_사용자_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(다른_사용자_로그인_요청).accessToken());

        // when
        final ExtractableResponse<Response> 프로젝트_시작_요청_응답 = 프로젝트를_시작한다(다른_사용자_액세스_토큰, 프로젝트_아이디);

        // then
        final ErrorResponse errorResponse = 프로젝트_시작_요청_응답.as(ErrorResponse.class);
        assertThat(프로젝트_시작_요청_응답.statusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.message())
                .isEqualTo("프로젝트의 리더만 프로젝트를 시작할 수 있습니다.");
    }

    @Test
    void 프로젝트_시작시_프로젝트의_시작날짜가_미래라면_예외가_발생한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(),
                        (int) ChronoUnit.DAYS.between(십일_후, 이십일_후), 십일_후, 이십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);
        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);

        // when
        final ExtractableResponse<Response> 프로젝트_시작_요청_응답 = 프로젝트를_시작한다(기본_로그인_토큰, 프로젝트_아이디);

        // then
        final ErrorResponse errorResponse = 프로젝트_시작_요청_응답.as(ErrorResponse.class);
        assertThat(프로젝트_시작_요청_응답.statusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.message())
                .isEqualTo("프로젝트의 시작 날짜가 되지 않았습니다.");
    }
}
