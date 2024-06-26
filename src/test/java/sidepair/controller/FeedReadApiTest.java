package sidepair.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.web.servlet.MvcResult;
import sidepair.controller.helper.ControllerTestHelper;
import sidepair.domain.member.Position;
import sidepair.domain.project.ProjectStatus;
import sidepair.service.dto.CustomScrollRequest;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.feed.requesst.FeedOrderTypeRequest;
import sidepair.service.dto.feed.response.FeedApplicantResponse;
import sidepair.service.dto.feed.response.FeedCategoryResponse;
import sidepair.service.dto.feed.response.FeedContentResponse;
import sidepair.service.dto.feed.response.FeedForListResponse;
import sidepair.service.dto.feed.response.FeedForListResponses;
import sidepair.service.dto.feed.response.FeedNodeResponse;
import sidepair.service.dto.feed.response.FeedProjectResponse;
import sidepair.service.dto.feed.response.FeedProjectResponses;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.feed.response.FeedTagResponse;
import sidepair.service.dto.feed.response.MemberFeedResponse;
import sidepair.service.dto.feed.response.MemberFeedResponses;
import sidepair.service.dto.mamber.response.MemberResponse;
import sidepair.service.dto.mamber.response.MemberSkillResponse;
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.feed.FeedCreateService;
import sidepair.service.feed.FeedReadService;

@WebMvcTest(FeedController.class)
class FeedReadApiTest extends ControllerTestHelper {
    private final LocalDateTime 오늘 = LocalDateTime.now();

    @MockBean
    private FeedReadService feedReadService;

    @MockBean
    private FeedCreateService feedCreateService;

