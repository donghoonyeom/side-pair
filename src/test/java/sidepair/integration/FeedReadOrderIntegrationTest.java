package sidepair.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static sidepair.integration.fixture.FeedAPIFixture.신청서를_생성한다;
import static sidepair.integration.fixture.FeedAPIFixture.정렬된_카테고리별_피드_리스트_조회;
import static sidepair.integration.fixture.FeedAPIFixture.정렬된_피드_리스트_조회;
import static sidepair.integration.fixture.FeedAPIFixture.카테고리_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드_생성;
import static sidepair.integration.fixture.FeedAPIFixture.피드를_아이디로_조회하고_응답객체를_반환한다;
import static sidepair.integration.fixture.MemberAPIFixture.사용자를_추가하고_토큰을_조회한다;
import static sidepair.integration.fixture.MemberAPIFixture.요청을_받는_사용자_자신의_정보_조회_요청;
import static sidepair.integration.fixture.ProjectAPIFixture.십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.오늘;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_이름;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_제한_인원;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_회고_횟수;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트를_생성하고_아이디를_반환한다;

import io.restassured.common.mapper.TypeRef;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.project.Project;
import sidepair.integration.helper.InitIntegrationTest;
import sidepair.persistence.dto.FeedOrderType;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedTagSaveRequest;
import sidepair.service.dto.feed.response.FeedForListResponses;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.mamber.response.MemberInformationResponse;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;

class FeedReadOrderIntegrationTest extends InitIntegrationTest {

    @Test
    void 특정_카테고리의_피드_목록을_최신순으로_조회한다() throws IOException {
        // given
        // 기본, 두 번째 피드 - 여행, 세 번째 피드 - 여가
        // 첫 번째 피드 생성
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        // 두 번째 피드 생성
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);

        // 다른 카테고리의 세 번째 피드 생성
        final FeedCategory 다른_카테고리 = 카테고리_생성(기본_로그인_토큰, "여가");
        final FeedSaveRequest 세번째_피드_생성_요청 = new FeedSaveRequest(2L, "third feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        피드_생성(세번째_피드_생성_요청, 기본_로그인_토큰);

        // when
        final FeedForListResponses 피드_리스트_응답 = 정렬된_카테고리별_피드_리스트_조회(FeedOrderType.LATEST, 기본_카테고리.getId(), 10)
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isFalse();
        assertThat(피드_리스트_응답.responses().get(0).feedId()).isEqualTo(두번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(1).feedId()).isEqualTo(기본_피드_아이디);
    }

    @Test
    void 특정_카테고리의_피드_목록을_신청서_작성순으로_조회한다() throws IOException {
        // given
        // 기본, 두 번째 피드 - 여행, 세 번째 피드 - 여가
        // 첫 번째 피드 생성
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        // 사용자 추가
        final String 팔로워_액세스_토큰1 = 사용자를_추가하고_토큰을_조회한다("test2@email.com", "닉네임2");
        프로젝트를_생성하고_참여자에_사용자를_추가한다(팔로워_액세스_토큰1, 피드_응답);

        // 두 번째 피드 생성
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 두번째_피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(두번째_피드_아이디);

        // 사용자 추가
        final String 팔로워_액세스_토큰2 = 사용자를_추가하고_토큰을_조회한다("test3@email.com", "닉네임3");
        프로젝트를_생성하고_참여자에_사용자를_추가한다(팔로워_액세스_토큰2, 두번째_피드_응답);

        // 두 번째 피드의 신청서 추가
        final FeedApplicantSaveRequest 피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");
        신청서를_생성한다(팔로워_액세스_토큰1, 두번째_피드_아이디, 피드_신청서_생성_요청);

        // 두 번째 피드의 신청서 추가
        final FeedApplicantSaveRequest 두번째_피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");
        신청서를_생성한다(팔로워_액세스_토큰2, 두번째_피드_아이디, 두번째_피드_신청서_생성_요청);

        // 다른 카테고리의 세 번째 피드 생성
        final FeedCategory 다른_카테고리 = 카테고리_생성(기본_로그인_토큰, "여가");
        final FeedSaveRequest 세번째_피드_생성_요청 = new FeedSaveRequest(다른_카테고리.getId(), "third feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        피드_생성(세번째_피드_생성_요청, 기본_로그인_토큰);

        // when
        final FeedForListResponses 피드_리스트_응답 = 정렬된_카테고리별_피드_리스트_조회(FeedOrderType.APPLICANT_COUNT, 기본_카테고리.getId(),
                10)
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isFalse();
        assertThat(피드_리스트_응답.responses().get(0).feedId()).isEqualTo(두번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(1).feedId()).isEqualTo(기본_피드_아이디);
    }

    @Test
    void 전체_피드_목록을_최신순으로_조회한다() throws IOException {
        // given
        // 첫 번째 피드 생성
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);

        // 두 번째 피드 생성
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);

