package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static sidepair.integration.fixture.FeedAPIFixture.로그인한_사용자가_생성한_피드를_이전에_받은_피드의_제일마지막_아이디_이후의_조건으로_조회한다;
import static sidepair.integration.fixture.FeedAPIFixture.로그인한_사용자가_생성한_피드를_조회한다;
import static sidepair.integration.fixture.FeedAPIFixture.모든_카테고리를_조회한다;
import static sidepair.integration.fixture.FeedAPIFixture.사이즈_없이_피드를_조회한다;
import static sidepair.integration.fixture.FeedAPIFixture.사이즈별로_피드를_조회한다;
import static sidepair.integration.fixture.FeedAPIFixture.정렬된_카테고리별_피드_리스트_조회;
import static sidepair.integration.fixture.FeedAPIFixture.카테고리_생성;
import static sidepair.integration.fixture.FeedAPIFixture.카테고리들_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드를_아이디로_조회한다;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import sidepair.domain.feed.FeedCategory;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.persistence.dto.FeedOrderType;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedTagSaveRequest;
import sidepair.service.dto.feed.response.FeedCategoryResponse;
import sidepair.service.dto.feed.response.FeedForListResponse;
import sidepair.service.dto.feed.response.FeedForListResponses;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.feed.response.MemberFeedResponses;

class FeedReadIntegrationTest extends InitIntegrationTest {

    @Test
    void 존재하는_피드_아이디로_요청했을_때_단일_피드_정보_조회를_성공한다() throws IOException {
        //given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 다른_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "다른 피드 제목", "다른 피드 소개글",
                "다른 피드 본문",  30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        피드_생성(다른_피드_생성_요청, 기본_로그인_토큰);

        //when
        final ExtractableResponse<Response> 단일_피드_조회_요청에_대한_응답 = 피드를_아이디로_조회한다(기본_피드_아이디);

        //then
        final FeedResponse 단일_피드_응답 = 단일_피드_조회_요청에_대한_응답.as(new TypeRef<>() {
        });

        assertAll(
                () -> assertThat(단일_피드_조회_요청에_대한_응답.statusCode())
                        .isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(단일_피드_응답.feedId())
                        .isEqualTo(기본_피드_아이디)
        );
    }

    @Test
    void 존재하지_않는_피드_아이디로_요청했을_때_조회를_실패한다() {
        //given
        final Long 존재하지_않는_피드_아이디 = 1L;

        //when
        final ExtractableResponse<Response> 요청에_대한_응답 = 피드를_아이디로_조회한다(존재하지_않는_피드_아이디);

        //then
        final String 예외_메시지 = 요청에_대한_응답.asString();

        assertAll(
                () -> assertThat(요청에_대한_응답.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value()),
                () -> assertThat(예외_메시지).contains("존재하지 않는 피드입니다. feedId = " + 존재하지_않는_피드_아이디)
        );
    }

    @Test
    void 사이즈_조건으로_피드_목록을_조회한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문",  30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedCategory 다른_카테고리 = 카테고리_생성(기본_로그인_토큰, "여가");
        final FeedSaveRequest 세번째_피드_생성_요청 = new FeedSaveRequest(다른_카테고리.getId(), "thrid feed", "다른 피드 소개글",
                "다른 피드 본문",  30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 세번째_피드_아이디 = 피드_생성(세번째_피드_생성_요청, 기본_로그인_토큰);

        // when
        final FeedForListResponses 피드_리스트_응답 = 사이즈별로_피드를_조회한다(10)
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isFalse();
        assertThat(피드_리스트_응답.responses().get(0).feedId()).isEqualTo(세번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(1).feedId()).isEqualTo(두번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(2).feedId()).isEqualTo(기본_피드_아이디);
    }

    @Test
    void 피드_태그가_여러개일_경우_피드를_조회한다() throws IOException {
        // given
        for (int i = 0; i < 10; i++) {
            final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "feed" + i,
                    "다른 피드 소개글", "다른 피드 본문",  30,
                    List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                    List.of(new FeedTagSaveRequest("다른 태그1"),
                            new FeedTagSaveRequest("다른 태그2"),
                            new FeedTagSaveRequest("다른 태그3")));
            피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        }