    @Test
    void 단일_피드_정보를_조회한다() throws Exception {
        //given
        final FeedResponse expectedResponse = 단일_피드_조회에_대한_응답();
        when(feedReadService.findFeed(anyLong()))
                .thenReturn(expectedResponse);

        //when
        final MvcResult response = mockMvc.perform(get(API_PREFIX + "/feeds/{feedId}", 1L)
                        .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디")
                        ),
                        responseFields(
                                fieldWithPath("feedId").description("피드 아이디"),
                                fieldWithPath("category.id").description("피드 카테고리 아이디"),
                                fieldWithPath("category.name").description("피드 카테고리 이름"),
                                fieldWithPath("feedTitle").description("피드 제목"),
                                fieldWithPath("introduction").description("피드 소개글"),
                                fieldWithPath("recommendedFeedPeriod").description("프로젝트 예상 기간"),
                                fieldWithPath("createdAt").description("피드 생성 시간"),
                                fieldWithPath("creator.id").description("피드 크리에이터 아이디"),
                                fieldWithPath("creator.name").description("피드 크리에이터 닉네임"),
                                fieldWithPath("creator.imageUrl").description("피드 크리에이터 프로필 이미지 경로"),
                                fieldWithPath("creator.position").description("피드 크리에이터 포지션"),
                                fieldWithPath("creator.skills[0].id").description("피드 크리에이터 기술 아이디"),
                                fieldWithPath("creator.skills[0].name").description("피드 크리에이터 기술 이름"),
                                fieldWithPath("content.id").description("피드 컨텐츠 아이디"),
                                fieldWithPath("content.content").description("피드 컨텐츠 본문"),
                                fieldWithPath("content.nodes[0].id").description("피드 노드 아이디"),
                                fieldWithPath("content.nodes[0].title").description("피드 노드 제목"),
                                fieldWithPath("content.nodes[0].description").description("피드 노드 본문"),
                                fieldWithPath("content.nodes[0].imageUrls[0]").description("피드 노드 이미지 파일 경로"),
                                fieldWithPath("tags[0].id").description("피드 태그 아이디"),
                                fieldWithPath("tags[0].name").description("피드 태그 이름"))))
                .andReturn();

        //then
        final FeedResponse feedResponse = jsonToClass(response, new TypeReference<>() {
        });

        assertThat(feedResponse)
                .isEqualTo(expectedResponse);
    }

    @Test
    void 존재하지_않는_피드_아이디로_요청_시_예외를_반환한다() throws Exception {
        // given
        when(feedReadService.findFeed(anyLong())).thenThrow(
                new NotFoundException("존재하지 않는 피드입니다. feedId = 1"));

        // when
        // then
        mockMvc.perform(get(API_PREFIX + "/feeds/{feedId}", 1L)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 피드입니다. feedId = 1"))
                .andDo(documentationResultHandler.document(
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디")),
                        responseFields(
                                fieldWithPath("message").description("예외 메세지")
                        )));
    }

    @Test
    void 피드_목록을_조건에_따라_조회한다() throws Exception {
        // given
        final FeedForListResponses expected = 피드_리스트_응답을_생성한다();
        when(feedReadService.findFeedsByOrderType(any(), any(), any()))
                .thenReturn(expected);

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds")
                                .param("categoryId", "1")
                                .param("filterCond", FeedOrderTypeRequest.LATEST.name())
                                .param("lastId", "1")
                                .param("size", "10")
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                queryParameters(
                                        parameterWithName("categoryId").description("카테고리 아이디(미전송 시 전체 조회)")
                                                .optional(),
                                        parameterWithName("filterCond").description(
                                                        "필터 조건(LATEST, PARTICIPANT_COUNT)")
                                                .optional(),
                                        parameterWithName("lastId")
                                                .description("이전 요청에서 받은 응답 중 가장 마지막 피드 아이디 (첫 요청 시 미전송)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에서 받아올 피드의 수")),
                                responseFields(
                                        fieldWithPath("responses[0].feedId").description("피드 아이디"),
                                        fieldWithPath("responses[0].feedTitle").description("피드 제목"),
                                        fieldWithPath("responses[0].introduction").description("피드 소개글"),
                                        fieldWithPath("responses[0].recommendedFeedPeriod").description("프로젝트 예상 기간"),
                                        fieldWithPath("responses[0].createdAt").description("피드 생성 시간"),
                                        fieldWithPath("responses[0].creator.id").description("피드 크리에이터 아이디"),
                                        fieldWithPath("responses[0].creator.name").description("피드 크리에이터 이름"),
                                        fieldWithPath("responses[0].creator.imageUrl").description(
                                                "피드 크리에이터 프로필 이미지 경로"),
                                        fieldWithPath("responses[0].creator.position").description("피드 포지션 이름"),
                                        fieldWithPath("responses[0].creator.skills[0].id").description("피드 크리에이터 기술 아이디"),
                                        fieldWithPath("responses[0].creator.skills[0].name").description("피드 크리에이터 기술 이름"),
                                        fieldWithPath("responses[0].category.id").description("피드 카테고리 아이디"),
                                        fieldWithPath("responses[0].category.name").description("피드 카테고리 이름"),
                                        fieldWithPath("responses[0].tags[0].id").description("피드 태그 아이디"),
                                        fieldWithPath("responses[0].tags[0].name").description("피드 태그 이름"),
                                        fieldWithPath("hasNext").description("다음 요소의 존재 여부")
                                )))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final FeedForListResponses feedForListResponses = objectMapper.readValue(response,
                new TypeReference<>() {
                });

        assertThat(feedForListResponses)
                .isEqualTo(expected);
    }

    @Test
    void 피드_목록_조회시_유효하지_않은_카테고리_아이디를_보내면_예외가_발생한다() throws Exception {
        // given
        when(feedReadService.findFeedsByOrderType(any(), any(), any())).thenThrow(
                new NotFoundException("존재하지 않는 카테고리입니다. categoryId = 1L"));

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds")
                                .param("categoryId", "1")
                                .param("filterCond", FeedOrderTypeRequest.LATEST.name())
                                .param("size", "10")
                                .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value("존재하지 않는 카테고리입니다. categoryId = 1L"))
                .andDo(documentationResultHandler.document(
                        queryParameters(
                                parameterWithName("categoryId").description("잘못된 카테고리 아이디"),
                                parameterWithName("filterCond").description(
                                                "필터 조건(LATEST, PARTICIPANT_COUNT)")
                                        .optional(),
                                parameterWithName("size").description("한 페이지에서 받아올 피드의 수")),
                        responseFields(fieldWithPath("message").description("예외 메시지"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse("존재하지 않는 카테고리입니다. categoryId = 1L");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 피드_목록_조회시_사이즈_값을_전송하지_않으면_예외가_발생한다() throws Exception {
        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds")
                                .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$[0].message").value("사이즈를 입력해 주세요."))
                .andDo(documentationResultHandler.document(
                        responseFields(fieldWithPath("[0].message").description("예외 메시지"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final List<ErrorResponse> errorResponse = objectMapper.readValue(response, new TypeReference<>() {
        });
        final ErrorResponse expected = new ErrorResponse("사이즈를 입력해 주세요.");
        assertThat(errorResponse.get(0))
                .isEqualTo(expected);
    }

    @Test
    void 피드의_프로젝트를_조회한다() throws Exception {
        // given
        final FeedProjectResponses 프로젝트_페이지_응답 = 프로젝트_응답들을_생성한다();
        given(feedReadService.findFeedProjects(any()))
                .willReturn(프로젝트_페이지_응답);

        // when
        final String 응답값 = mockMvc.perform(
                        get(API_PREFIX + "/feeds/{feedId}/projects", 1L)
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("feedId").description("피드 아이디")),
                                responseFields(
                                        fieldWithPath("responses[0].projectId").description("프로젝트 아이디"),
                                        fieldWithPath("responses[0].name").description("프로젝트 이름"),
                                        fieldWithPath("responses[0].status").description("프로젝트 상태"),
                                        fieldWithPath("responses[0].currentMemberCount").description("현재 프로젝트에 참여한 인원 수"),
                                        fieldWithPath("responses[0].limitedMemberCount").description(
                                                "프로젝트에 참여할 수 있는 제한 인원 수"),
                                        fieldWithPath("responses[0].createdAt").description("프로젝트 생성 날짜와 시간"),
                                        fieldWithPath("responses[0].startDate").description("프로젝트의 시작 날짜"),
                                        fieldWithPath("responses[0].endDate").description("프로젝트의 종료 날짜"),
                                        fieldWithPath("responses[0].projectLeader.id").description("프로젝트 리더의 아이디"),
                                        fieldWithPath("responses[0].projectLeader.name").description("프로젝트 리더의 닉네임"),
                                        fieldWithPath("responses[0].projectLeader.imageUrl").description(
                                                "프로젝트 리더의 프로필 이미지 경로"),
                                        fieldWithPath("responses[0].projectLeader.position").description(
                                                "프로젝트 리더 포지션"),
                                        fieldWithPath("responses[0].projectLeader.skills[].id").description(
                                                "프로젝트 리더 기술 ID"),
                                        fieldWithPath("responses[0].projectLeader.skills[].name").description(
                                                "프로젝트 리더 기술 이름")
                                )
                        )
                )
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final FeedProjectResponses 응답값으로_생성한_프로젝트_페이지 = objectMapper.readValue(응답값,
                new TypeReference<>() {
                });

        final FeedProjectResponses 예상되는_프로젝트_페이지_응답 = 프로젝트_응답들을_생성한다();
        assertThat(응답값으로_생성한_프로젝트_페이지)
                .usingRecursiveComparison()
                .isEqualTo(예상되는_프로젝트_페이지_응답);
    }

    @Test
    void 피드의_프로젝트룰_조회할_때_피드가_존재하지_않으면_예외_발생() throws Exception {
        // given
        given(feedReadService.findFeedProjects(any()))
                .willThrow(new NotFoundException("존재하지 않는 피드입니다. feedId = 1"));

        // when
        final MvcResult 응답값 = mockMvc.perform(
                        get(API_PREFIX + "/feeds/{feedId}/projects", 1L)
                                .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value("존재하지 않는 피드입니다. feedId = 1"))
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("feedId").description("피드 아이디")),
                                responseFields(fieldWithPath("message").description("예외 메시지")))
                )
                .andReturn();
        // then
        final ErrorResponse errorResponse = jsonToClass(응답값, new TypeReference<>() {
        });
        final ErrorResponse expected = new ErrorResponse("존재하지 않는 피드입니다. feedId = 1");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 피드_카테고리_목록을_조회한다() throws Exception {
        // given
        final List<FeedCategoryResponse> expected = 피드_카테고리_응답_리스트를_반환한다();
        when(feedReadService.findAllFeedCategories())
                .thenReturn(expected);

        // when
        final String response = mockMvc.perform(
                        get("/api/feeds/categories")
                                .contextPath(API_PREFIX))
                .andDo(
                        documentationResultHandler.document(
                                responseFields(
                                        fieldWithPath("[0].id").description("카테고리 아이디"),
                                        fieldWithPath("[0].name").description("카테고리 이름")
                                )
                        )
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        final List<FeedCategoryResponse> feedCategoryResponses = objectMapper.readValue(response,
                new TypeReference<>() {
                });

        assertThat(feedCategoryResponses)
                .isEqualTo(expected);
    }

    @Test
    void 피드를_조건별로_검색한다() throws Exception {
        // given
        final FeedForListResponses expected = 피드_리스트_응답을_생성한다();
        when(feedReadService.search(any(), any(), any()))
                .thenReturn(expected);

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds/search")
                                .param("feedTitle", "feed")
                                .param("lastId", "1")
                                .param("creatorName", "사이드페어")
                                .param("tagName", "Java")
                                .param("filterCond", FeedOrderTypeRequest.LATEST.name())
                                .param("size", "10")
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                queryParameters(
                                        parameterWithName("feedTitle").description("피드 제목 검색어")
                                                .attributes(new Attributes.Attribute(RESTRICT, "- 길이: 1자 이상"))
                                                .optional(),
                                        parameterWithName("creatorName").description("크리에이터 닉네임")
                                                .optional(),
                                        parameterWithName("tagName").description("피드 태그 이름")
                                                .attributes(new Attributes.Attribute(RESTRICT, "- 길이: 1자 이상"))
                                                .optional(),
                                        parameterWithName("filterCond").description(
                                                        "필터 조건(LATEST, PARTICIPANT_COUNT)")
                                                .optional(),
                                        parameterWithName("lastId")
                                                .description("이전 요청에서 받은 응답 중 가장 마지막 피드 아이디 (첫 요청 시 미전송)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에서 받아올 피드의 수")),
                                responseFields(
                                        fieldWithPath("responses[0].feedId").description("피드 아이디"),
                                        fieldWithPath("responses[0].feedTitle").description("피드 제목"),
                                        fieldWithPath("responses[0].introduction").description("피드 소개글"),
                                        fieldWithPath("responses[0].recommendedFeedPeriod").description("프로젝트 예상 기간"),
                                        fieldWithPath("responses[0].createdAt").description("피드 생성 시간"),
                                        fieldWithPath("responses[0].creator.id").description("피드 크리에이터 아이디"),
                                        fieldWithPath("responses[0].creator.name").description("피드 크리에이터 이름"),
                                        fieldWithPath("responses[0].creator.imageUrl").description(
                                                "피드 크리에이터 프로필 이미지 경로"),
                                        fieldWithPath("responses[0].creator.position").description("피드 포지션 이름"),
                                        fieldWithPath("responses[0].creator.skills[0].id").description("피드 크리에이터 기술 아이디"),
                                        fieldWithPath("responses[0].creator.skills[0].name").description("피드 크리에이터 기술 이름"),
                                        fieldWithPath("responses[0].category.id").description("피드 카테고리 아이디"),
                                        fieldWithPath("responses[0].category.name").description("피드 카테고리 이름"),
                                        fieldWithPath("responses[0].tags[0].id").description("피드 태그 아이디"),
                                        fieldWithPath("responses[0].tags[0].name").description("피드 태그 이름"),
                                        fieldWithPath("hasNext").description("다음 요소의 존재 여부")
                                )))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final FeedForListResponses feedForListResponses = objectMapper.readValue(response,
                new TypeReference<>() {
                });

        assertThat(feedForListResponses)
                .isEqualTo(expected);
    }

    @Test
    void 피드_검색시_사이즈_값을_전송하지_않으면_예외가_발생한다() throws Exception {
        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds/search")
                                .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$[0].message").value("사이즈를 입력해 주세요."))
                .andDo(documentationResultHandler.document(
                        responseFields(fieldWithPath("[0].message").description("예외 메시지"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final List<ErrorResponse> errorResponse = objectMapper.readValue(response, new TypeReference<>() {
        });
        final ErrorResponse expected = new ErrorResponse("사이즈를 입력해 주세요.");
        assertThat(errorResponse.get(0))
                .isEqualTo(expected);
    }

    @Test
    void 사용자가_생성한_피드를_조회한다() throws Exception {
        // given
        final MemberFeedResponses expected = 사용자_피드_조회에_대한_응답을_생성한다();

        when(feedReadService.findAllMemberFeeds(any(), any()))
                .thenReturn(expected);

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer <accessToken>")
                                .param("lastId", "1")
                                .param("size", "10")
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")),
                                queryParameters(
                                        parameterWithName("lastId")
                                                .description("이전 요청에서 받은 응답 중 가장 마지막 피드 아이디 (첫 요청 시 미전송)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에서 받아올 피드의 수")),
                                responseFields(
                                        fieldWithPath("responses[0].feedId").description("피드 아이디"),
                                        fieldWithPath("responses[0].feedTitle").description("피드 제목"),
                                        fieldWithPath("responses[0].createdAt").description("피드 생성날짜"),
                                        fieldWithPath("responses[0].category.id").description("피드 카테고리 아이디"),
                                        fieldWithPath("responses[0].category.name").description("피드 카테고리 이름"),
                                        fieldWithPath("hasNext").description("다음 요소의 존재 여부")
                                )))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final MemberFeedResponses memberFeedRespons = objectMapper.readValue(response,
                new TypeReference<>() {
                });
        assertThat(memberFeedRespons)
                .isEqualTo(expected);
    }

    @Test
    void 사용자가_생성한_피드를_조회할_때_존재하지_않는_회원이면_예외가_발생한다() throws Exception {
        // given
        when(feedReadService.findAllMemberFeeds(any(), any()))
                .thenThrow(new NotFoundException("존재하지 않는 회원입니다."));

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer <accessToken>")
                                .param("lastId", "1")
                                .param("size", "10")
                                .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value("존재하지 않는 회원입니다."))
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")),
                                queryParameters(
                                        parameterWithName("lastId")
                                                .description("이전 요청에서 받은 응답 중 가장 마지막 피드 아이디 (첫 요청 시 미전송)")
                                                .optional(),
                                        parameterWithName("size").description("한 페이지에서 받아올 피드의 수")),
                                responseFields(fieldWithPath("message").description("예외 메시지"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse("존재하지 않는 회원입니다.");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 피드의_신청서들을_조회한다() throws Exception {
        // given
        final List<FeedApplicantResponse> expected = List.of(
                new FeedApplicantResponse(1L, new MemberResponse(1L, "신청자1", "image1-file-path", Position.BACKEND.name(),
                        List.of(new MemberSkillResponse(1L, "Java"))),
                        LocalDateTime.of(2023, 8, 15, 12, 30, 0, 123456), "신청서 내용"),
                new FeedApplicantResponse(2L, new MemberResponse(2L, "신청자2", "image2-file-path", Position.FRONTEND.name(),
                        List.of(new MemberSkillResponse(1L, "HTML"))),
                        LocalDateTime.of(2023, 8, 16, 12, 30, 0, 123456), "신청서 내용")
        );

        when(feedReadService.findFeedApplicants(anyLong(), any(), any()))
                .thenReturn(expected);

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds/me/{feedId}/applicants", 1L)
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                                .param("lastId", "1")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디")
                        ),
                        queryParameters(
                                parameterWithName("lastId")
                                        .description("이전 요청에서 받은 응답 중 가장 마지막 신청서 아이디 (첫 요청 시 미전송)")
                                        .optional(),
                                parameterWithName("size").description("한 번에 조회할 신청서갯수")
                        ),
                        responseFields(
                                fieldWithPath("[0].id").description("신청서 아이디"),
                                fieldWithPath("[0].member.id").description("작성자 아이디"),
                                fieldWithPath("[0].member.name").description("작성자 닉네임"),
                                fieldWithPath("[0].member.position").description("사용자 포지션"),
                                fieldWithPath("[0].member.skills[].id").description("기술 ID"),
                                fieldWithPath("[0].member.skills[].name").description("기술 이름"),
                                fieldWithPath("[0].member.imageUrl").description("작성자 프로필 이미지 경로"),
                                fieldWithPath("[0].createdAt").description("신청서 최종 작성날짜"),
                                fieldWithPath("[0].content").description("신청서 내용")
                        )))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final List<FeedApplicantResponse> applicantResponse = objectMapper.readValue(response,
                new TypeReference<>() {
                });

        assertThat(applicantResponse)
                .usingRecursiveComparison()
                .ignoringFields("createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 피드_신청서_조회_시_유효하지_않은_피드_아이디일_경우_예외를_반환한다() throws Exception {
        // given
        when(feedReadService.findFeedApplicants(anyLong(), anyString(), any()))
                .thenThrow(new NotFoundException("존재하지 않는 피드입니다. feedId = 1"));

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds/me/{feedId}/applicants", 1L)
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디")
                        ),
                        queryParameters(
                                parameterWithName("lastId")
                                        .description("이전 요청에서 받은 응답 중 가장 마지막 신청서 아이디 (첫 요청 시 미전송)")
                                        .optional(),
                                parameterWithName("size").description("한 번에 조회할 신청서갯수")
                        ),
                        responseFields(
                                fieldWithPath("message").description("예외 메시지")
                        )))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(errorResponse.message())
                .isEqualTo("존재하지 않는 피드입니다. feedId = 1");
    }

    @Test
    void 피드_신청서_조회_시_피드_생성자와_일치하지_않은_셍성자일_경우_예외를_반환한다() throws Exception {
        // given
        when(feedReadService.findFeedApplicants(anyLong(), anyString(), any()))
                .thenThrow(new ForbiddenException("해당 피드를 생성한 사용자가 아닙니다."));

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/feeds/me/{feedId}/applicants", 1L)
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .contextPath(API_PREFIX))
                .andExpectAll(status().isForbidden())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디")
                        ),
                        queryParameters(
                                parameterWithName("lastId")
                                        .description("이전 요청에서 받은 응답 중 가장 마지막 신청서 아이디 (첫 요청 시 미전송)")
                                        .optional(),
                                parameterWithName("size").description("한 번에 조회할 신청서갯수")
                        ),
                        responseFields(
                                fieldWithPath("message").description("예외 메시지")
                        )))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(errorResponse.message())
                .isEqualTo("해당 피드를 생성한 사용자가 아닙니다.");
    }

    private FeedResponse 단일_피드_조회에_대한_응답() {
        final FeedCategoryResponse category = new FeedCategoryResponse(1, "운동");
        final MemberResponse creator = new MemberResponse(1, "닉네임", "profile-image-filepath",
                Position.BACKEND.name(), List.of(new MemberSkillResponse(1L, "Java")));
        final List<FeedNodeResponse> nodes = List.of(
                new FeedNodeResponse(1L, "1번 노드", "1번 노드 설명", List.of("image1-filepath", "image2-filepath")),
                new FeedNodeResponse(2L, "2번 노드", "2번 노드 설명", Collections.emptyList())
        );
        final List<FeedTagResponse> tags = List.of(
                new FeedTagResponse(1L, "태그1"),
                new FeedTagResponse(2L, "태그2")
        );
        return new FeedResponse(1L, category, "제목", "소개글", creator,
                new FeedContentResponse(1L, "본문", nodes), 100, 오늘, tags);
    }

    private FeedForListResponses 피드_리스트_응답을_생성한다() {
        final List<FeedTagResponse> tags = List.of(
                new FeedTagResponse(1L, "태그1"),
                new FeedTagResponse(2L, "태그2")
        );

        final FeedForListResponse feedResponse1 = new FeedForListResponse(1L, "피드 제목1", "피드 소개글1",
                10, 오늘, new MemberResponse(1L, "사이드페어", "default-member-image",
                Position.BACKEND.name(), List.of(new MemberSkillResponse(1L, "Java"))),
                new FeedCategoryResponse(1L, "여행"), tags);
        final FeedForListResponse feedResponse2 = new FeedForListResponse(2L, "피드 제목2", "피드 소개글2",
                7, 오늘, new MemberResponse(2L, "페어사이드", "default-member-image",
                Position.FRONTEND.name(), List.of(new MemberSkillResponse(1L, "HTML"))),
                new FeedCategoryResponse(2L, "IT"), tags);
        final List<FeedForListResponse> responses = List.of(feedResponse1, feedResponse2);
        return new FeedForListResponses(responses, false);
    }

    private List<FeedCategoryResponse> 피드_카테고리_응답_리스트를_반환한다() {
        final FeedCategoryResponse category1 = new FeedCategoryResponse(1L, "이커머스");
        final FeedCategoryResponse category2 = new FeedCategoryResponse(2L, "IT");
        final FeedCategoryResponse category3 = new FeedCategoryResponse(3L, "커뮤니티");
        final FeedCategoryResponse category4 = new FeedCategoryResponse(4L, "헬스케어");
        final FeedCategoryResponse category5 = new FeedCategoryResponse(5L, "플햇폼");
        final FeedCategoryResponse category6 = new FeedCategoryResponse(6L, "뮤직");
        final FeedCategoryResponse category7 = new FeedCategoryResponse(7L, "게임");
        final FeedCategoryResponse category8 = new FeedCategoryResponse(8L, "기타");
        return List.of(category1, category2, category3, category4, category5, category6, category7, category8);
    }

    private MemberFeedResponses 사용자_피드_조회에_대한_응답을_생성한다() {
        final List<MemberFeedResponse> responses = List.of(
                new MemberFeedResponse(3L, "세 번째 피드", LocalDateTime.now(),
                        new FeedCategoryResponse(2L, "게임")),
                new MemberFeedResponse(2L, "두 번째 피드", LocalDateTime.now(),
                        new FeedCategoryResponse(1L, "헬스케어")),
                new MemberFeedResponse(1L, "첫 번째 피드", LocalDateTime.now(),
                        new FeedCategoryResponse(1L, "커뮤니티")));
        return new MemberFeedResponses(responses, true);
    }

    private FeedProjectResponses 프로젝트_응답들을_생성한다() {
        final FeedProjectResponse feedProjectResponse1 = new FeedProjectResponse(1L, "프로젝트 이름1",
                ProjectStatus.RECRUITING, 3, 6,
                LocalDateTime.of(2023, 7, 20, 13, 0, 0),
                LocalDate.now(), LocalDate.now().plusDays(100),
                new MemberResponse(1L, "사이드", "default-member-image", Position.BACKEND.name(),
                        List.of(new MemberSkillResponse(1L, "Java"))));
        final FeedProjectResponse feedProjectResponse2 = new FeedProjectResponse(2L, "프로젝트 이름2",
                ProjectStatus.RECRUITING, 4, 10,
                LocalDateTime.of(2023, 7, 10, 13, 0, 0),
                LocalDate.now(), LocalDate.now().plusDays(100),
                new MemberResponse(2L, "페어", "default-member-image", Position.BACKEND.name(),
                        List.of(new MemberSkillResponse(1L, "Java"))));
        final List<FeedProjectResponse> responses = List.of(feedProjectResponse1,
                feedProjectResponse2);
        return new FeedProjectResponses(responses);
    }
}
