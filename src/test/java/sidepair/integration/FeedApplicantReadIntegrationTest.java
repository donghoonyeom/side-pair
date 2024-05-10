package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static sidepair.integration.fixture.AuthenticationAPIFixture.로그인;
import static sidepair.integration.fixture.CommonFixture.BEARER_TOKEN_FORMAT;
import static sidepair.integration.fixture.FeedAPIFixture.신청서를_생성한다;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드_신청서를_조회한다;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;
import static sidepair.integration.fixture.MemberAPIFixture.요청을_받는_사용자_자신의_정보_조회_요청;
import static sidepair.integration.fixture.MemberAPIFixture.회원가입;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.service.dto.CustomScrollRequest;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.feed.response.FeedApplicantResponse;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.PositionType;
import sidepair.service.dto.mamber.response.MemberInformationResponse;
import sidepair.service.dto.mamber.response.MemberResponse;
import sidepair.service.dto.mamber.response.MemberSkillResponse;

class FeedApplicantReadIntegrationTest extends InitIntegrationTest {

    @Test
    void 피드에_대한_신청서를_최신순으로_조회한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test0@email.com", "paswword2@",
                "follow", PositionType.FRONTEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());
        final MemberInformationResponse 팔로워_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(팔로워_액세스_토큰).as(new TypeRef<>() {
        });

        final MemberJoinRequest 팔로워1_회원_가입_요청 = new MemberJoinRequest("test1@email.com", "paswword2@",
                "follow1", PositionType.FRONTEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워1_로그인_요청 = new LoginRequest(팔로워1_회원_가입_요청.email(), 팔로워1_회원_가입_요청.password());
        회원가입(팔로워1_회원_가입_요청);
        final String 팔로워1_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워1_로그인_요청).accessToken());
        final MemberInformationResponse 팔로워1_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(팔로워1_액세스_토큰).as(new TypeRef<>() {
        });

        final MemberJoinRequest 팔로워2_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follow2", PositionType.FRONTEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워2_로그인_요청 = new LoginRequest(팔로워2_회원_가입_요청.email(), 팔로워2_회원_가입_요청.password());
        회원가입(팔로워2_회원_가입_요청);
        final String 팔로워2_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워2_로그인_요청).accessToken());
        final MemberInformationResponse 팔로워2_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(팔로워2_액세스_토큰).as(new TypeRef<>() {
        });

        final FeedApplicantSaveRequest 팔로워_피드_신청서_생성_요청 = new FeedApplicantSaveRequest("팔로워 신청서 내용");
        final FeedApplicantSaveRequest 팔로워1_피드_신청서_생성_요청 = new FeedApplicantSaveRequest("팔로워1 신청서 내용");
        final FeedApplicantSaveRequest 팔로워2_피드_신청서_생성_요청 = new FeedApplicantSaveRequest("팔로워2 신청서 내용");
        신청서를_생성한다(팔로워_액세스_토큰, 피드_아이디, 팔로워_피드_신청서_생성_요청);
        신청서를_생성한다(팔로워1_액세스_토큰, 피드_아이디, 팔로워1_피드_신청서_생성_요청);
        신청서를_생성한다(팔로워2_액세스_토큰, 피드_아이디, 팔로워2_피드_신청서_생성_요청);

        // when
        final CustomScrollRequest 첫번째_스크롤_요청 = new CustomScrollRequest(null, 2);
        final ExtractableResponse<Response> 첫번째_피드_신청서_조회_응답 = 피드_신청서를_조회한다(기본_로그인_토큰, 피드_아이디, 첫번째_스크롤_요청);
        final List<FeedApplicantResponse> 첫번째_피드_신청서_조회_응답값 = jsonToClass(첫번째_피드_신청서_조회_응답.asString(),
                new TypeReference<>() {
                });

        final CustomScrollRequest 두번째_스크롤_요청 = new CustomScrollRequest(첫번째_피드_신청서_조회_응답값.get(1).id(), 2);
        final ExtractableResponse<Response> 두번째_피드_신청서_조회_응답 = 피드_신청서를_조회한다(기본_로그인_토큰, 피드_아이디, 두번째_스크롤_요청);
        final List<FeedApplicantResponse> 두번째_피드_신청서_조회_응답값 = jsonToClass(두번째_피드_신청서_조회_응답.asString(),
                new TypeReference<>() {
                });

        // then
        final List<FeedApplicantResponse> 첫번째_피드_신청서_조회_요청_예상값 = List.of(
                new FeedApplicantResponse(3L, new MemberResponse(4L, "follow2", 팔로워2_정보.profileImageUrl(),
                        PositionType.FRONTEND.name(),
                        List.of(new MemberSkillResponse(7L, "Spring"), new MemberSkillResponse(8L, "Java"))),
                        LocalDateTime.now(), "팔로워2 신청서 내용"),
                new FeedApplicantResponse(2L, new MemberResponse(3L, "follow1", 팔로워1_정보.profileImageUrl(),
                        PositionType.FRONTEND.name(),
                        List.of(new MemberSkillResponse(5L, "Spring"), new MemberSkillResponse(6L, "Java"))),
                        LocalDateTime.now(), "팔로워1 신청서 내용"));

        final List<FeedApplicantResponse> 두번째_피드_신청서_조회_요청_예상값 = List.of(
                new FeedApplicantResponse(1L, new MemberResponse(2L, "follow", 팔로워_정보.profileImageUrl(),
                        PositionType.FRONTEND.name(),
                        List.of(new MemberSkillResponse(3L, "Spring"), new MemberSkillResponse(4L, "Java"))),
                        LocalDateTime.now(), "팔로워 신청서 내용"));

        assertAll(
                () -> assertThat(첫번째_피드_신청서_조회_응답값)
                        .usingRecursiveComparison()
                        .ignoringFields("createdAt")
                        .isEqualTo(첫번째_피드_신청서_조회_요청_예상값),
                () -> assertThat(두번째_피드_신청서_조회_응답값)
                        .usingRecursiveComparison()
                        .ignoringFields("createdAt")
                        .isEqualTo(두번째_피드_신청서_조회_요청_예상값)
        );
    }

    @Test
    void 피드_신청서_조회_요청_시_작성된_신청서가_없다면_빈_값을_반환한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);


        final CustomScrollRequest 스크롤_요청 = new CustomScrollRequest(null, 10);

        // when
        final ExtractableResponse<Response> 피드_신청서_조회_응답 = 피드_신청서를_조회한다(기본_로그인_토큰, 피드_아이디, 스크롤_요청);

        // then
        final List<FeedApplicantResponse> 피드_신청서_조회_응답값 = jsonToClass(피드_신청서_조회_응답.asString(),
                new TypeReference<>() {
                });

        assertThat(피드_신청서_조회_응답값).isEmpty();
    }

    @Test
    void 피드_신청서_조회_요청_시_유효하지_않은_피드_아이디로_요청_시_예외를_반환한다() throws JsonProcessingException {

        //when
        final CustomScrollRequest 스크롤_요청 = new CustomScrollRequest(null, 10);

        // when
        final ExtractableResponse<Response> 피드_신청서_조회_응답 = 피드_신청서를_조회한다(기본_로그인_토큰, 1L, 스크롤_요청);

        // then
        final ErrorResponse 피드_신청서_조회_응답값 = jsonToClass(피드_신청서_조회_응답.asString(),
                new TypeReference<>() {
                });

        assertAll(
                () -> assertThat(피드_신청서_조회_응답.statusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()),
                () -> assertThat(피드_신청서_조회_응답값.message())
                        .isEqualTo("존재하지 않는 피드입니다. feedId = 1")
        );
    }

    @Test
    void 피드_신청서_조회_요청_시_피드_작성자가_아닐_시_예외를_반환한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test0@email.com", "paswword2@",
                "follow", PositionType.FRONTEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        final FeedApplicantSaveRequest 팔로워_피드_신청서_생성_요청 = new FeedApplicantSaveRequest("팔로워 신청서 내용");
        신청서를_생성한다(팔로워_액세스_토큰, 피드_아이디, 팔로워_피드_신청서_생성_요청);

        //when
        final CustomScrollRequest 스크롤_요청 = new CustomScrollRequest(null, 10);

        // when
        final ExtractableResponse<Response> 피드_신청서_조회_응답 = 피드_신청서를_조회한다(팔로워_액세스_토큰, 피드_아이디, 스크롤_요청);

        // then
        final ErrorResponse 피드_신청서_조회_응답값 = jsonToClass(피드_신청서_조회_응답.asString(),
                new TypeReference<>() {
                });

        assertAll(
                () -> assertThat(피드_신청서_조회_응답.statusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN.value()),
                () -> assertThat(피드_신청서_조회_응답값.message())
                        .isEqualTo("해당 피드를 생성한 사용자가 아닙니다.")
        );
    }
}
