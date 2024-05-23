package sidepair.integration.fixture;

import static io.restassured.RestAssured.given;
import static sidepair.integration.fixture.CommonFixture.API_PREFIX;
import static sidepair.integration.fixture.CommonFixture.AUTHORIZATION;
import static sidepair.integration.fixture.CommonFixture.LOCATION;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Header;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.project.request.MemoirRequest;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;
import sidepair.service.dto.project.request.ProjectTodoRequest;

public class ProjectAPIFixture {

    public static final LocalDate 오늘 = LocalDate.now();
    public static final LocalDate 십일_후 = 오늘.plusDays(10L);
    public static final LocalDate 이십일_후 = 십일_후.plusDays(10L);
    public static final LocalDate 삼십일_후 = 이십일_후.plusDays(10L);
    public static final String 정상적인_프로젝트_이름 = "PROJECT_NAME";
    public static final int 정상적인_프로젝트_제한_인원 = 6;
    public static final String 정상적인_프로젝트_투두_컨텐츠 = "PROJECT_TO_DO_CONTENT";
    public static final int 정상적인_프로젝트_회고_횟수 = (int) ChronoUnit.DAYS.between(오늘, 십일_후);

    public static Long 프로젝트를_생성하고_아이디를_반환한다(final ProjectCreateRequest 프로젝트_생성_요청, final String 액세스_토큰) {
        final String 프로젝트_생성_응답_Location_헤더 = 프로젝트_생성(프로젝트_생성_요청, 액세스_토큰).response().getHeader(LOCATION);
        return Long.parseLong(프로젝트_생성_응답_Location_헤더.substring(14));
    }