        // 세 번째 피드 생성
        final FeedCategory 다른_카테고리 = 카테고리_생성(기본_로그인_토큰, "여가");
        final FeedSaveRequest 세번째_피드_생성_요청 = new FeedSaveRequest(다른_카테고리.getId(), "third feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 세번째_피드_아이디 = 피드_생성(세번째_피드_생성_요청, 기본_로그인_토큰);

        // when
        final FeedForListResponses 피드_리스트_응답 = 정렬된_피드_리스트_조회(FeedOrderType.LATEST, 10)
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
    void 전체_카테고리의_피드_목록을_신청서_작성순으로_조회한다() throws IOException {
        // given
        // 기본, 두 번째 피드 - 여행, 세 번째 피드 - 여가
        // 첫 번째 피드 생성
        final Long 기본_피드_아이디 = 피드_생성(기본_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(기본_피드_아이디);

        // 사용자 추가
        final String 팔로워_액세스_토큰1 = 사용자를_추가하고_토큰을_조회한다("test2@email.com", "닉네임2");
        프로젝트를_생성하고_참여자에_사용자를_추가한다(팔로워_액세스_토큰1, 피드_응답);

        // 두 번째 피드 생성
        final FeedSaveRequest 두번째_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "second feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 두번째_피드_아이디 = 피드_생성(두번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 두번째_피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(두번째_피드_아이디);

        // 사용자 추가
        final String 팔로워_액세스_토큰2 = 사용자를_추가하고_토큰을_조회한다("test3@email.com", "닉네임3");
        프로젝트를_생성하고_참여자에_사용자를_추가한다(팔로워_액세스_토큰2, 두번째_피드_응답);

        // 두 번째 피드의 신청서 추가
        final FeedApplicantSaveRequest 피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");
        신청서를_생성한다(팔로워_액세스_토큰1, 두번째_피드_아이디, 피드_신청서_생성_요청);

        // 두 번째 피드의 신청서 추가
        final FeedApplicantSaveRequest 두번째_피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");
        신청서를_생성한다(팔로워_액세스_토큰2, 두번째_피드_아이디, 두번째_피드_신청서_생성_요청);

        // 세 번째 피드 생성
        final FeedCategory 다른_카테고리 = 카테고리_생성(기본_로그인_토큰, "여가");
        final FeedSaveRequest 세번째_피드_생성_요청 = new FeedSaveRequest(다른_카테고리.getId(), "third feed", "다른 피드 소개글",
                "다른 피드 본문", 30,
                List.of(new FeedNodeSaveRequest("다른 피드 1주차", "다른 피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("다른 태그1")));
        final Long 세번째_피드_아이디 = 피드_생성(세번째_피드_생성_요청, 기본_로그인_토큰);
        final FeedResponse 세번째_피드_응답 = 피드를_아이디로_조회하고_응답객체를_반환한다(세번째_피드_아이디);

        // 사용자 추가
        final String 팔로워_액세스_토큰3 = 사용자를_추가하고_토큰을_조회한다("test4@email.com", "닉네임4");
        프로젝트를_생성하고_참여자에_사용자를_추가한다(팔로워_액세스_토큰3, 세번째_피드_응답);

        // 세 번째 피드의 신청서 추가
        final FeedApplicantSaveRequest 세번째_피드_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");
        신청서를_생성한다(팔로워_액세스_토큰3, 세번째_피드_아이디, 세번째_피드_신청서_생성_요청);

        // when
        final FeedForListResponses 피드_리스트_응답 = 정렬된_피드_리스트_조회(FeedOrderType.APPLICANT_COUNT, 10)
                .response()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(피드_리스트_응답.hasNext()).isFalse();
        assertThat(피드_리스트_응답.responses().get(0).feedId()).isEqualTo(두번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(1).feedId()).isEqualTo(세번째_피드_아이디);
        assertThat(피드_리스트_응답.responses().get(2).feedId()).isEqualTo(기본_피드_아이디);
    }


    private Long 피드에_대한_프로젝트를_생성한다(final FeedResponse 피드_응답) {
        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);
        return 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);
    }

    private void 프로젝트를_생성하고_참여자에_사용자를_추가한다(final String 액세스_토큰, final FeedResponse 피드_응답) {
        final MemberInformationResponse 팔로워_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(액세스_토큰).as(new TypeRef<>() {
        });

        final MemberInformationResponse 리더_정보 = 요청을_받는_사용자_자신의_정보_조회_요청(기본_로그인_토큰).as(new TypeRef<>() {
        });
        final Project 프로젝트 = testTransactionService.완료한_프로젝트를_생성한다(피드_응답);
        testTransactionService.프로젝트에_대한_참여자_리스트를_생성한다(리더_정보, 프로젝트, 팔로워_정보);
    }
}
