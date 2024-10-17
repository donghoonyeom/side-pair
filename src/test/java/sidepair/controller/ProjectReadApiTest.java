package sidepair.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
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
import static sidepair.persistence.project.dto.ProjectMemberSortType.PARTICIPATION_RATE;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;
import sidepair.controller.helper.ControllerTestHelper;
import sidepair.domain.member.Position;
import sidepair.domain.project.ProjectStatus;
import sidepair.service.dto.ErrorResponse;
import sidepair.service.dto.mamber.response.MemberProjectForListResponse;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.mamber.response.MemberResponse;
import sidepair.service.dto.mamber.response.MemberSkillResponse;
import sidepair.service.dto.project.request.ProjectStatusTypeRequest;
import sidepair.service.dto.project.response.MemoirResponse;
import sidepair.service.dto.project.response.ProjectCertifiedResponse;
import sidepair.service.dto.project.response.ProjectFeedNodeDetailResponse;
import sidepair.service.dto.project.response.ProjectFeedNodeResponse;
import sidepair.service.dto.project.response.ProjectFeedNodesResponse;
import sidepair.service.dto.project.response.ProjectMemberResponse;
import sidepair.service.dto.project.response.ProjectMemoirResponse;
import sidepair.service.dto.project.response.ProjectResponse;
import sidepair.service.dto.project.response.ProjectToDoCheckResponse;
import sidepair.service.dto.project.response.ProjectTodoResponse;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.project.ProjectCreateService;
import sidepair.service.project.ProjectReadService;

@WebMvcTest(ProjectController.class)
class ProjectReadApiTest extends ControllerTestHelper{

    @MockBean
    private ProjectReadService projectReadService;

    @MockBean
    private ProjectCreateService projectCreateService;

