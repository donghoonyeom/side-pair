package sidepair.integration.fixture;

import static io.restassured.RestAssured.given;
import static sidepair.integration.fixture.CommonFixture.API_PREFIX;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_EMAIL;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_PASSWORD;


import org.springframework.http.MediaType;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.auth.request.ReissueTokenRequest;
import sidepair.service.dto.auth.response.AuthenticationResponse;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class AuthenticationAPIFixture {

    public static ExtractableResponse<Response> 응답을_반환하는_로그인(final LoginRequest 로그인_요청) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .body(로그인_요청)
                .post(API_PREFIX + "/auth/login")
                .then()
                .log().all()
                .extract();
    }

    public static AuthenticationResponse 로그인(final LoginRequest 로그인_요청) {
        return 응답을_반환하는_로그인(로그인_요청).as(AuthenticationResponse.class);
    }

    public static AuthenticationResponse 기본_로그인() {
        final LoginRequest request = new LoginRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        return 로그인(request);
    }

    public static ExtractableResponse<Response> 토큰_재발행(final ReissueTokenRequest 토큰_재발행_요청) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .body(토큰_재발행_요청)
                .post(API_PREFIX + "/auth/reissue")
                .then()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .log().all()
                .extract();
    }
}
