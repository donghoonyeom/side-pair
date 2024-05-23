package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static sidepair.integration.fixture.FeedAPIFixture.제목으로_최신순_정렬된_피드를_검색한다;
import static sidepair.integration.fixture.FeedAPIFixture.크리에이터_닉네임으로_정렬된_피드를_생성한다;
import static sidepair.integration.fixture.FeedAPIFixture.태그_이름으로_최신순_정렬된_피드를_검색한다;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.MemberAPIFixture.요청을_받는_사용자_자신의_정보_조회_요청;

import io.restassured.common.mapper.TypeRef;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedTagSaveRequest;
import sidepair.service.dto.feed.response.FeedForListResponses;
import sidepair.service.dto.mamber.response.MemberInformationResponse;

class FeedSearchIntegrationTest extends InitIntegrationTest {

    @Test
    void 피드를_제목을_기준으로_검색한다() throws IOException {
        // given
        피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 세번쨰_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "third feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 세번째_피드_아이디 = 피드_생성(세번쨰_피드_생성_요청, 기본_로그인_토큰);

        // when
        final FeedForListResponses 피드_리스트_응답 = 제목으로_최신순_정렬된_피드를_검색한다(10, "feed")
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isFalse();
        assertThat(피드_리스트_응답.responses().get(0).feedId()).isEqualTo(세번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(1).feedId()).isEqualTo(두번째_피드_아이디);
    }

    @Test
    void 피드를_크리에이터_닉네임_기준으로_검색한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final MemberInformationResponse 사용자_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(기본_로그인_토큰).as(new TypeRef<>() {
        });
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);

        // when
        final FeedForListResponses 피드_리스트_응답 = 크리에이터_닉네임으로_정렬된_피드를_생성한다(10, 사용자_정보.nickname())
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isFalse();
        assertThat(피드_리스트_응답.responses().get(0).feedId()).isEqualTo(두번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(1).feedId()).isEqualTo(기본_피드_아이디);
    }

    @Test
    void 피드를_태그_이름을_기준으로_검색한다() throws IOException {
        // given
        final String 태그_이름 = "tag name";

        피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest(태그_이름)));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 세번쨰_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "세번쨰 피드", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest(태그_이름)));
        final Long 세번째_피드_아이디 = 피드_생성(세번쨰_피드_생성_요청, 기본_로그인_토큰);

        // when
        final FeedForListResponses 피드_리스트_응답 = 태그_이름으로_최신순_정렬된_피드를_검색한다(10, 태그_이름)
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isFalse();
        assertThat(피드_리스트_응답.responses().get(0).feedId()).isEqualTo(세번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(1).feedId()).isEqualTo(두번째_피드_아이디);
    }
}