    @Test
    void 프로젝트_아이디로_프로젝트를_조회한다() throws Exception {
        // given
        final ProjectResponse expected = 프로젝트_조회_응답을_생성한다();
        when(projectReadService.findProject(any()))
                .thenReturn(expected);

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/projects/{projectId}", 1L)
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("name").description("프로젝트 제목"),
                                        fieldWithPath("currentMemberCount").description("현재 참여 인원 수"),
                                        fieldWithPath("limitedMemberCount").description("모집 인원 수"),
                                        fieldWithPath("projectNodes[0].id").description("프로젝트 피드 노드 아이디"),
                                        fieldWithPath("projectNodes[0].title").description("프로젝트 피드 노드 제목"),
                                        fieldWithPath("projectNodes[0].startDate").description("프로젝트 피드 노드 시작 날짜"),
                                        fieldWithPath("projectNodes[0].endDate").description("프로젝트 피드 노드 종료 날짜"),
                                        fieldWithPath("projectNodes[0].checkCount").description("프로젝트 피드 노드 회고 횟수"),
                                        fieldWithPath("period").description("프로젝트 진행 기간"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ProjectResponse 프로젝트_단일_조회_응답 = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertThat(프로젝트_단일_조회_응답)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디로_프로젝트_조회시_아이디가_유효하지_않으면_예외가_발생한다() throws Exception {
        // given
        when(projectReadService.findProject(any()))
                .thenThrow(new NotFoundException("프로젝트 정보가 존재하지 않습니다. projectId = 1L"));

        // when
        final String response = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}", 1L)
                        .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value("프로젝트 정보가 존재하지 않습니다. projectId = 1L"))
                .andDo(documentationResultHandler.document(
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메시지"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse("프로젝트 정보가 존재하지 않습니다. projectId = 1L");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디와_사용자_아이디로_프로젝트를_조회한다() throws Exception {
        // given
        final ProjectCertifiedResponse expected = 로그인시_프로젝트_조회_응답을_생성한다(true);
        when(projectReadService.findProject(any(), any()))
                .thenReturn(expected);

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/projects/{projectId}", 1L)
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("name").description("프로젝트 제목"),
                                        fieldWithPath("currentMemberCount").description("현재 참여 인원 수"),
                                        fieldWithPath("limitedMemberCount").description("모집 인원 수"),
                                        fieldWithPath("projectNodes[0].id").description("프로젝트 피드 노드 아이디"),
                                        fieldWithPath("projectNodes[0].title").description("프로젝트 피드 노드 제목"),
                                        fieldWithPath("projectNodes[0].startDate").description("프로젝트 피드 노드 시작 날짜"),
                                        fieldWithPath("projectNodes[0].endDate").description("프로젝트 피드 노드 종료 날짜"),
                                        fieldWithPath("projectNodes[0].checkCount").description("프로젝트 피드 노드 회고 갯수"),
                                        fieldWithPath("period").description("프로젝트 진행 기간"),
                                        fieldWithPath("isJoined").description("프로젝트 참여 여부 (true / false)"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ProjectCertifiedResponse 프로젝트_단일_조회_응답 = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertThat(프로젝트_단일_조회_응답)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디와_사용자_아이디로_프로젝트_조회시_프로젝트_아이디가_유효하지_않으면_예외_발생() throws Exception {
        // given
        when(projectReadService.findProject(any(), any()))
                .thenThrow(new NotFoundException("프로젝트 정보가 존재하지 않습니다. projectId = 1L"));

        // when
        final String response = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value("프로젝트 정보가 존재하지 않습니다. projectId = 1L"))
                .andDo(documentationResultHandler.document(
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 아이디")),
                        responseFields(fieldWithPath("message").description("예외 메시지"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        final ErrorResponse expected = new ErrorResponse("프로젝트 정보가 존재하지 않습니다. projectId = 1L");
        assertThat(errorResponse)
                .isEqualTo(expected);
    }

    @Test
    void 사용자_단일_프로젝트를_조회한다() throws Exception {
        //given
        final MemberProjectResponse expected = 사용자_프로젝트_조회_응답을_생성한다();
        when(projectReadService.findMemberProject(any(), any()))
                .thenReturn(expected);

        //when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/projects/{projectId}/me", 1L)
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "access-token"))
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("name").description("프로젝트 이름"),
                                        fieldWithPath("status").description("프로젝트 상태"),
                                        fieldWithPath("leaderId").description("프로젝트 리더 아이디"),
                                        fieldWithPath("currentMemberCount").description("현재 프로젝트 참여자 수"),
                                        fieldWithPath("limitedMemberCount").description("프로젝트 참여 제한 인원 수"),
                                        fieldWithPath("startDate").description("프로젝트 시작 날짜"),
                                        fieldWithPath("endDate").description("프로젝트 종료 날짜"),
                                        fieldWithPath("feedContentId").description("피드 컨텐츠 아이디"),
                                        fieldWithPath("projectFeedNodes.hasFrontNode").description(
                                                "대시 보드에 표시된 프로젝트 노드 앞의 노드 존재 여부"),
                                        fieldWithPath("projectFeedNodes.hasBackNode").description(
                                                "대시 보드에 표시된 프로젝트 노드 이후의 노드 존재 여부"),
                                        fieldWithPath("projectFeedNodes.nodes[0].id").description(
                                                "프로젝트 피드 노드 아이디"),
                                        fieldWithPath("projectFeedNodes.nodes[0].title").description(
                                                "프로젝트 피드 노드 제목"),
                                        fieldWithPath("projectFeedNodes.nodes[0].startDate").description(
                                                "프로젝트 피드 노드 시작일"),
                                        fieldWithPath("projectFeedNodes.nodes[0].endDate").description(
                                                "프로젝트 피드 노드 종료일"),
                                        fieldWithPath("projectFeedNodes.nodes[0].checkCount").description(
                                                "프로젝트 피드 노드 최대 회고 갯수"),
                                        fieldWithPath("projectTodos[0].id").description("프로젝트 투두 아이디"),
                                        fieldWithPath("projectTodos[0].content").description("프로젝트 투두 내용"),
                                        fieldWithPath("projectTodos[0].startDate").description("프로젝트 투두 시작일"),
                                        fieldWithPath("projectTodos[0].endDate").description("프로젝트 투두 종료일"),
                                        fieldWithPath("projectTodos[0].check.isChecked").description(
                                                "프로젝트 투두 체크 여부(true/false)"),
                                        fieldWithPath("memoirs[0].id").description("회고 아이디"),
                                        fieldWithPath("memoirs[0].description").description("회고 본문"),
                                        fieldWithPath("memoirs[0].createdAt").description("회고 등록 날짜")
                                )))
                .andReturn().getResponse()
                .getContentAsString();

        //then
        final MemberProjectResponse memberProjectResponses = objectMapper.readValue(response,
                new TypeReference<>() {
                });

        assertThat(memberProjectResponses)
                .isEqualTo(expected);
    }

    @Test
    void 사용자_프로젝트_조회_시_유효하지_않은_프로젝트_아이디를_보내면_예외가_발생한다() throws Exception {
        //given
        when(projectReadService.findMemberProject(any(), any()))
                .thenThrow(new ForbiddenException("프로젝트 정보가 존재하지 않습니다. projectId = 1"));

        //when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/projects/{projectId}/me", 1L)
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "access-token"))
                                .contextPath(API_PREFIX))
                .andExpect(status().isForbidden())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메시지")
                                )))
                .andReturn().getResponse()
                .getContentAsString();

        //then
        final ErrorResponse errorResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(errorResponse.message()).isEqualTo("프로젝트 정보가 존재하지 않습니다. projectId = 1");
    }

    @Test
    void 사용자_참가_프로젝트_목록을_조회한다() throws Exception {
        //given
        final List<MemberProjectForListResponse> expected = 사용자_프로젝트_목록_조회_응답을_생성한다();
        when(projectReadService.findMemberProjectsByStatusType(any(), any()))
                .thenReturn(expected);

        //when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/projects/me")
                                .param("statusCond", ProjectStatusTypeRequest.RUNNING.name())
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "access-token"))
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")
                                ),
                                queryParameters(
                                        parameterWithName("statusCond").description("프로젝트 상태")
                                ),
                                responseFields(
                                        fieldWithPath("[0].projectId").description("프로젝트 아이디"),
                                        fieldWithPath("[0].name").description("프로젝트 이름"),
                                        fieldWithPath("[0].projectStatus").description("프로젝트 상태"),
                                        fieldWithPath("[0].currentMemberCount").description("현재 프로젝트 참여자 수"),
                                        fieldWithPath("[0].limitedMemberCount").description("프로젝트 참여 제한 인원 수"),
                                        fieldWithPath("[0].createdAt").description("프로젝트 생성 시간"),
                                        fieldWithPath("[0].startDate").description("프로젝트 시작 날짜"),
                                        fieldWithPath("[0].endDate").description("프로젝트 종료 날짜"),
                                        fieldWithPath("[0].projectLeader.id").description("프로젝트 생성 사용자 아이디"),
                                        fieldWithPath("[0].projectLeader.name").description("프로젝트 생성 사용자 닉네임"),
                                        fieldWithPath("[0].projectLeader.imageUrl").description("프로젝트 생성 사용자 프로필 이미지 경로"),
                                        fieldWithPath("[0].projectLeader.position").description("프로젝트 생성 사용자 포지션"),
                                        fieldWithPath("[0].projectLeader.skills[].id").description("프로젝트 생성 사용자 기술 ID"),
                                        fieldWithPath("[0].projectLeader.skills[].name").description("프로젝트 생성 사용자 기술 이름")
                                )))
                .andReturn().getResponse()
                .getContentAsString();

        //then
        final List<MemberProjectForListResponse> memberProjectResponses = objectMapper.readValue(response,
                new TypeReference<>() {
                });

        assertThat(memberProjectResponses)
                .isEqualTo(expected);
    }

    @Test
    void 정상적으로_프로젝트_멤버를_조회한다() throws Exception {
        // given
        final ProjectMemberResponse projectMemberResponse1 = new ProjectMemberResponse(1L, "nickname1", "imagePath1",
                50D, Position.BACKEND.name(), List.of(new MemberSkillResponse(1L, "Java")));
        final ProjectMemberResponse projectMemberResponse2 = new ProjectMemberResponse(2L, "nickname2", "imagePath2",
                40D, Position.BACKEND.name(), List.of(new MemberSkillResponse(1L, "Java")));
        given(projectReadService.findProjectMembers(anyLong(), any()))
                .willReturn(List.of(projectMemberResponse1, projectMemberResponse2));

        // when
        final MvcResult mvcResult = mockMvc.perform(
                        get(API_PREFIX + "/projects/{projectId}/members", 1L)
                                .param("sortCond", PARTICIPATION_RATE.name())
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("Bearer 액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                queryParameters(
                                        parameterWithName("sortCond")
                                                .description(
                                                        "정렬 조건 (null일 경우: 프로젝트 모집중 -> 프로젝트 입장 순(오래된순)/ 프로젝트 진행중, 완료됨 -> 회고 작성 순으로 기본 정렬) +"
                                                                + "\n"
                                                                + "PARTICIPATION_RATE : 회고 작성 순 +" + "\n"
                                                                + "JOINED_ASC : 프로젝트 입장 순 (오래된순) +" + "\n"
                                                                + "JOINED_DESC : 프로젝트 입장 순 (최신순) +" + "\n")
                                                .optional()
                                ),
                                responseFields(
                                        fieldWithPath("[0].memberId").description("회원 id"),
                                        fieldWithPath("[0].nickname").description("회원 닉네임"),
                                        fieldWithPath("[0].imagePath").description("회원 이미지 경로"),
                                        fieldWithPath("[0].participationRate").description("회원 참여율"),
                                        fieldWithPath("[0].position").description("사용자 포지션"),
                                        fieldWithPath("[0].skills[].id").description("기술 ID"),
                                        fieldWithPath("[0].skills[].name").description("기술 이름"))))
                .andReturn();

        // then
        final List<ProjectMemberResponse> response = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(response)
                .isEqualTo(List.of(projectMemberResponse1, projectMemberResponse2));
    }

    @Test
    void 프로젝트_멤버_조회_시_존재하지_않는_프로젝트일_경우() throws Exception {
        //given
        doThrow(new NotFoundException("존재하지 않는 프로젝트입니다. projectId = 1"))
                .when(projectReadService)
                .findProjectMembers(anyLong(), any());

        //when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/members", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .param("sortCond", PARTICIPATION_RATE.name())
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                queryParameters(
                                        parameterWithName("sortCond")
                                                .description(
                                                        "정렬 조건 (null일 경우: 프로젝트 모집중 -> 프로젝트 입장 순(오래된순)/ 프로젝트 진행중, 완료됨 -> 회고 작성 순으로 기본 정렬) +"
                                                                + "\n"
                                                                + "PARTICIPATION_RATE : 회고 작성 순 +" + "\n"
                                                                + "JOINED_ASC : 프로젝트 입장 순 (오래된순) +" + "\n"
                                                                + "JOINED_DESC : 프로젝트 입장 순 (최신순) +" + "\n")
                                                .optional()
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )))
                .andReturn();

        // then
        final ErrorResponse responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트의_투두리스트를_조회한다() throws Exception {
        // given
        final LocalDate today = LocalDate.now();
        final List<ProjectTodoResponse> projectTodoResponses = List.of(
                new ProjectTodoResponse(1L, "투두 1", today, today.plusDays(10), new ProjectToDoCheckResponse(true)),
                new ProjectTodoResponse(2L, "투두 2", today.plusDays(20), today.plusDays(30),
                        new ProjectToDoCheckResponse(false)));

        when(projectReadService.findAllProjectTodo(any(), any()))
                .thenReturn(projectTodoResponses);

        // when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/todos", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("[0].id").description("투두 아이디"),
                                        fieldWithPath("[0].content").description("투두 내용"),
                                        fieldWithPath("[0].startDate").description("투두 시작 날짜"),
                                        fieldWithPath("[0].endDate").description("투두 종료 날짜"),
                                        fieldWithPath("[0].check.isChecked").description("투두 체크 여부")
                                )))
                .andReturn();

        // then
        final List<ProjectTodoResponse> response = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(response)
                .isEqualTo(projectTodoResponses);
    }

    @Test
    void 프로젝트_투두리스트_조회시_존재하지_않은_프로젝트일_경우() throws Exception {
        // given
        doThrow(new NotFoundException("존재하지 않는 프로젝트입니다. projectId = 1"))
                .when(projectReadService)
                .findAllProjectTodo(any(), any());

        // when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/todos", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )))
                .andReturn();

        // then
        final ErrorResponse responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses)
                .isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트_투두리스트_조회시_참여하지_않은_사용자일_경우() throws Exception {
        // given
        doThrow(new ForbiddenException("프로젝트에 참여하지 않은 사용자입니다. projectId = 1 memberEmail = email"))
                .when(projectReadService)
                .findAllProjectTodo(any(), any());

        // when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/todos", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isForbidden())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )))
                .andReturn();

        // then
        final ErrorResponse responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses)
                .isEqualTo(new ErrorResponse("프로젝트에 참여하지 않은 사용자입니다. projectId = 1 memberEmail = email"));
    }

    @Test
    void 프로젝트의_노드를_조회한다() throws Exception {
        // given
        final LocalDate today = LocalDate.now();
        final List<ProjectFeedNodeDetailResponse> projectNodeResponses = List.of(
                new ProjectFeedNodeDetailResponse(1L, "프로젝트 노드 1", "프로젝트 노드 본문1",
                        List.of("image1-filepath", "image2-filepath"), today, today.plusDays(10), 10),
                new ProjectFeedNodeDetailResponse(2L, "프로젝트 노드 2", "프로젝트 노드 본문2",
                        List.of("image1-filepath", "image2-filepath"), today.plusDays(20), today.plusDays(30), 5));

        when(projectReadService.findAllProjectNodes(any(), any()))
                .thenReturn(projectNodeResponses);

        // when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/nodes", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("[0].id").description("프로젝트 피드 노드 아이디"),
                                        fieldWithPath("[0].title").description("노드 제목"),
                                        fieldWithPath("[0].description").description("노드 본문"),
                                        fieldWithPath("[0].imageUrls[0]").description("노드 이미지 파일 경로"),
                                        fieldWithPath("[0].startDate").description("노드 시작 날짜"),
                                        fieldWithPath("[0].endDate").description("노드 종료 날짜"),
                                        fieldWithPath("[0].checkCount").description("회고 갯수")
                                )))
                .andReturn();

        // then
        final List<ProjectFeedNodeDetailResponse> response = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(response)
                .isEqualTo(projectNodeResponses);
    }

    @Test
    void 프로젝트_노드_조회시_존재하지_않은_프로젝트일_경우() throws Exception {
        // given
        doThrow(new NotFoundException("존재하지 않는 프로젝트입니다. projectId = 1"))
                .when(projectReadService)
                .findAllProjectNodes(any(), any());

        // when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/nodes", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )))
                .andReturn();

        // then
        final ErrorResponse responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses)
                .isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트_노드_조회시_참여하지_않은_사용자일_경우() throws Exception {
        // given
        doThrow(new ForbiddenException("프로젝트에 참여하지 않은 사용자입니다. projectId = 1 memberEmail = email"))
                .when(projectReadService)
                .findAllProjectNodes(any(), any());

        // when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/nodes", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isForbidden())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )))
                .andReturn();

        // then
        final ErrorResponse responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses)
                .isEqualTo(new ErrorResponse("프로젝트에 참여하지 않은 사용자입니다. projectId = 1 memberEmail = email"));
    }

    @Test
    void 프로젝트의_회고를_전체_조회한다() throws Exception {
        // given
        final ProjectMemoirResponse projectMemoirResponse1 = new ProjectMemoirResponse(
                new MemberResponse(1L, "name1", "imageUrl", Position.BACKEND.name(),
                        List.of(new MemberSkillResponse(1L, "Java"))),
                new MemoirResponse(1L, "회고 글", LocalDate.now()));
        final ProjectMemoirResponse projectMemoirResponse2 = new ProjectMemoirResponse(
                new MemberResponse(2L, "name2", "imageUrl", Position.BACKEND.name(),
                        List.of(new MemberSkillResponse(1L, "Java"))),
                new MemoirResponse(2L, "회고 글",  LocalDate.now()));

        final List<ProjectMemoirResponse> expected = List.of(projectMemoirResponse2,
                projectMemoirResponse1);

        when(projectReadService.findProjectMemoirs(any(), any()))
                .thenReturn(expected);

        // when
        final String response = mockMvc.perform(
                        get(API_PREFIX + "/projects/{projectId}/memoirs", 1L)
                                .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                                .contextPath(API_PREFIX))
                .andExpect(status().isOk())
                .andDo(
                        documentationResultHandler.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION).description("액세스 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("[0].member.id").description("사용자 ID"),
                                        fieldWithPath("[0].member.name").description("사용자 닉네임"),
                                        fieldWithPath("[0].member.imageUrl").description("사용자 이미지 Url"),
                                        fieldWithPath("[0].member.position").description("사용자 포지션"),
                                        fieldWithPath("[0].member.skills[].id").description("기술 ID"),
                                        fieldWithPath("[0].member.skills[].name").description("기술 이름"),
                                        fieldWithPath("[0].memoir.id").description("회고 ID"),
                                        fieldWithPath("[0].memoir.description").description("회고 설명"),
                                        fieldWithPath("[0].memoir.createdAt").description("회고 등록 날짜"))))
                .andReturn().getResponse()
                .getContentAsString();

        // then
        final List<ProjectMemoirResponse> 프로젝트_회고_전체_조회_응답 = objectMapper.readValue(response,
                new TypeReference<>() {
                });
        assertThat(프로젝트_회고_전체_조회_응답)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_회고_전체_조회_시_존재하지_않는_프로젝트일_경우_예외가_발생한다() throws Exception {
        //given
        doThrow(new NotFoundException("존재하지 않는 프로젝트입니다. projectId = 1"))
                .when(projectReadService)
                .findProjectMemoirs(any(), any());

        //when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/memoirs", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isNotFound())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )))
                .andReturn();

        // then
        final ErrorResponse responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).isEqualTo(new ErrorResponse("존재하지 않는 프로젝트입니다. projectId = 1"));
    }

    @Test
    void 프로젝트_회고_전체_조회_시_프로젝트에_참여하지_않은_사용자일_경우_예외_발생() throws Exception {
        //given
        doThrow(new BadRequestException("프로젝트에 참여하지 않은 회원입니다."))
                .when(projectReadService)
                .findProjectMemoirs(any(), any());

        //when
        final MvcResult mvcResult = mockMvc.perform(get(API_PREFIX + "/projects/{projectId}/memoirs", 1L)
                        .header(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "test-token"))
                        .contextPath(API_PREFIX))
                .andExpect(status().isBadRequest())
                .andDo(
                        documentationResultHandler.document(
                                pathParameters(
                                        parameterWithName("projectId").description("프로젝트 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("message").description("예외 메세지")
                                )))
                .andReturn();

        // then
        final ErrorResponse responses = jsonToClass(mvcResult, new TypeReference<>() {
        });

        assertThat(responses).isEqualTo(new ErrorResponse("프로젝트에 참여하지 않은 회원입니다."));
    }

    private ProjectResponse 프로젝트_조회_응답을_생성한다() {
        final List<ProjectFeedNodeResponse> projectNodeResponses = List.of(
                new ProjectFeedNodeResponse(1L, "피드 1주차", LocalDate.of(2023, 7, 19),
                        LocalDate.of(2023, 7, 30), 10),
                new ProjectFeedNodeResponse(2L, "피드 2주차", LocalDate.of(2023, 8, 1),
                        LocalDate.of(2023, 8, 5), 2));
        return new ProjectResponse("프로젝트", 1, 10, projectNodeResponses, 17);
    }

    private ProjectCertifiedResponse 로그인시_프로젝트_조회_응답을_생성한다(final boolean isJoined) {
        final List<ProjectFeedNodeResponse> projectNodeResponses = List.of(
                new ProjectFeedNodeResponse(1L, "피드 1주차", LocalDate.of(2023, 7, 19),
                        LocalDate.of(2023, 7, 30), 10),
                new ProjectFeedNodeResponse(2L, "피드 2주차", LocalDate.of(2023, 8, 1),
                        LocalDate.of(2023, 8, 5), 2));
        return new ProjectCertifiedResponse("프로젝트", 1, 10, projectNodeResponses, 17, isJoined);
    }

    private MemberProjectResponse 사용자_프로젝트_조회_응답을_생성한다() {
        return new MemberProjectResponse("프로젝트 이름", "RUNNING", 1L,
                15, 20, LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31), 1L,
                new ProjectFeedNodesResponse(false, true, List.of(
                        new ProjectFeedNodeResponse(1L, "첫번째 프로젝트 노드 제목", LocalDate.of(2023, 1, 1),
                                LocalDate.of(2023, 1, 31), 15),
                        new ProjectFeedNodeResponse(2L, "두번째 프로젝트 노드 제목", LocalDate.of(2023, 2, 1),
                                LocalDate.of(2023, 2, 28), 14))),
                List.of(new ProjectTodoResponse(1L, "첫 번째 할일",
                        LocalDate.of(2023, 1, 15), LocalDate.of(2023, 1, 31),
                        new ProjectToDoCheckResponse(false))),
                List.of(new MemoirResponse(1L,  "회고 설명 1", LocalDate.now()),
                        new MemoirResponse(2L,  "회고 설명 2", LocalDate.now()),
                        new MemoirResponse(3L,  "회고 설명 3", LocalDate.now()),
                        new MemoirResponse(4L,  "회고 설명 4", LocalDate.now())));

    }

    private List<MemberProjectForListResponse> 사용자_프로젝트_목록_조회_응답을_생성한다() {
        return List.of(new MemberProjectForListResponse(1L, "프로젝트 이름", ProjectStatus.RUNNING.name(),
                        15, 20, LocalDateTime.of(2023, 7, 1, 0, 0),
                        LocalDate.of(2023, 7, 15), LocalDate.of(2023, 8, 15),
                        new MemberResponse(1L, "사이드", "default-member-image", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java")))),
                new MemberProjectForListResponse(2L, "프로젝트 이름", ProjectStatus.RUNNING.name(),
                        15, 20, LocalDateTime.of(2023, 7, 5, 0, 0),
                        LocalDate.of(2023, 7, 8), LocalDate.of(2023, 8, 1),
                        new MemberResponse(2L, "페어", "default-member-image", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java"))))
        );
    }
}