    public static ExtractableResponse<Response> 프로젝트_생성(final ProjectCreateRequest 프로젝트_생성_요청, final String 액세스_토큰) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .body(프로젝트_생성_요청)
                .header(new Header(HttpHeaders.AUTHORIZATION, 액세스_토큰))
                .post(API_PREFIX + "/projects")
                .then()
                .log().all()
                .extract();
    }

    public static Long 기본_프로젝트_생성(final String 액세스_토큰, final FeedResponse 피드_응답) {
        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.content().id(), 정상적인_프로젝트_이름,
                정상적인_프로젝트_제한_인원, 프로젝트_노드_별_기간_요청);
        final String Location_헤더 = 프로젝트_생성(프로젝트_생성_요청, 액세스_토큰).response().header(LOCATION);
        return Long.parseLong(Location_헤더.substring(14));
    }

    public static ExtractableResponse<Response> 프로젝트_나가기_요청(final Long 프로젝트_아이디, final String 프로젝트_참여자_액세스_토큰) {
        return given()
                .log().all()
                .header(AUTHORIZATION, 프로젝트_참여자_액세스_토큰)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .post(API_PREFIX + "/projects/{projectId}/leave", 프로젝트_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 프로젝트_목록_조회_요청(final Long feedId, final LocalDateTime lastValue,
                                                              final int size,
                                                              final String filterCond) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("feedId", feedId)
                .param("lastCreatedAt", lastValue)
                .param("size", size)
                .param("filterCond", filterCond)
                .when()
                .get(API_PREFIX + "/feeds/{feedId}/projects", feedId)
                .then().log().all()
                .extract();
    }

    public static Long 정상적인_프로젝트_생성(final String 액세스_토큰, final Long 피드_아이디, final Long 피드_노드_아이디) {
        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_노드_아이디, 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_아이디, 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);
        final ExtractableResponse<Response> 프로젝트_생성_응답 = 프로젝트_생성(프로젝트_생성_요청, 액세스_토큰);
        final String Location_헤더 = 프로젝트_생성_응답.response().header("Location");
        return Long.parseLong(Location_헤더.substring(14));
    }

    public static ExtractableResponse<Response> 프로젝트_투두리스트_추가(final String 액세스_토큰, final Long 프로젝트_아이디,
                                                              final ProjectTodoRequest 프로젝트_투두_리스트_추가_요청) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .body(프로젝트_투두_리스트_추가_요청)
                .header(new Header(HttpHeaders.AUTHORIZATION, 액세스_토큰))
                .post(API_PREFIX + "/projects/" + 프로젝트_아이디 + "/todos")
                .then()
                .log().all()
                .extract();
    }

    public static Long 프로젝트_투두리스트_추가후_아이디를_반환한다(final String 로그인_토큰_정보, final Long 프로젝트_아이디) {
        final ProjectTodoRequest 프로젝트_투두리스트_추가_요청 = new ProjectTodoRequest(정상적인_프로젝트_투두_컨텐츠, 오늘, 십일_후);
        final String 응답_헤더값 = 프로젝트_투두리스트_추가(로그인_토큰_정보, 프로젝트_아이디, 프로젝트_투두리스트_추가_요청)
                .response()
                .getHeader(LOCATION)
                .replace(API_PREFIX + "/projects/" + 프로젝트_아이디 + "/todos/", "");
        return Long.valueOf(응답_헤더값);
    }

    public static ExtractableResponse<Response> 프로젝트_투두리스트를_체크한다(final String 로그인_토큰_정보, final Long 프로젝트_아이디,
                                                                 final Long 투두_아이디) {
        return given()
                .header(AUTHORIZATION, 로그인_토큰_정보)
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post(API_PREFIX + "/projects/{projectId}/todos/{todoId}", 프로젝트_아이디, 투두_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회고글_등록(final Long 프로젝트_아이디, final MemoirRequest 회고_등록_요청,
                                                       final String 로그인_토큰) {
        return given().log().all()
                .header(AUTHORIZATION, 로그인_토큰)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(회고_등록_요청)
                .when()
                .post(API_PREFIX + "/projects/{projectId}/memoirs", 프로젝트_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 프로젝트를_시작한다(final String 로그인_토큰, final Long 프로젝트_아이디) {
        final ExtractableResponse<Response> 프로젝트_시작_요청_응답 = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .header(new Header(HttpHeaders.AUTHORIZATION, 로그인_토큰))
                .post(API_PREFIX + "/projects/{projectId}/start", 프로젝트_아이디)
                .then()
                .log().all()
                .extract();
        return 프로젝트_시작_요청_응답;
    }

    public static ExtractableResponse<Response> 프로젝트의_사용자_정보를_정렬_기준없이_조회(final Long 프로젝트_아이디, final String 로그인_토큰) {
        return given().log().all()
                .header(AUTHORIZATION, 로그인_토큰)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX + "/projects/{projectId}/members", 프로젝트_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static MemberProjectResponse 사용자의_특정_프로젝트_정보를_조회한다(final String 로그인_토큰_정보, final Long 프로젝트_아이디) {
        return given().log().all()
                .header(AUTHORIZATION, 로그인_토큰_정보)
                .when()
                .get(API_PREFIX + "/projects/{projectId}/me", 프로젝트_아이디)
                .then()
                .log().all()
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static ExtractableResponse<Response> 회고글_전체_조회_요청(final String 액세스_토큰, final Long 프로젝트_아이디) {
        return given().log().all()
                .header(AUTHORIZATION, 액세스_토큰)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX + "/projects/{projectId}/memoirs", 프로젝트_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 프로젝트_아이디로_프로젝트를_조회(final Long 기본_프로젝트_아이디) {
        return given()
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX + "/projects/{projectId}", 기본_프로젝트_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 프로젝트_아이디와_토큰으로_프로젝트_정보를_조회(final Long 기본_프로젝트_아이디,
                                                                           final String 로그인_토큰) {
        return given()
                .header(AUTHORIZATION, 로그인_토큰)
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX + "/projects/{projectId}", 기본_프로젝트_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 프로젝트_투두리스트_조회(final Long 기본_프로젝트_아이디, final String 로그인_토큰) {
        return given()
                .header(AUTHORIZATION, 로그인_토큰)
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX + "/projects/{projectId}/todos", 기본_프로젝트_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 사용자의_모든_프로젝트_조회(final String 로그인_토큰) {
        return given().log().all()
                .header(AUTHORIZATION, 로그인_토큰)
                .when()
                .get(API_PREFIX + "/projects/me")
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 사용자가_참여한_프로젝트_중_프로젝트_진행_상태에_따라_목록을_조회(final String 로그인_토큰,
                                                                                      final String 프로젝트_진행_상태) {
        return given().log().all()
                .header(AUTHORIZATION, 로그인_토큰)
                .queryParam("statusCond", 프로젝트_진행_상태)
                .when()
                .get(API_PREFIX + "/projects/me")
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 프로젝트_노드_조회(final Long 기본_프로젝트_아이디, final String 로그인_토큰) {
        return given()
                .header(AUTHORIZATION, 로그인_토큰)
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX + "/projects/{projectId}/nodes", 기본_프로젝트_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 프로젝트의_사용자_정보를_전체_조회(final Long 기본_프로젝트_아이디, final String 로그인_토큰,
                                                                    final String 정렬조건) {
        return given().log().all()
                .header(AUTHORIZATION, 로그인_토큰)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX + "/projects/{projectId}/members?sortCond={sortType}", 기본_프로젝트_아이디, 정렬조건)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 피드_아이디로_프로젝트_조회(final String 로그인_토큰, final Long 피드_아이디) {
        return given().log().all()
                .header(AUTHORIZATION, 로그인_토큰)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX
                                + "/feeds/{feedId}/projects", 피드_아이디)
                .then()
                .log().all()
                .extract();
    }
}
