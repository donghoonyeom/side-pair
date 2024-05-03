package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;
import static sidepair.integration.fixture.MemberAPIFixture.요청을_받는_회원가입;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.PositionType;

class MemberCreateIntegrationTest extends InitIntegrationTest {

    @Test
    void 정상적으로_회원가입을_성공한다() {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("test1@email.com", "password12!@#$%", "hello",
                PositionType.BACKEND, DEFAULT_SKILLS);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a@email.com", "ab@email.com"})
    void 이메일_길이가_틀린_경우_회원가입에_실패한다(final String 회원_이메일) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest(회원_이메일, "password12!", "nickname", PositionType.BACKEND,
                DEFAULT_SKILLS);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(new TypeRef<>() {
        });
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 이메일입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Abcd!@email.com", "abcde*@email.com", "가나다라@email.com"})
    void 이메일에_허용되지_않은_문자가_들어온_경우_회원가입에_실패한다(final String 회원_이메일) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest(회원_이메일, "password12!", "nickname",
                PositionType.BACKEND, DEFAULT_SKILLS);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(new TypeRef<>() {
        });
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 이메일입니다.");
    }

    @Test
    void 이메일이_중복된_경우_회원가입에_실패한다() {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("test@email.com", "password12!", "hello",
                PositionType.BACKEND, DEFAULT_SKILLS);
        요청을_받는_회원가입(회원가입_요청);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(에러_메세지.message()).isEqualTo("이미 존재하는 이메일입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcde1!", "abcdefghijklmn12"})
    void 비밀번호_길이가_틀린_경우_회원가입에_실패한다(final String password) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("test1@email.com", password, "nickname",
                PositionType.BACKEND, DEFAULT_SKILLS);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("정해진 비밀번호의 양식이 아닙니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdef1/", "abcdefghij1₩", "abcdefgH1!"})
    void 비밀번호에_허용되지_않은_문자가_들어온_경우_회원가입에_실패한다(final String password) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("test1@email.com", password, "nickname",
                PositionType.BACKEND, DEFAULT_SKILLS);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("정해진 비밀번호의 양식이 아닙니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefgh", "abcdefghijkl"})
    void 비밀번호에_영소문자만_들어온_경우_회원가입에_실패한다(final String password) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("test1@email.com", password, "nickname", PositionType.BACKEND,
                DEFAULT_SKILLS);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("정해진 비밀번호의 양식이 아닙니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "12345678910"})
    void 비밀번호에_숫자만_들어온_경우_회원가입에_실패한다(final String password) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("test1@email.com", password, "nickname",
                PositionType.BACKEND, DEFAULT_SKILLS);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("정해진 비밀번호의 양식이 아닙니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "123456789012345678901"})
    void 닉네임_길이가_틀린_경우_회원가입에_실패한다(final String nickname) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("test1@email.com", "password12!@#$%", nickname,
                PositionType.BACKEND, DEFAULT_SKILLS);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 닉네임입니다.");
    }
}
