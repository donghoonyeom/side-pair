package sidepair.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.request.RequestPartDescriptor;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.restdocs.snippet.Attributes.Attribute;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import sidepair.controller.helper.ControllerTestHelper;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.feed.requesst.FeedCategorySaveRequest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedTagSaveRequest;
import sidepair.service.exception.AuthenticationException;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.ConflictException;
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.feed.FeedCreateService;
import sidepair.service.feed.FeedReadService;

@WebMvcTest(FeedController.class)
class FeedCreateApiTest extends ControllerTestHelper {

    @MockBean
    private FeedCreateService feedCreateService;

    @MockBean
    private FeedReadService feedReadService;

    @Test
    void 정상적으로_피드을_생성한다() throws Exception {
        // given
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30, List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willReturn(1L);

        // expect
        피드_생성_요청(request, status().isCreated());
    }

    @Test
    void 피드_생성시_존재하지_않은_회원이면_예외가_발생한다() throws Exception {
        // given
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new AuthenticationException("존재하지 않는 회원입니다."));

        // expect
        피드_생성_요청(request, status().isUnauthorized());
    }

    @Test
    void 피드_생성시_유효하지_않은_카테고리_아이디를_입력하면_예외가_발생한다() throws Exception {
        // given
        final Long categoryId = 10L;
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(categoryId, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new NotFoundException("존재하지 않는 카테고리입니다. categoryId = 10"));

        // expect
        피드_생성_요청(request, status().isNotFound());
    }

    @Test
    void 피드_생성시_카테고리_아이디를_입력하지_않으면_예외가_발생한다() throws Exception {
        // given
        final Long categoryId = null;
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(categoryId, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_제목의_길이가_40보다_크면_예외가_발생한다() throws Exception {
        // given
        final String title = "a".repeat(41);
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, title, "피드 소개글", "피드 본문",
                30, List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("피드 제목의 길이는 최소 1글자, 최대 40글자입니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_제목을_입력하지_않으면_예외가_발생한다() throws Exception {
        // given
        final String title = null;
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, title, "피드 소개글", "피드 본문",
                30, List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_소개글의_길이가_150보다_크면_예외가_발생한다() throws Exception {
        // given
        final String introduction = "a".repeat(151);
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", introduction, "피드 본문",
                30, List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("피드 소개글의 길이는 최소 1글자, 최대 150글자입니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_소개글을_입력하지_않으면_예외가_발생한다() throws Exception {
        // given
        final String introduction = null;
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", introduction, "피드 본문",
                30, List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_본문의_길이가_2000보다_크면_예외가_발생한다() throws Exception {
        // given
        final String content = "a".repeat(2001);
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", content,
                30, List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("피드 본문의 길이는 최대 2000글자입니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1001})
    void 피드_생성시_피드_추천_소요기간이_0보다_작거나_1000보다_크면_예외가_발생한다(final int requiredPeriod) throws Exception {
        // given
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                requiredPeriod, List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("피드 추천 소요 기간은 최소 0일, 최대 1000일입니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_추천_소요기간을_입력하지_않으면_예외가_발생한다() throws Exception {
        // given
        final Integer requiredPeriod = null;
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                requiredPeriod,
                List.of(new FeedNodeSaveRequest("피드 1주차", "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_노드의_제목의_길이가_40보다_크면_예외가_발생한다() throws Exception {
        // given
        final String nodeTitle = "a".repeat(41);
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest(nodeTitle, "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("피드 노드의 제목의 길이는 최소 1글자, 최대 40글자입니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_노드의_설명의_길이가_2000보다_크면_예외가_발생한다() throws Exception {
        // given
        final String nodeContent = "a".repeat(2001);
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest("피드 1주차", nodeContent, null)),
                List.of(new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("피드 노드의 설명의 길이는 최소 1글자, 최대 2000글자입니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_노드의_제목을_입력하지_않으면_예외가_발생한다() throws Exception {
        // given
        final String nodeTitle = null;
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest(nodeTitle, "프로젝트 1주차에는 erd를 만들거에요.", null)),
                List.of(new FeedTagSaveRequest("태그1")));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_피드_노드의_설명을_입력하지_않으면_예외가_발생한다() throws Exception {
        // given
        final String nodeContent = null;
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest("피드 1주차", nodeContent, null)),
                List.of(new FeedTagSaveRequest("태그1")));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 11})
    void 피드_생성시_태그_이름이_1미만_10초과면_예외가_발생한다(final int nameLength) throws Exception {
        // given
        final String tagName = "a".repeat(nameLength);
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 내용", null)),
                List.of(new FeedTagSaveRequest(tagName)));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("태그 이름은 최소 1자부터 최대 10자까지 가능합니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_태그_개수가_5개_초과면_예외가_발생한다() throws Exception {
        // given
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 내용", null)),
                List.of(new FeedTagSaveRequest("태그1"), new FeedTagSaveRequest("태그2"),
                        new FeedTagSaveRequest("태그3"), new FeedTagSaveRequest("태그4"),
                        new FeedTagSaveRequest("태그5"), new FeedTagSaveRequest("태그6")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("태그의 개수는 최대 5개까지 가능합니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 피드_생성시_중복된_태그_이름이_있으면_예외가_발생한다() throws Exception {
        // given
        final FeedSaveRequest request = 피드_생성_요청을_생성한다(1L, "피드 제목", "피드 소개글", "피드 본문",
                30,
                List.of(new FeedNodeSaveRequest("피드 1주차", "피드 내용", null)),
                List.of(new FeedTagSaveRequest("태그1"), new FeedTagSaveRequest("태그1")));

        given(feedCreateService.create(any(), any()))
                .willThrow(new BadRequestException("태그 이름은 중복될 수 없습니다."));

        // expect
        피드_생성_요청(request, status().isBadRequest());
    }

    @Test
    void 정상적으로_피드을_삭제한다() throws Exception {
        // given
        doNothing()
                .when(feedCreateService)
                .deleteFeed(anyString(), anyLong());

        // when
        mockMvc.perform(delete(API_PREFIX + "/feeds/{feedId}", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNoContent())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디").optional()))
                );
    }

    @Test
    void 피드_삭제시_존재하지_않는_피드인_경우_예외가_발생한다() throws Exception {
        // given
        doThrow(new NotFoundException("존재하지 않는 피드입니다. feedId = 1"))
                .when(feedCreateService)
                .deleteFeed(anyString(), anyLong());

        // when
        final String response = mockMvc.perform(delete(API_PREFIX + "/feeds/{feedId}", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpectAll(status().isNotFound(),
                        jsonPath("$.message")
                                .value("존재하지 않는 피드입니다. feedId = 1"))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디").optional()),
                        responseFields(
                                fieldWithPath("message").description("예외 메시지"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse("존재하지 않는 피드입니다. feedId = 1");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 피드_삭제시_자신이_생성한_피드이_아닌_경우_예외가_발생한다() throws Exception {
        // given
        doThrow(new ForbiddenException("해당 피드을 생성한 사용자가 아닙니다."))
                .when(feedCreateService)
                .deleteFeed(anyString(), anyLong());

        // when
        final String response = mockMvc.perform(delete(API_PREFIX + "/feeds/{feedId}", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpectAll(status().isForbidden(),
                        jsonPath("$.message")
                                .value("해당 피드을 생성한 사용자가 아닙니다."))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디").optional()),
                        responseFields(
                                fieldWithPath("message").description("예외 메시지"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse("해당 피드을 생성한 사용자가 아닙니다.");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 피드의_신청서를_생성한다() throws Exception {
        // given
        doNothing().when(feedCreateService)
                .createApplicant(any(), any(), any());

        final FeedApplicantSaveRequest request = new FeedApplicantSaveRequest("신청서 내용");
        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post(API_PREFIX + "/feeds/{feedId}/applicant", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpect(status().isCreated())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("content").description("피드 신청서 내용")
                                        .attributes(new Attribute(RESTRICT, "- 길이 : 0 ~ 1000"))
                                        .optional()),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디"))));
    }

    @Test
    void 피드_신청서_생성시_내용이_1000자가_넘으면_예외가_발생한다() throws Exception {
        // given
        doThrow(new BadRequestException("신청서는 최대 1000글자까지 입력할 수 있습니다."))
                .when(feedCreateService)
                .createApplicant(any(), any(), any());

        final String content = "a".repeat(1001);
        final FeedApplicantSaveRequest request = new FeedApplicantSaveRequest(content);
        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        final String response = mockMvc.perform(post(API_PREFIX + "/feeds/{feedId}/applicant", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value("신청서는 최대 1000글자까지 입력할 수 있습니다."))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("content").description("피드 신청서 내용")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse("신청서는 최대 1000글자까지 입력할 수 있습니다.");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 피드_신청서_생성시_존재하지_않은_피드이면_예외가_발생한다() throws Exception {
        // given
        doThrow(new NotFoundException("존재하지 않는 피드입니다. feedId = 1L"))
                .when(feedCreateService)
                .createApplicant(any(), any(), any());

        final FeedApplicantSaveRequest request = new FeedApplicantSaveRequest("신청서 내용");
        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        final String response = mockMvc.perform(post(API_PREFIX + "/feeds/{feedId}/applicant", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value("존재하지 않는 피드입니다. feedId = 1L"))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("content").description("피드 신청서 내용")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse("존재하지 않는 피드입니다. feedId = 1L");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 피드_신청서_생성시_피드_생성자가_신청서를_보내려고_하면_예외가_발생한다() throws Exception {
        // given
        doThrow(new BadRequestException("피드 생성자는 신청서를 보낼 수 없습니다. feedId = 1L memberId = 1L"))
                .when(feedCreateService)
                .createApplicant(any(), any(), any());

        final FeedApplicantSaveRequest request = new FeedApplicantSaveRequest("신청서 내용");
        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        final String response = mockMvc.perform(post(API_PREFIX + "/feeds/{feedId}/applicant", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message")
                                .value("피드 생성자는 신청서를 보낼 수 없습니다. feedId = 1L memberId = 1L"))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("content").description("피드 신청서 내용")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse(
                "피드 생성자는 신청서를 보낼 수 없습니다. feedId = 1L memberId = 1L");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 피드_신청서_생성시_이미_신청서를_단적이_있으면_예외가_발생한다() throws Exception {
        // given
        doThrow(new BadRequestException("이미 작성한 신청서가 존재합니다. feedId = 1L memberId = 1L"))
                .when(feedCreateService)
                .createApplicant(any(), any(), any());

        final FeedApplicantSaveRequest request = new FeedApplicantSaveRequest("신청서 내용");
        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        final String response = mockMvc.perform(post(API_PREFIX + "/feeds/{feedId}/applicant", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message")
                                .value("이미 작성한 신청서가 존재합니다. feedId = 1L memberId = 1L"))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("content").description("피드 신청서 내용")),
                        pathParameters(
                                parameterWithName("feedId").description("피드 아이디"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse(
                "이미 작성한 신청서가 존재합니다. feedId = 1L memberId = 1L");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 정상적으로_피드_카테고리를_생성한다() throws Exception {
        // given
        final FeedCategorySaveRequest request = new FeedCategorySaveRequest("카테고리 이름");
        doNothing().when(feedCreateService)
                .createFeedCategory(request);

        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post(API_PREFIX + "/feeds/categories")
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpect(status().isCreated())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("name").description("카테고리 이름")
                                        .attributes(new Attribute(RESTRICT, "- 길이 : 1 ~ 10")))));
    }

    @Test
    void 피드_카테고리_생성_시_카테고리_이름이_빈값일_경우() throws Exception {
        // given
        final FeedCategorySaveRequest request = new FeedCategorySaveRequest("");
        doNothing().when(feedCreateService)
                .createFeedCategory(request);

        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post(API_PREFIX + "/feeds/categories")
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("name").description("카테고리 이름")
                                        .attributes(new Attribute(RESTRICT, "- 길이 : 1 ~ 10")))));
    }

    @Test
    void 피드_카테고리_생성_시_카테고리_이름이_10자_초과일_경우() throws Exception {
        // given
        final FeedCategorySaveRequest request = new FeedCategorySaveRequest("10자가 초과되는 카테고리 이름입니다.");
        doThrow(new BadRequestException("카테고리 이름은 1자 이상 10자 이하입니다.")).when(feedCreateService)
                .createFeedCategory(request);

        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post(API_PREFIX + "/feeds/categories")
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("name").description("카테고리 이름")
                                        .attributes(new Attribute(RESTRICT, "- 길이 : 1 ~ 10"))),
                        responseFields(
                                fieldWithPath("message").description("예외 메시지"))));
    }

    @Test
    void 피드_카테고리_생성_시_카테고리_이름이_중복될_경우() throws Exception {
        // given
        final FeedCategorySaveRequest request = new FeedCategorySaveRequest("여행");
        doThrow(new ConflictException("이미 존재하는 이름의 카테고리입니다.")).when(feedCreateService)
                .createFeedCategory(request);

        final String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post(API_PREFIX + "/feeds/categories")
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .contextPath(API_PREFIX))
                .andExpect(status().isConflict())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("name").description("카테고리 이름")
                                        .attributes(new Attribute(RESTRICT, "- 길이 : 1 ~ 10"))),
                        responseFields(
                                fieldWithPath("message").description("예외 메시지"))));
    }

    private FeedSaveRequest 피드_생성_요청을_생성한다(final Long categoryId, final String feedTitle,
                                           final String feedIntroduction, final String feedContent,
                                           final Integer requiredPeriod,
                                           final List<FeedNodeSaveRequest> feedNodesSaveRequests,
                                           final List<FeedTagSaveRequest> feedTagSaveRequests) {
        return new FeedSaveRequest(categoryId, feedTitle, feedIntroduction, feedContent,
                requiredPeriod, feedNodesSaveRequests, feedTagSaveRequests);
    }

    private void 피드_생성_요청(final FeedSaveRequest request, final ResultMatcher httpStatus) throws Exception {
        final String jsonRequest = objectMapper.writeValueAsString(request);
        final MockMultipartFile jsonDataFile = new MockMultipartFile("jsonData", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        final List<RequestPartDescriptor> MULTIPART_FORM_데이터_설명_리스트 = new ArrayList<>();
        MULTIPART_FORM_데이터_설명_리스트.add(partWithName("jsonData").description("피드 생성 요청 json 데이터"));

        MockMultipartHttpServletRequestBuilder httpServletRequestBuilder = multipart(API_PREFIX + "/feeds");

        for (final FeedNodeSaveRequest feedNode : request.feedNodes()) {
            final String 피드_노드_제목 = feedNode.getTitle() != null ? feedNode.getTitle() : "name";
            final MockMultipartFile 가짜_이미지_객체 = new MockMultipartFile(피드_노드_제목,
                    "originalFileName.jpeg", "image/jpeg", "tempImage".getBytes());
            httpServletRequestBuilder = httpServletRequestBuilder.file(가짜_이미지_객체);

            MULTIPART_FORM_데이터_설명_리스트.add(
                    partWithName(피드_노드_제목).description("피드 노드 title")
            );
        }

        mockMvc.perform(httpServletRequestBuilder
                        .file(jsonDataFile)
                        .param("jsonData", jsonRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contextPath(API_PREFIX))
                .andExpect(httpStatus)
                .andDo(documentationResultHandler.document(
                        requestHeaders(headerWithName("Authorization").description("access token")),
                        requestParts(MULTIPART_FORM_데이터_설명_리스트),
                        requestPartFields("jsonData",
                                fieldWithPath("categoryId").description("피드 카테고리 아이디"),
                                fieldWithPath("title").description("피드 제목")
                                        .attributes(new Attributes.Attribute(RESTRICT, "- 길이 : 1-40")),
                                fieldWithPath("introduction").description("피드 소개글")
                                        .attributes(new Attributes.Attribute(RESTRICT, "- 길이 : 1-150")),
                                fieldWithPath("content").description("피드 본문 내용").optional()
                                        .attributes(new Attributes.Attribute(RESTRICT, "- 길이 : 0-2000")),
                                fieldWithPath("requiredPeriod").description("피드 전체 추천 소요 기간")
                                        .attributes(new Attributes.Attribute(RESTRICT, "- 길이 : 0-1000")),
                                fieldWithPath("feedNodes").description("피드 노드들"),
                                fieldWithPath("feedNodes[0].title").description("피드 노드의 제목")
                                        .attributes(new Attributes.Attribute(RESTRICT, "- 길이 : 1-40")),
                                fieldWithPath("feedNodes[0].content").description("피드 노드의 설명")
                                        .attributes(new Attributes.Attribute(RESTRICT, "- 길이 : 1-2000")),
                                fieldWithPath("feedNodes[0].images").ignored(),
                                fieldWithPath("feedTags[0].name").description("피드 태그 이름")
                                        .attributes(new Attributes.Attribute(RESTRICT, "- 길이 : 1-10"))
                                        .optional()
                        ),
                        responseHeaders(
                                headerWithName("Location").description("피드 아이디").optional()
                        )));
    }
}