        // when
        final FeedForListResponses 피드_리스트_응답 = 사이즈별로_피드를_조회한다(10)
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isFalse();
        assertThat(피드_리스트_응답.responses().size()).isEqualTo(10);
        for (final FeedForListResponse response : 피드_리스트_응답.responses()) {
            assertThat(response.tags().size()).isEqualTo(3);
        }
    }

    @Test
    void 피드_조회시_사이즈_조건을_주지_않으면_예외가_발생한다() {
        // when
        final List<ErrorResponse> 예외_메시지 = 사이즈_없이_피드를_조회한다()
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(예외_메시지.get(0))
                .isEqualTo(new ErrorResponse("사이즈를 입력해 주세요."));
    }

    @Test
    void 피드_카테고리_리스트를_조회한다() {
        // given
        final List<FeedCategory> 피드_카테고리_리스트 = 카테고리들_생성(기본_로그인_토큰, "IT", "헬스케어", "금융", "커뮤니티",
                "게임");

        // when
        final List<FeedCategoryResponse> 피드_카테고리_응답_리스트 = 모든_카테고리를_조회한다()
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_카테고리_응답_리스트.get(0).name()).isEqualTo("이커머스");
        for (int index = 1; index < 피드_카테고리_응답_리스트.size(); index++) {
            assertThat(피드_카테고리_응답_리스트.get(index).name()).isEqualTo(피드_카테고리_리스트.get(index - 1).getName());
        }
    }

    @Test
    void 사용자가_생성한_피드를_조회한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문",  30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 세번쨰_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "세번쨰 피드", "다른 피드 소개글",
                "다른 피드 본문",  30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 세번째_피드_아이디 = 피드_생성(세번쨰_피드_생성_요청, 기본_로그인_토큰);

        // when
        final MemberFeedResponses 사용자_피드_응답_리스트 = 로그인한_사용자가_생성한_피드를_조회한다(기본_로그인_토큰, 10)
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(사용자_피드_응답_리스트.hasNext()).isFalse();
        assertThat(사용자_피드_응답_리스트.responses().get(0).feedId()).isEqualTo(세번째_피드_아이디);
        assertThat(사용자_피드_응답_리스트.responses().get(1).feedId()).isEqualTo(두번째_피드_아이디);
        assertThat(사용자_피드_응답_리스트.responses().get(2).feedId()).isEqualTo(기본_피드_아이디);
    }

    @Test
    void 피드_목록_조회시_다음_요소가_존재하면_hasNext가_true로_반환된다() throws IOException {
        // given
        피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문",  30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);

        // when
        final FeedForListResponses 피드_리스트_응답 = 정렬된_카테고리별_피드_리스트_조회(FeedOrderType.LATEST, 기본_카테고리.getId(), 1)
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isTrue();
        assertThat(피드_리스트_응답.responses().get(0).feedId()).isEqualTo(두번째_피드_아이디);
    }

    @Test
    void 사용자가_생성한_피드를_이전에_받아온_리스트_이후로_조회한다() throws IOException {
        // given
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문",  30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedSaveRequest 세번쨰_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "세번쨰 피드", "다른 피드 소개글",
                "다른 피드 본문",  30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 세번째_피드_아이디 = 피드_생성(세번쨰_피드_생성_요청, 기본_로그인_토큰);

        // when
        final MemberFeedResponses 사용자_피드_응답_리스트 = 로그인한_사용자가_생성한_피드를_이전에_받은_피드의_제일마지막_아이디_이후의_조건으로_조회한다(
                기본_로그인_토큰, 10, 두번째_피드_아이디
        )
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(사용자_피드_응답_리스트.hasNext()).isFalse();
        assertThat(사용자_피드_응답_리스트.responses().get(0).feedId()).isEqualTo(기본_피드_아이디);
    }
}
