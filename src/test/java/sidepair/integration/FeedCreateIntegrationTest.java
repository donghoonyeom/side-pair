package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static sidepair.integration.fixture.AuthenticationAPIFixture.로그인;
import static sidepair.integration.fixture.CommonFixture.BEARER_TOKEN_FORMAT;
import static sidepair.integration.fixture.CommonFixture.아이디를_반환한다;
import static sidepair.integration.fixture.CommonFixture.응답_상태_코드_검증;
import static sidepair.integration.fixture.FeedAPIFixture.요청을_받는_이미지가_포함된_피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드_삭제;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드_카테고리를_생성한다;
import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_SKILLS;
import static sidepair.integration.fixture.MemberAPIFixture.회원가입;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.auth.request.LoginRequest;
import sidepair.service.dto.feed.requesst.FeedCategorySaveRequest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedTagSaveRequest;
import sidepair.service.dto.mamber.request.MemberJoinRequest;
import sidepair.service.dto.mamber.request.PositionType;

class FeedCreateIntegrationTest extends InitIntegrationTest {

    @Test
    void 정상적으로_피드를_생성한다() throws IOException {
        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        // expect
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.CREATED);
        final Long 피드_아이디 = 아이디를_반환한다(피드_생성_응답값);
        assertThat(피드_아이디).isEqualTo(1L);
    }

    @Test
    void 본문의_값이_없는_피드가_정상적으로_생성한다() throws IOException {
        // given
        final String 피드_본문 = null;
        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글", 피드_본문, 30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 1주차 내용", Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.CREATED);
        final Long 피드_아이디 = 아이디를_반환한다(피드_생성_응답값);
        assertThat(피드_아이디).isEqualTo(1L);
    }

    @Test
    void 피드_생성시_잘못된_빈값을_넘기면_실패한다() throws IOException {
        // given
        final Long 카테고리_아이디 = null;
        final String 피드_제목 = null;
        final String 피드_소개글 = null;
        final Integer 추천_소요_기간 = null;
        final String 피드_노드_제목 = null;
        final String 피드_노드_설명 = null;

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(카테고리_아이디, 피드_제목, 피드_소개글,
                "피드 본문", 추천_소요_기간,
                List.of(new FeedNodeSaveRequest(피드_노드_제목, 피드_노드_설명, Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final List<ErrorResponse> 에러_메시지들 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메시지들)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        new ErrorResponse("예상 소요 기간을 입력해주세요."),
                        new ErrorResponse("피드 노드의 설명을 입력해주세요."),
                        new ErrorResponse("피드 노드의 제목을 입력해주세요."),
                        new ErrorResponse("프로젝트의 소개글을 입력해주세요."),
                        new ErrorResponse("카테고리를 입력해주세요."),
                        new ErrorResponse("피드의 제목을 입력해주세요.")));
    }

    @Test
    void 존재하지_않는_카테고리_아이디를_입력한_경우_실패한다() throws IOException {
        // given
        final long 카테고리_아이디 = 2L;

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(카테고리_아이디, "피드 제목", "피드 소개글", "피드 본문", 30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 1주차 내용", Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.NOT_FOUND);
        assertThat(에러_메세지.message()).isEqualTo("존재하지 않는 카테고리입니다. categoryId = 2");

    }

    @Test
    void 제목의_길이가_40보다_크면_실패한다() throws IOException {
        // given
        final String 피드_제목 = "a".repeat(41);

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), 피드_제목, "피드 소개글", "피드 본문", 30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 1주차 내용", Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("피드 제목의 길이는 최소 1글자, 최대 40글자입니다.");
    }

    @Test
    void 소개글의_길이가_150보다_크면_실패한다() throws IOException {
        // given
        final String 피드_소개글 = "a".repeat(151);

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", 피드_소개글, "피드 본문", 30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 1주차 내용", Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("피드 소개글의 길이는 최소 1글자, 최대 150글자입니다.");
    }

    @Test
    void 본문의_길이가_2000보다_크면_실패한다() throws IOException {
        // given
        final String 피드_본문 = "a".repeat(2001);

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글", 피드_본문, 30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 1주차 내용", Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("피드 본문의 길이는 최대 2000글자입니다.");
    }

    @Test
    void 추천_소요_기간이_0보다_작으면_실패한다() throws IOException {
        // given
        final Integer 추천_소요_기간 = -1;

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글",
                "피드 본문", 추천_소요_기간,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 1주차 내용", Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("피드 추천 소요 기간은 최소 0일, 최대 1000일입니다.");
    }

    @Test
    void 피드_노드를_입력하지_않으면_실패한다() throws IOException {
        // given
        final List<FeedNodeSaveRequest> 피드_노드들 = null;

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글",
                "피드 본문", 30, 피드_노드들, List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final List<ErrorResponse> 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.get(0).message()).isEqualTo("프로젝트의 첫 번째 단계를 입력해주세요.");
    }

    @Test
    void 피드_노드의_제목의_길이가_40보다_크면_실패한다() throws IOException {
        // given
        final String 피드_노드_제목 = "a".repeat(41);
        final List<FeedNodeSaveRequest> 피드_노드들 = List.of(
                new FeedNodeSaveRequest(피드_노드_제목, "피드 1주차 내용", Collections.emptyList()));

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글",
                "피드 본문", 30, 피드_노드들, List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("피드 노드의 제목의 길이는 최소 1글자, 최대 40글자입니다.");
    }

    @Test
    void 피드_노드의_설명의_길이가_2000보다_크면_실패한다() throws IOException {
        // given
        final String 피드_노드_설명 = "a".repeat(2001);
        final List<FeedNodeSaveRequest> 피드_노드들 = List.of(
                new FeedNodeSaveRequest("피드 노드 제목", 피드_노드_설명, Collections.emptyList()));

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글",
                "피드 본문", 30, 피드_노드들, List.of(new FeedTagSaveRequest("태그1")));

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("피드 노드의 설명의 길이는 최소 1글자, 최대 2000글자입니다.");
    }

    @Test
    void 피드_태그_이름이_중복되면_예외가_발생한다() throws IOException {
        // given
        final List<FeedTagSaveRequest> 태그_저장_요청 = List.of(new FeedTagSaveRequest("태그"),
                new FeedTagSaveRequest("태그"));

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글", "피드 본문", 30,
                List.of(new FeedNodeSaveRequest("피드 노드 제목", "피드 노드 설명", Collections.emptyList())), 태그_저장_요청);

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("태그 이름은 중복될 수 없습니다.");
    }

    @Test
    void 피드_태그_개수가_5개_초과면_예외가_발생한다() throws IOException {
        // given
        final List<FeedTagSaveRequest> 태그_저장_요청 = List.of(new FeedTagSaveRequest("태그1"),
                new FeedTagSaveRequest("태그2"), new FeedTagSaveRequest("태그3"),
                new FeedTagSaveRequest("태그4"), new FeedTagSaveRequest("태그5"),
                new FeedTagSaveRequest("태그6"));

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글", "피드 본문", 30,
                List.of(new FeedNodeSaveRequest("피드 노드 제목", "피드 노드 설명", Collections.emptyList())), 태그_저장_요청);

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("태그의 개수는 최대 5개까지 가능합니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 11})
    void 피드_태그_이름의_길이가_1자_미만_10자_초과면_예외가_발생한다(final int nameLength) throws IOException {
        // given
        final String 태그_이름 = "a".repeat(nameLength);
        final List<FeedTagSaveRequest> 태그_저장_요청 = List.of(new FeedTagSaveRequest(태그_이름));

        final FeedSaveRequest 피드_생성_요청값 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글", "피드 본문", 30,
                List.of(new FeedNodeSaveRequest("피드 노드 제목", "피드 노드 설명", Collections.emptyList())), 태그_저장_요청);

        // when
        final ExtractableResponse<Response> 피드_생성_응답값 = 요청을_받는_이미지가_포함된_피드_생성(피드_생성_요청값, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_생성_응답값.as(new TypeRef<>() {
        });
        응답_상태_코드_검증(피드_생성_응답값, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("태그 이름은 최소 1자부터 최대 10자까지 가능합니다.");
    }

    @Test
    void 프로젝트가_생성된_적이_없는_피드을_정상적으로_삭제한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        // when
        final ExtractableResponse<Response> 피드_삭제_응답 = 피드_삭제(피드_아이디, 기본_로그인_토큰);

        // then
        응답_상태_코드_검증(피드_삭제_응답, HttpStatus.NO_CONTENT);
    }

    @Test
    void 피드를_삭제할_때_존재하지_않는_피드이면_예외가_발생한다() throws IOException {
        // given
        final Long 존재하지_않는_피드_아이디 = 1L;

        // when
        final ExtractableResponse<Response> 피드_삭제_응답 = 피드_삭제(존재하지_않는_피드_아이디, 기본_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_삭제_응답.as(ErrorResponse.class);
        응답_상태_코드_검증(피드_삭제_응답, HttpStatus.NOT_FOUND);
        assertThat(에러_메세지.message()).isEqualTo("존재하지 않는 피드입니다. feedId = 1");

    }

    @Test
    void 피드를_삭제할_때_자신이_생성한_피드이_아니면_예외가_발생한다() throws IOException {
        // given
        final Long 피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        회원가입(new MemberJoinRequest("test2@email.com", "password2!", "name2",
                PositionType.BACKEND, DEFAULT_SKILLS));
        final String 다른_사용자_로그인_토큰 = String.format(BEARER_TOKEN_FORMAT,
                로그인(new LoginRequest("test2@email.com", "password2!")).accessToken());

        // when
        final ExtractableResponse<Response> 피드_삭제_응답 = 피드_삭제(피드_아이디, 다른_사용자_로그인_토큰);

        // then
        final ErrorResponse 에러_메세지 = 피드_삭제_응답.as(ErrorResponse.class);
        응답_상태_코드_검증(피드_삭제_응답, HttpStatus.FORBIDDEN);
        assertThat(에러_메세지.message()).isEqualTo("해당 피드를 생성한 사용자가 아닙니다.");

    }

    @Test
    void 정상적으로_카테고리를_생성한다() {
        //given
        final FeedCategorySaveRequest 피드_카테고리_생성_요청 = new FeedCategorySaveRequest("운동");

        //when
        final ExtractableResponse<Response> 피드_카테고리_생성_응답 = 피드_카테고리를_생성한다(기본_로그인_토큰, 피드_카테고리_생성_요청);

        //then
        응답_상태_코드_검증(피드_카테고리_생성_응답, HttpStatus.CREATED);
    }

    @Test
    void 카테고리_생성_시_10글자_초과_이름이_들어올_경우() {
        //given
        final FeedCategorySaveRequest 피드_카테고리_생성_요청 = new FeedCategorySaveRequest("10자 초과되는 카테고리 이름");

        //when
        final ExtractableResponse<Response> 피드_카테고리_생성_응답 = 피드_카테고리를_생성한다(기본_로그인_토큰, 피드_카테고리_생성_요청);

        //then
        final ErrorResponse 에러_메세지 = 피드_카테고리_생성_응답.as(ErrorResponse.class);

        응답_상태_코드_검증(피드_카테고리_생성_응답, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지.message()).isEqualTo("카테고리 이름은 1자 이상 10자 이하입니다.");
    }

    @Test
    void 카테고리_생성_시_공백이_들어올_경우() {
        //given
        final FeedCategorySaveRequest 피드_카테고리_생성_요청 = new FeedCategorySaveRequest("");

        //when
        final ExtractableResponse<Response> 피드_카테고리_생성_응답 = 피드_카테고리를_생성한다(기본_로그인_토큰, 피드_카테고리_생성_요청);

        //then
        final ErrorResponse[] 에러_메세지 = 피드_카테고리_생성_응답.as(ErrorResponse[].class);

        응답_상태_코드_검증(피드_카테고리_생성_응답, HttpStatus.BAD_REQUEST);
        assertThat(에러_메세지[0].message()).isEqualTo("카테고리 이름은 빈 값일 수 없습니다.");
    }

    @Test
    void 카테고리_생성_시_이미_있는_이름인_경우() {
        //given
        final FeedCategorySaveRequest 피드_카테고리_생성_요청 = new FeedCategorySaveRequest("헬스케어");
        피드_카테고리를_생성한다(기본_로그인_토큰, 피드_카테고리_생성_요청);

        //when
        final ExtractableResponse<Response> 피드_카테고리_생성_응답 = 피드_카테고리를_생성한다(기본_로그인_토큰, 피드_카테고리_생성_요청);

        //then
        final ErrorResponse 에러_메세지 = 피드_카테고리_생성_응답.as(ErrorResponse.class);

        응답_상태_코드_검증(피드_카테고리_생성_응답, HttpStatus.CONFLICT);
        assertThat(에러_메세지.message()).isEqualTo("이미 존재하는 이름의 카테고리입니다.");
    }
}
