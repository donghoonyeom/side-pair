package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static sidepair.integration.fixture.AuthenticationAPIFixture.로그인;
import static sidepair.integration.fixture.CommonFixture.BEARER_TOKEN_FORMAT;
import static sidepair.integration.fixture.FeedAPIFixture.신청서를_생성한다;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;
import static sidepair.integration.fixture.MemberAPIFixture.회원가입;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.PositionType;

class FeedApplicantCreateIntegrationTest extends InitIntegrationTest {

    @Test
    void 피드_신청서를_생성한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        final FeedApplicantSaveRequest 피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");

        // when
        final ExtractableResponse<Response> 신청서_생성_요청_결과 = 신청서를_생성한다(팔로워_액세스_토큰, 피드_아이디, 피드_신청서_생성_요청);

        // then
        assertThat(신청서_생성_요청_결과.statusCode())
                .isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 피드_신청서_생성시_내용이_1000자가_넘으면_예외가_발생한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        final String 엄청_긴_신청서_내용 = "a".repeat(1001);
        final FeedApplicantSaveRequest 피드_신청서_생성_요청 = new FeedApplicantSaveRequest(엄청_긴_신청서_내용);

        // when
        final ExtractableResponse<Response> 신청서_생성_요청_결과 = 신청서를_생성한다(팔로워_액세스_토큰, 피드_아이디, 피드_신청서_생성_요청);

        // then
        final ErrorResponse 예외_응답 = 신청서_생성_요청_결과.as(ErrorResponse.class);
        assertThat(신청서_생성_요청_결과.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(예외_응답.message()).isEqualTo("신청서는 최대 1000글자까지 입력할 수 있습니다.");
    }

    @Test
    void 피드_신청서_생성시_존재하지_않은_피드이면_예외가_발생한다() {
        // given
        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        final FeedApplicantSaveRequest 피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");

        // when
        final ExtractableResponse<Response> 신청서_생성_요청_결과 = 신청서를_생성한다(팔로워_액세스_토큰, 1L, 피드_신청서_생성_요청);

        // then
        final ErrorResponse 예외_응답 = 신청서_생성_요청_결과.as(ErrorResponse.class);
        assertThat(신청서_생성_요청_결과.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(예외_응답.message()).isEqualTo("존재하지 않는 피드입니다. feedId = 1");
    }

    @Test
    void 피드_신청서_생성시_피드_생성자가_신청서를_보내려고_하면_예외가_발생한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);

        회원가입(팔로워_회원_가입_요청);

        final FeedApplicantSaveRequest 피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");

        // when
        final ExtractableResponse<Response> 신청서_생성_요청_결과 = 신청서를_생성한다(기본_로그인_토큰, 피드_아이디, 피드_신청서_생성_요청);

        // then
        final ErrorResponse 예외_응답 = 신청서_생성_요청_결과.as(ErrorResponse.class);
        assertThat(신청서_생성_요청_결과.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(예외_응답.message()).isEqualTo("피드 생성자는 신청서를 보낼 수 없습니다. feedId = " + 피드_아이디 +
                " memberId = " + 기본_회원_아이디);
    }

    @Test
    void 피드_신청서_생성시_이미_신청서를_단적이_있으면_예외가_발생한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        final MemberJoinRequest 팔로워_회원_가입_요청 = new MemberJoinRequest("test2@email.com", "paswword2@",
                "follower", PositionType.BACKEND, DEFAULT_SKILLS);
        final LoginRequest 팔로워_로그인_요청 = new LoginRequest(팔로워_회원_가입_요청.email(), 팔로워_회원_가입_요청.password());
        final Long 팔로워_아이디 = 회원가입(팔로워_회원_가입_요청);
        final String 팔로워_액세스_토큰 = String.format(BEARER_TOKEN_FORMAT, 로그인(팔로워_로그인_요청).accessToken());

        final FeedApplicantSaveRequest 피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");

        // when
        신청서를_생성한다(팔로워_액세스_토큰, 피드_아이디, 피드_신청서_생성_요청);

        // when
        final ExtractableResponse<Response> 두번째_신청서_생성_요청결과 = 신청서를_생성한다(팔로워_액세스_토큰, 피드_아이디, 피드_신청서_생성_요청);

        // then
        final ErrorResponse 예외_응답 = 두번째_신청서_생성_요청결과.as(ErrorResponse.class);
        assertThat(두번째_신청서_생성_요청결과.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(예외_응답.message()).isEqualTo("이미 작성한 신청서가 존재합니다. feedId = " + 피드_아이디 +
                " memberId = " + 팔로워_아이디);
    }
}
