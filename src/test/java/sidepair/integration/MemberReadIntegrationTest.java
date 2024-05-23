package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_EMAIL;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_NICKNAME;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_POSITION_TYPE;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;
import static sidepair.integration.fixture.MemberAPIFixture.요청을_받는_사용자_자신의_정보_조회_요청;
import static sidepair.integration.fixture.MemberAPIFixture.요청을_받는_특정_사용자의_정보_조회;
import static sidepair.integration.fixture.MemberAPIFixture.회원가입;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import sidepair.domain.member.Position;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.response.MemberInformationForPublicResponse;
import sidepair.service.dto.mamber.response.MemberInformationResponse;
import sidepair.service.dto.mamber.response.MemberSkillResponse;

class MemberReadIntegrationTest extends InitIntegrationTest {

    @Test
    void 로그인한_사용자_자신의_정보를_성공적으로_조회한다() throws JsonProcessingException {
        // given
        // when
        final ExtractableResponse<Response> 사용자_자신의_정보_조회_응답 = 요청을_받는_사용자_자신의_정보_조회_요청(기본_로그인_토큰);

        // then
        final MemberInformationResponse 사용자_자신의_정보_조회_응답_바디 = jsonToClass(사용자_자신의_정보_조회_응답.asString(),
                new TypeReference<>() {
                });
        final MemberInformationResponse 예상하는_응답값 = new MemberInformationResponse(기본_회원_아이디, DEFAULT_NICKNAME, null,
                DEFAULT_POSITION_TYPE.name(),
                List.of(new MemberSkillResponse(1L, "Spring"), new MemberSkillResponse(2L, "Java")),
                DEFAULT_EMAIL);

        assertThat(사용자_자신의_정보_조회_응답_바디).usingRecursiveComparison()
                .ignoringFields("profileImageUrl")
                .isEqualTo(예상하는_응답값);
    }

    @Test
    void 특정_사용자의_정보를_성공적으로_조회한다() throws JsonProcessingException {
        // given
        final MemberJoinRequest 다른_회원의_가입_요청 = new MemberJoinRequest("test2@email.com", "password2!",
                "hello", DEFAULT_POSITION_TYPE, DEFAULT_SKILLS);
        final Long 다른_회원_아이디 = 회원가입(다른_회원의_가입_요청);

        // when
        final ExtractableResponse<Response> 특정_사용자의_정보_조회_응답 = 요청을_받는_특정_사용자의_정보_조회(기본_로그인_토큰, 다른_회원_아이디);

        // then
        final MemberInformationForPublicResponse 특정_사용자의_정보_조회_응답_바디 = jsonToClass(특정_사용자의_정보_조회_응답.asString(),
                new TypeReference<>() {
                });
        final MemberInformationForPublicResponse 예상하는_응답값 = new MemberInformationForPublicResponse("hello",
                null,
                Position.BACKEND.name(),
                List.of(new MemberSkillResponse(3L, "Spring"), new MemberSkillResponse(4L, "Java")));

        assertThat(특정_사용자의_정보_조회_응답_바디).usingRecursiveComparison()
                .ignoringFields("profileImageUrl")
                .isEqualTo(예상하는_응답값);
    }

    @Test
    void 특정_사용자의_정보를_조회시_존재하지_않는_회원이면_실패한다() throws JsonProcessingException {
        // given
        // when
        final ExtractableResponse<Response> 특정_사용자의_정보_조회_응답 = 요청을_받는_특정_사용자의_정보_조회(기본_로그인_토큰, 2L);

        // then
        final ErrorResponse 에러_메세지 = jsonToClass(특정_사용자의_정보_조회_응답.asString(),
                new TypeReference<>() {
                });
        assertThat(특정_사용자의_정보_조회_응답.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(에러_메세지.message()).isEqualTo("존재하지 않는 회원입니다. memberId = 2");
    }
}

