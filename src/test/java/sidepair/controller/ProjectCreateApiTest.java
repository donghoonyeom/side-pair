package sidepair.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import sidepair.controller.helper.ControllerTestHelper;
import sidepair.controller.helper.FieldDescriptionHelper.FieldDescription;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.project.request.MemoirRequest;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;
import sidepair.service.dto.project.request.ProjectTodoRequest;
import sidepair.service.dto.project.response.ProjectToDoCheckResponse;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.project.ProjectCreateService;
import sidepair.service.project.ProjectReadService;

@WebMvcTest(ProjectController.class)
class ProjectCreateApiTest extends ControllerTestHelper {
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);

    @MockBean
    private ProjectCreateService projectCreateService;

    @MockBean
    private ProjectReadService projectReadService;

    @Test
    void 정상적으로_프로젝트를_생성한다() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));

        given(projectCreateService.create(any(), any()))
                .willReturn(1L);
        final String jsonRequest = objectMapper.writeValueAsString(request);

        //when
        final List<FieldDescription> requestFieldDescription = makeCreateProjectSuccessRequestFieldDescription();

        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isCreated())
                .andDo(documentationResultHandler.document(
                        requestFields(makeFieldDescriptor(requestFieldDescription)),
                        requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")),
                        responseHeaders(headerWithName(HttpHeaders.LOCATION).description("프로젝트 단일 조회 api 경로"))
                ))
                .andReturn();

        //then
        assertThat(mvcResult.getResponse().getHeader("Location")).isEqualTo("/api/projects/" + 1);
    }

    @Test
    void 프로젝트_생성_시_요청에_빈값이_있을_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(null, null,
                null, new ArrayList<>(List.of(new ProjectFeedNodeRequest(null, null, null, null))));
        final String jsonRequest = objectMapper.writeValueAsString(request);

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isBadRequest())
                .andReturn();

        //then
        final ErrorResponse feedCheckCountIdErrorResponse = new ErrorResponse("인증 횟수는 빈 값일 수 없습니다.");
        final ErrorResponse feedNodeIdErrorResponse = new ErrorResponse("피드 노드 아이디는 빈 값일 수 없습니다.");
        final ErrorResponse limitedMemberCountErrorResponse = new ErrorResponse("프로젝트 제한 인원은 빈 값일 수 없습니다.");
        final ErrorResponse projectNameErrorResponse = new ErrorResponse("프로젝트 이름을 빈 값일 수 없습니다.");
        final ErrorResponse feedContentIdErrorResponse = new ErrorResponse("피드 컨텐츠 아이디는 빈 값일 수 없습니다.");
        final ErrorResponse projectNodeStartDateErrorResponse = new ErrorResponse("피드 노드 시작 날짜는 빈 값일 수 없습니다.");
        final ErrorResponse projectNodeEndDateErrorResponse = new ErrorResponse("피드 노드 종료 날짜는 빈 값일 수 없습니다.");
        final List<ErrorResponse> responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(feedCheckCountIdErrorResponse, feedNodeIdErrorResponse,
                        limitedMemberCountErrorResponse, projectNameErrorResponse, feedContentIdErrorResponse,
                        projectNodeStartDateErrorResponse, projectNodeEndDateErrorResponse
                ));
    }

    @Test
    void 프로젝트_생성_시_피드가_존재하지_않을_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new NotFoundException("존재하지 않는 피드입니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isNotFound())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("존재하지 않는 피드입니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 프로젝트_생성_시_삭제된_피드_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new BadRequestException("삭제된 피드에 대해 프로젝트를 생성할 수 없습니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isBadRequest())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("삭제된 피드에 대해 프로젝트를 생성할 수 없습니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 프로젝트_생성_시_피드의_노드_크기와_요청의_노드_크기가_일치하지_않을_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new BadRequestException("모든 노드에 대해 기간이 설정돼야 합니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isBadRequest())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("모든 노드에 대해 기간이 설정돼야 합니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 프로젝트_생성_시_피드에_존재하지_않는_노드일_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new NotFoundException("피드에 존재하지 않는 노드입니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isNotFound())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("피드에 존재하지 않는 노드입니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 프로젝트_생성_시_존재하지_않는_회원일_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new NotFoundException("존재하지 않는 회원입니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isNotFound())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("존재하지 않는 회원입니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 프로젝트_생성_시_프로젝트_노드의_시작_날짜보다_종료_날짜가_빠른_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TEN_DAY_LATER, TODAY))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new BadRequestException("시작일은 종료일보다 후일 수 없습니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isBadRequest())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("시작일은 종료일보다 후일 수 없습니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 프로젝트_생성_시_프로젝트_노드의_시작_날짜가_오늘보다_전일_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20,
                new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY.minusDays(10), TEN_DAY_LATER))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new BadRequestException("시작일은 오늘보다 전일 수 없습니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isBadRequest())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("시작일은 오늘보다 전일 수 없습니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 프로젝트_생성_시_프로젝트_노드의_인증_횟수가_0보다_작을_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 0, TODAY, TEN_DAY_LATER))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new BadRequestException("프로젝트 노드의 인증 횟수는 0보다 커야합니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isBadRequest())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("프로젝트 노드의 인증 횟수는 0보다 커야합니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 프로젝트_생성_시_프로젝트_노드의_인증_횟수가_기간보다_클_경우() throws Exception {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                20, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 11, TODAY, TEN_DAY_LATER))));
        final String jsonRequest = objectMapper.writeValueAsString(request);
        doThrow(new BadRequestException("프로젝트 노드의 인증 횟수가 설정 기간보다 클 수 없습니다."))
                .when(projectCreateService)
                .create(any(), any());

        //when
        final MvcResult mvcResult = 프로젝트_생성(jsonRequest, status().isBadRequest())
                .andReturn();

        //then
        final ErrorResponse expectedResponse = new ErrorResponse("프로젝트 노드의 인증 횟수가 설정 기간보다 클 수 없습니다.");
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void 정상적으로_프로젝트에_투두리스트를_추가한다() throws Exception {
        //given
        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("content", TODAY, TEN_DAY_LATER);
        final String jsonRequest = objectMapper.writeValueAsString(projectTodoRequest);
        given(projectCreateService.addProjectTodo(anyLong(), anyString(), any()))
                .willReturn(1L);

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos", 1L)
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestFields(makeFieldDescriptor(makeAddTodoSuccessRequestFieldDescription())),
                        requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")),
                        responseHeaders(headerWithName(HttpHeaders.LOCATION).description("프로젝트 투두 단일 조회 api 경로")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디"))))
                .andReturn();

        //then
        assertThat(mvcResult.getResponse().getHeader(HttpHeaders.LOCATION)).isEqualTo(
                API_PREFIX + "/projects/1/todos/1");
    }

    @Test
    void 프로젝트_투두_추가시_존재하지_않는_회원일_경우() throws Exception {
        //given
        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("content", TODAY, TEN_DAY_LATER);
        final String jsonRequest = objectMapper.writeValueAsString(projectTodoRequest);
        doThrow(new NotFoundException("존재하지 않는 회원입니다."))
                .when(projectCreateService)
                .addProjectTodo(anyLong(), anyString(), any());

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos", 1)
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestFields(makeFieldDescriptor(makeAddTodoSuccessRequestFieldDescription())),
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("존재하지 않는 회원입니다."));
    }

    @Test
    void 프로젝트_투두_추가시_존재하지_않는_프로젝트일_경우() throws Exception {
        //given
        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("content", TODAY, TEN_DAY_LATER);
        final String jsonRequest = objectMapper.writeValueAsString(projectTodoRequest);
        doThrow(new NotFoundException("존재하지 않는 프로젝트입니다. projectId = 1"))
                .when(projectCreateService)
                .addProjectTodo(anyLong(), anyString(), any());

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos", 1)
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestFields(makeFieldDescriptor(makeAddTodoSuccessRequestFieldDescription())),
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트_투두_추가시_이미_종료된_프로젝트일_경우() throws Exception {
        //given
        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("content", TODAY, TEN_DAY_LATER);
        final String jsonRequest = objectMapper.writeValueAsString(projectTodoRequest);
        doThrow(new BadRequestException("이미 종료된 프로젝트입니다."))
                .when(projectCreateService)
                .addProjectTodo(anyLong(), anyString(), any());

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos", 1L)
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestFields(makeFieldDescriptor(makeAddTodoSuccessRequestFieldDescription())),
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("이미 종료된 프로젝트입니다."));
    }

    @Test
    void 프로젝트_투두_추가시_리더가_아닌_경우() throws Exception {
        //given
        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("content", TODAY, TEN_DAY_LATER);
        final String jsonRequest = objectMapper.writeValueAsString(projectTodoRequest);
        doThrow(new BadRequestException("프로젝트의 리더만 투두리스트를 추가할 수 있습니다."))
                .when(projectCreateService)
                .addProjectTodo(anyLong(), anyString(), any());

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos", 1L)
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestFields(makeFieldDescriptor(makeAddTodoSuccessRequestFieldDescription())),
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("프로젝트의 리더만 투두리스트를 추가할 수 있습니다."));
    }

    @Test
    void 프로젝트_투두_추가시_컨텐츠가_250글자가_넘을_경우() throws Exception {
        //given
        final String content = "a".repeat(251);
        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest(content, TODAY, TEN_DAY_LATER);
        final String jsonRequest = objectMapper.writeValueAsString(projectTodoRequest);
        doThrow(new BadRequestException("투두 컨텐츠의 길이가 적절하지 않습니다."))
                .when(projectCreateService)
                .addProjectTodo(anyLong(), anyString(), any());

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos", 1L)
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestFields(makeFieldDescriptor(makeAddTodoSuccessRequestFieldDescription())),
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("투두 컨텐츠의 길이가 적절하지 않습니다."));
    }

    @Test
    void 프로젝트_투두리스트에_대해_체크한다() throws Exception {
        // given
        final ProjectToDoCheckResponse expected = new ProjectToDoCheckResponse(true);
        when(projectCreateService.checkProjectTodo(anyLong(), anyLong(), anyString()))
                .thenReturn(expected);

        // when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos/{todoId}", 1L, 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디"),
                                        parameterWithName("todoId").description("프로젝트 투두 아이디")),
                                responseFields(
                                        fieldWithPath("isChecked").description(
                                                "투두 체크 현황 (true: 체크됨, false: 체크되지 않음)"))))
                .andReturn();

        // then
        final ProjectToDoCheckResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(response)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_투두리스트_체크시_체크_이력이_있으면_제거한다() throws Exception {
        // given
        final ProjectToDoCheckResponse expected = new ProjectToDoCheckResponse(false);
        when(projectCreateService.checkProjectTodo(anyLong(), anyLong(), anyString()))
                .thenReturn(expected);

        // when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos/{todoId}", 1L, 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디"),
                                        parameterWithName("todoId").description("프로젝트 투두 아이디")),
                                responseFields(
                                        fieldWithPath("isChecked").description(
                                                "투두 체크 현황 (true: 체크됨, false: 체크되지 않음)"))))
                .andReturn();

        // then
        final ProjectToDoCheckResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(response)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_투두리스트_체크시_프로젝트이_존재하지_않으면_예외가_발생한다() throws Exception {
        //given
        doThrow(new NotFoundException("프로젝트이 존재하지 않습니다. projectId = 1"))
                .when(projectCreateService)
                .checkProjectTodo(anyLong(), anyLong(), anyString());

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos/{todoId}", 1L, 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디"),
                                parameterWithName("todoId").description("프로젝트 투두 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("프로젝트이 존재하지 않습니다. projectId = 1"));
    }

    @Test
    void 프로젝트_투두리스트_체크시_해당_투두가_존재하지_않으면_예외가_발생한다() throws Exception {
        //given
        doThrow(new NotFoundException("존재하지 않는 투두입니다. todoId = 1"))
                .when(projectCreateService)
                .checkProjectTodo(anyLong(), anyLong(), anyString());

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos/{todoId}", 1L, 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디"),
                                parameterWithName("todoId").description("프로젝트 투두 아이디")),
                        responseFields(
                                fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response)
                .isEqualTo(new ErrorResponse("존재하지 않는 투두입니다. todoId = 1"));
    }

    @Test
    void 프로젝트_투두리스트_체크시_사용자가_없으면_예외가_발생한다() throws Exception {
        //given
        doThrow(new NotFoundException("프로젝트에 사용자가 존재하지 않습니다. projectId = 1 memberEmail = test@email.com"))
                .when(projectCreateService)
                .checkProjectTodo(anyLong(), anyLong(), anyString());

        //when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/todos/{todoId}", 1L, 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디"),
                                parameterWithName("todoId").description("프로젝트 투두 아이디")),
                        responseFields(
                                fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response)
                .isEqualTo(new ErrorResponse("프로젝트에 사용자가 존재하지 않습니다. projectId = 1 memberEmail = test@email.com"));
    }

    @Test
    void 회고_등록_요청을_보낸다() throws Exception {
        //given
        final MemoirRequest memoirRequest = new MemoirRequest("content");
        final String jsonRequest = objectMapper.writeValueAsString(memoirRequest);

        given(projectCreateService.createMemoir(anyString(), anyLong(), any()))
                .willReturn(1L);

        //expect
        mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/memoirs", 1L)
                        .content(jsonRequest)
                        .header("Authorization", "Bearer accessToken")
                        .contextPath(API_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andDo(documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                requestFields(
                                        fieldWithPath("description").description("회고 내용")
                                )));
    }

    @Test
    void 회고_등록시_노드_기간에_해당하지_않으면_예외가_발생한다() throws Exception {
        // given
        final MemoirRequest memoirRequest = new MemoirRequest("content");
        final String jsonRequest = objectMapper.writeValueAsString(memoirRequest);

        doThrow(new BadRequestException("회고는 노드 기간 내에만 작성할 수 있습니다."))
                .when(projectCreateService)
                .createMemoir(anyString(), anyLong(), any());

        //when
        mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/memoirs", 1L)
                                .content(jsonRequest)
                                .header("Authorization", "Bearer accessToken")
                                .contextPath(API_PREFIX)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("회고는 노드 기간 내에만 작성할 수 있습니다."))
                .andDo(documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                requestFields(
                                        fieldWithPath("description").description("회고 내용")
                                ),
        responseFields(
                fieldWithPath("message").description("예외 메세지")
        )));
    }

    @Test
    void 회고_등록_요청시_멤버가_존재하지_않을_경우_예외를_반환한다() throws Exception {
        //given
        final MemoirRequest memoirRequest = new MemoirRequest("content");
        final String jsonRequest = objectMapper.writeValueAsString(memoirRequest);

        doThrow(new NotFoundException("존재하지 않는 회원입니다."))
                .when(projectCreateService)
                .createMemoir(anyString(), anyLong(), any());

        //when
        mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/memoirs", 1L)
                        .content(jsonRequest)
                                .header("Authorization", "Bearer accessToken")
                                .contextPath(API_PREFIX)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."))
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                requestFields(
                                        fieldWithPath("description").description("회고 내용")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )));
    }

    @Test
    void 회고_등록_요청시_피드가_존재하지_않을_경우_예외를_반환한다() throws Exception {
        //given
        final MemoirRequest memoirRequest = new MemoirRequest("content");
        final String jsonRequest = objectMapper.writeValueAsString(memoirRequest);

        doThrow(new NotFoundException("프로젝트 정보가 존재하지 않습니다. projectId = 1L"))
                .when(projectCreateService)
                .createMemoir(anyString(), anyLong(), any());

        //when
        mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/memoirs", 1L)
                                .content(jsonRequest)
                                .header("Authorization", "Bearer accessToken")
                                .contextPath(API_PREFIX)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("프로젝트 정보가 존재하지 않습니다. projectId = 1L"))
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                requestFields(
                                        fieldWithPath("description").description("회고 내용")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )));
    }

    @Test
    void 정상적으로_프로젝트를_나간다() throws Exception {
        // given
        final Long projectId = 1L;
        doNothing().when(projectCreateService)
                .leave(anyString(), anyLong());

        // when
        // then
        mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/leave", projectId)
                        .header(AUTHORIZATION, "Bearer <AccessToken>")
                        .contextPath(API_PREFIX))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디").optional()
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    void 프로젝트를_나갈때_존재하지_않는_회원이면_실패한다() throws Exception {
        // given
        final Long projectId = 1L;
        doThrow(new NotFoundException("존재하지 않는 회원입니다."))
                .when(projectCreateService)
                .leave(anyString(), anyLong());

        // when
        final MvcResult mvcResult = mockMvc.perform(
                        post(API_PREFIX + "/projects/{projectId}/leave", projectId)
                                .header("Authorization", "Bearer <AccessToken>")
                                .content(MediaType.APPLICATION_JSON_VALUE)
                                .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디").optional()
                        ),
                        responseFields(
                                fieldWithPath("message").description("예외 메세지")
                        )))
                .andReturn();

        // then
        final ErrorResponse errorResponse = jsonToClass(mvcResult, new TypeReference<>() {
        });
        final ErrorResponse expected = new ErrorResponse("존재하지 않는 회원입니다.");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트를_나갈때_존재하지_않는_프로젝트이면_실패한다() throws Exception {
        // given
        final Long projectId = 1L;
        doThrow(new NotFoundException("존재하지 않는 프로젝트입니다. projectId = 1"))
                .when(projectCreateService)
                .leave(anyString(), anyLong());

        // when
        final MvcResult mvcResult = mockMvc.perform(
                        post(API_PREFIX + "/projects/{projectId}/leave", projectId)
                                .header("Authorization", "Bearer <AccessToken>")
                                .content(MediaType.APPLICATION_JSON_VALUE)
                                .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 프로젝트입니다. projectId = 1"))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디").optional()
                        ),
                        responseFields(
                                fieldWithPath("message").description("예외 메세지")
                        )))
                .andReturn();

        // then
        final ErrorResponse errorResponse = jsonToClass(mvcResult, new TypeReference<>() {
        });
        final ErrorResponse expected = new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트를_나갈때_진행중인_프로젝트이면_실패한다() throws Exception {
        // given
        final Long projectId = 1L;
        doThrow(new BadRequestException("진행중인 프로젝트에서는 나갈 수 없습니다."))
                .when(projectCreateService)
                .leave(anyString(), anyLong());

        // when
        final MvcResult mvcResult = mockMvc.perform(
                        post(API_PREFIX + "/projects/{projectId}/leave", projectId)
                                .header("Authorization", "Bearer <AccessToken>")
                                .content(MediaType.APPLICATION_JSON_VALUE)
                                .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("진행중인 프로젝트에서는 나갈 수 없습니다."))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디").optional()
                        ),
                        responseFields(
                                fieldWithPath("message").description("예외 메세지")
                        )))
                .andReturn();

        // then
        final ErrorResponse errorResponse = jsonToClass(mvcResult, new TypeReference<>() {
        });
        final ErrorResponse expected = new ErrorResponse("진행중인 프로젝트에서는 나갈 수 없습니다.");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트를_나갈때_참여하지_않은_프로젝트이면_실패한다() throws Exception {
        // given
        final Long projectId = 1L;
        doThrow(new BadRequestException("프로젝트에 참여한 사용자가 아닙니다. memberId = 1"))
                .when(projectCreateService)
                .leave(anyString(), anyLong());

        // when
        final MvcResult mvcResult = mockMvc.perform(
                        post(API_PREFIX + "/projects/{projectId}/leave", projectId)
                                .header("Authorization", "Bearer <AccessToken>")
                                .content(MediaType.APPLICATION_JSON_VALUE)
                                .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("프로젝트에 참여한 사용자가 아닙니다. memberId = 1"))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디").optional()
                        ),
                        responseFields(
                                fieldWithPath("message").description("예외 메세지")
                        )))
                .andReturn();

        // then
        final ErrorResponse errorResponse = jsonToClass(mvcResult, new TypeReference<>() {
        });
        final ErrorResponse expected = new ErrorResponse("프로젝트에 참여한 사용자가 아닙니다. memberId = 1");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트를_시작한다() throws Exception {
        // given
        doNothing().when(projectCreateService)
                .startProject(anyString(), anyLong());

        // when
        mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/start", 1L)
                        .header(AUTHORIZATION, "Bearer <AccessToken>")
                        .contextPath(API_PREFIX))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디").optional()
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    void 프로젝트_시작시_존재하지_않는_사용자면_예외가_발생한다() throws Exception {
        // given
        doThrow(new NotFoundException("존재하지 않는 회원입니다."))
                .when(projectCreateService)
                .startProject(anyString(), anyLong());

        // when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/start", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("존재하지 않는 회원입니다."));
    }

    @Test
    void 프로젝트_시작시_존재하지_않는_프로젝트이면_예외가_발생한다() throws Exception {
        // given
        doThrow(new NotFoundException("존재하지 않는 프로젝트입니다. projectId = 1"))
                .when(projectCreateService)
                .startProject(anyString(), anyLong());

        // when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/start", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트를_시작하는_사용자가_프로젝트의_리더가_아니면_예외가_발생한다() throws Exception {
        // given
        doThrow(new NotFoundException("프로젝트의 리더만 프로젝트를 시작할 수 있습니다."))
                .when(projectCreateService)
                .startProject(anyString(), anyLong());

        // when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/start", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("프로젝트의 리더만 프로젝트를 시작할 수 있습니다."));
    }

    @Test
    void 프로젝트_시작시_프로젝트의_시작날짜가_미래라면_예외가_발생한다() throws Exception {
        // given
        doThrow(new NotFoundException("프로젝트의 시작 날짜가 되지 않았습니다."))
                .when(projectCreateService)
                .startProject(anyString(), anyLong());

        // when
        final MvcResult mvcResult = mockMvc.perform(post(API_PREFIX + "/projects/{projectId}/start", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andDo(documentationResultHandler.document(
                        requestHeaders(headerWithName(AUTHORIZATION).description("액세스 토큰")),
                        pathParameters(parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메세지"))))
                .andReturn();

        //then
        final ErrorResponse response = jsonToClass(mvcResult, new TypeReference<>() {
        });
        assertThat(response).isEqualTo(new ErrorResponse("프로젝트의 시작 날짜가 되지 않았습니다."));
    }

    private ResultActions 프로젝트_생성(final String jsonRequest, final ResultMatcher result) throws Exception {
        return mockMvc.perform(post(API_PREFIX + "/projects")
                        .content(jsonRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contextPath(API_PREFIX))
                .andExpect(result)
                .andDo(print());
    }

    private List<FieldDescription> makeCreateProjectSuccessRequestFieldDescription() {
        return List.of(
                new FieldDescription("feedContentId", "피드 컨텐츠 oauthId"),
                new FieldDescription("name", "프로젝트 이름", "- 길이 : 1 ~ 40"),
                new FieldDescription("limitedMemberCount", "최대 제한 인원", "- 길이 : 1 ~ 20"),
                new FieldDescription("projectFeedNodeRequests", "프로젝트 노드 정보"),
                new FieldDescription("projectFeedNodeRequests[].feedNodeId", "설정할 피드 노드의 oauthId"),
                new FieldDescription("projectFeedNodeRequests[].checkCount", "프로젝트 노드의 인증 횟수"),
                new FieldDescription("projectFeedNodeRequests[].startDate", "프로젝트 노드의 시작일", "- yyyyMMdd 형식"),
                new FieldDescription("projectFeedNodeRequests[].endDate", "프로젝트 노드의 종료일", "- yyyyMMdd 형식")
        );
    }

    private List<FieldDescription> makeAddTodoSuccessRequestFieldDescription() {
        return List.of(
                new FieldDescription("content", "프로젝트 투두 컨텐츠", "- 길이 : 1 ~ 250"),
                new FieldDescription("startDate", "프로젝트 투두 시작일", "- yyyyMMdd 형식"),
                new FieldDescription("endDate", "프로젝트 투두 종료일", "- yyyyMMdd 형식")
        );
    }
}
