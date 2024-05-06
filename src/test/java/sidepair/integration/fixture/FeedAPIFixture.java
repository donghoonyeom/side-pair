package sidepair.integration.fixture;

import static io.restassured.RestAssured.given;
import static sidepair.integration.fixture.CommonFixture.API_PREFIX;
import static sidepair.integration.fixture.CommonFixture.AUTHORIZATION;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import sidepair.domain.feed.FeedCategory;
import sidepair.persistence.dto.FeedOrderType;
import sidepair.service.dto.CustomScrollRequest;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.feed.requesst.FeedCategorySaveRequest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.response.FeedResponse;

public class FeedAPIFixture {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static Long 피드_생성(final FeedSaveRequest 피드_생성_요청, final String 액세스_토큰) throws IOException {
        final Response 응답 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청, 액세스_토큰).response();
        return Long.parseLong(응답.header(HttpHeaders.LOCATION).split("/")[3]);
    }

    public static Long 신청서를_생성하고_아이디를_반환한다(final FeedApplicantSaveRequest 피드_신청서_생성_요청, final String 액세스_토큰,
                                           final Long 피드_아이디) {
        final Response 응답 = 신청서를_생성한다(액세스_토큰, 피드_아이디, 피드_신청서_생성_요청).response();
        return Long.parseLong(응답.header(HttpHeaders.LOCATION).split("/")[3]);
    }

    public static ExtractableResponse<Response> 요청을_받는_이미지가_포함된_피드_생성(final FeedSaveRequest 피드_생성_요청값,
                                                                      final String accessToken)
            throws IOException {
        final String jsonRequest = objectMapper.writeValueAsString(피드_생성_요청값);

        RequestSpecification requestSpecification = given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE + "; charset=utf-8")
                .multiPart("jsonData", "jsonData.json", jsonRequest, MediaType.APPLICATION_JSON_VALUE);

        requestSpecification = makeRequestSpecification(피드_생성_요청값, requestSpecification);

        return requestSpecification
                .log().all()
                .post(API_PREFIX + "/feeds")
                .then().log().all()
                .extract();
    }

    private static RequestSpecification makeRequestSpecification(final FeedSaveRequest 피드_생성_요청값,
                                                                 RequestSpecification requestSpecification)
            throws IOException {
        if (피드_생성_요청값.feedNodes() == null) {
            return requestSpecification;
        }
        for (final FeedNodeSaveRequest feedNode : 피드_생성_요청값.feedNodes()) {
            final String 피드_노드_제목 = feedNode.getTitle() != null ? feedNode.getTitle() : "name";
            final MockMultipartFile 가짜_이미지_객체 = new MockMultipartFile(피드_노드_제목, "originalFileName.jpeg",
                    "image/jpeg", "tempImage".getBytes());
            requestSpecification = requestSpecification
                    .multiPart(가짜_이미지_객체.getName(), 가짜_이미지_객체.getOriginalFilename(),
                            가짜_이미지_객체.getBytes(), 가짜_이미지_객체.getContentType());
        }
        return requestSpecification;
    }

    public static ExtractableResponse<Response> 피드_삭제(final Long 삭제할_피드_아이디, final String 로그인_토큰) {
        return given().log().all()
                .header(HttpHeaders.AUTHORIZATION, 로그인_토큰)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .delete(API_PREFIX + "/feeds/{feedId}", 삭제할_피드_아이디)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 피드를_아이디로_조회한다(final Long 피드_아이디) {
        return given()
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_PREFIX + "/feeds/{feedId}", 피드_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static FeedResponse 피드를_아이디로_조회하고_응답객체를_반환한다(final Long 피드_아이디) {
        return 피드를_아이디로_조회한다(피드_아이디)
                .response()
                .as(FeedResponse.class);
    }

    public static ExtractableResponse<Response> 정렬된_카테고리별_피드_리스트_조회(final FeedOrderType 정렬_조건,
                                                                    final Long 검색할_카테고리_아이디, final int 페이지_크기) {
        return given()
                .log().all()
                .when()
                .get("/api/feeds?size=" + 페이지_크기 + "&filterCond=" + 정렬_조건.name() + "&categoryId=" + 검색할_카테고리_아이디)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 로그인한_사용자가_생성한_피드를_이전에_받은_피드의_제일마지막_아이디_이후의_조건으로_조회한다(
            final String 로그인_토큰_정보, final int 페이지_사이즈, final Long 마지막_피드_아이디) {
        return given()
                .log().all()
                .when()
                .header(HttpHeaders.AUTHORIZATION, 로그인_토큰_정보)
                .get("/api/feeds/me?lastId=" + 마지막_피드_아이디 + "&size=" + 페이지_사이즈)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 로그인한_사용자가_생성한_피드를_조회한다(final String 로그인_토큰_정보, final int 페이지_사이즈) {
        return given()
                .log().all()
                .when()
                .header(HttpHeaders.AUTHORIZATION, 로그인_토큰_정보)
                .get("/api/feeds/me?size=" + 페이지_사이즈)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 모든_카테고리를_조회한다() {
        return given()
                .log().all()
                .when()
                .get("/api/feeds/categories")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 사이즈별로_피드를_조회한다(final Integer size) {
        return given()
                .log().all()
                .when()
                .get("/api/feeds?size=" + size)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 사이즈_없이_피드를_조회한다() {
        return given()
                .log().all()
                .when()
                .get("/api/feeds")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 신청서를_생성한다(final String 팔로워_토큰_정보, final Long 피드_아이디,
                                                          final FeedApplicantSaveRequest 피드_신청서_생성_요청) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .header(AUTHORIZATION, 팔로워_토큰_정보)
                .body(피드_신청서_생성_요청)
                .post("/api/feeds/" + 피드_아이디 + "/applicant")
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 피드_신청서를_조회한다(final String 로그인_토큰_정보, final Long 피드_아이디,
                                                             final CustomScrollRequest 스크롤_요청) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .header(HttpHeaders.AUTHORIZATION, 로그인_토큰_정보)
                .param("lastId", 스크롤_요청.lastId())
                .param("size", 스크롤_요청.size())
                .get("/api/feeds/me/{feedId}/applicants", 피드_아이디)
                .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 정렬된_피드_리스트_조회(final FeedOrderType 정렬_조건, final int 페이지_크기) {
        return given()
                .log().all()
                .when()
                .get("/api/feeds?size=" + 페이지_크기 + "&filterCond=" + 정렬_조건.name())
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 태그_이름으로_최신순_정렬된_피드를_검색한다(final int 페이지_사이즈, final String 태그) {
        return given()
                .log().all()
                .when()
                .get("/api/feeds/search?size=" + 페이지_사이즈 + "&tagName=" + 태그)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 크리에이터_닉네임으로_정렬된_피드를_생성한다(final int 페이지_사이즈, final String 크리에이터_닉네임) {
        return given()
                .log().all()
                .when()
                .get("/api/feeds/search?size=" + 페이지_사이즈 + "&creatorName=" + 크리에이터_닉네임)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 제목으로_최신순_정렬된_피드를_검색한다(final int 페이지_사이즈, final String 피드_제목) {
        return given()
                .log().all()
                .when()
                .get("/api/feeds/search?size=" + 페이지_사이즈 + "&feedTitle=" + 피드_제목)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 피드_카테고리를_생성한다(final String 로그인_토큰_정보,
                                                              final FeedCategorySaveRequest 카테고리_생성_요청) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .header(AUTHORIZATION, 로그인_토큰_정보)
                .body(카테고리_생성_요청)
                .post("/api/feeds/categories")
                .then()
                .log().all()
                .extract();
    }

    public static FeedCategory 카테고리_생성(final String 로그인_토큰_정보, final String 카테고리_이름) {
        피드_카테고리를_생성한다(로그인_토큰_정보, new FeedCategorySaveRequest(카테고리_이름));
        return new FeedCategory(1L, 카테고리_이름);
    }

    public static List<FeedCategory> 카테고리들_생성(final String 로그인_토큰_정보, final String... 카테고리_이름들) {
        final List<FeedCategory> 카테고리들 = new ArrayList<>();
        for (final String 카테고리_이름 : 카테고리_이름들) {
            final FeedCategory 카테고리 = 카테고리_생성(로그인_토큰_정보, 카테고리_이름);
            카테고리들.add(카테고리);
        }
        return 카테고리들;
    }
}
