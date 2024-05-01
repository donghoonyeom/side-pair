package sidepair.service.mapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.domain.project.ProjectFeedNodes;
import sidepair.domain.project.ProjectStatus;
import sidepair.domain.project.ProjectToDo;
import sidepair.domain.project.ProjectToDoCheck;
import sidepair.domain.project.ProjectToDos;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectName;
import sidepair.domain.project.vo.ProjectTodoContent;
import sidepair.persistence.project.dto.FeedProjectsOrderType;
import sidepair.persistence.project.dto.ProjectMemberSortType;
import sidepair.service.dto.feed.FeedProjectNumberDto;
import sidepair.service.dto.feed.response.FeedProjectResponse;
import sidepair.service.dto.feed.response.FeedProjectResponses;
import sidepair.service.dto.mamber.MemberDto;
import sidepair.service.dto.mamber.MemberSkillDto;
import sidepair.service.dto.mamber.response.MemberProjectForListResponse;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.mamber.response.MemberResponse;
import sidepair.service.dto.mamber.response.MemberSkillResponse;
import sidepair.service.dto.project.MemoirDto;
import sidepair.service.dto.project.FeedProjectDto;
import sidepair.service.dto.project.FeedProjectScrollDto;
import sidepair.service.dto.project.FeedProjectsOrderTypeDto;
import sidepair.service.dto.project.MemberProjectForListDto;
import sidepair.service.dto.project.ProjectMemoirDto;
import sidepair.service.dto.project.ProjectCreateDto;
import sidepair.service.dto.project.ProjectFeedNodeDetailDto;
import sidepair.service.dto.project.ProjectFeedNodeDto;
import sidepair.service.dto.project.ProjectMemberDto;
import sidepair.service.dto.project.ProjectMemberSortTypeDto;
import sidepair.service.dto.project.reequest.ProjectCreateRequest;
import sidepair.service.dto.project.reequest.ProjectFeedNodeRequest;
import sidepair.service.dto.project.reequest.ProjectStatusTypeRequest;
import sidepair.service.dto.project.reequest.ProjectTodoRequest;
import sidepair.service.dto.project.response.MemoirResponse;
import sidepair.service.dto.project.response.ProjectCertifiedResponse;
import sidepair.service.dto.project.response.ProjectMemoirResponse;
import sidepair.service.dto.project.response.ProjectFeedNodeDetailResponse;
import sidepair.service.dto.project.response.ProjectFeedNodeResponse;
import sidepair.service.dto.project.response.ProjectFeedNodesResponse;
import sidepair.service.dto.project.response.ProjectMemberResponse;
import sidepair.service.dto.project.response.ProjectResponse;
import sidepair.service.dto.project.response.ProjectToDoCheckResponse;
import sidepair.service.dto.project.response.ProjectTodoResponse;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectMapper {

    private static final int MAX_MEMBER_PROJECT_TODO_NUMBER = 3;
    private static final int MAX_MEMBER_PROJECT_MEMOIR_NUMBER = 4;

    public static ProjectCreateDto convertToProjectCreateDto(final ProjectCreateRequest projectCreateRequest) {
        final List<ProjectFeedNodeRequest> projectFeedNodeRequests = projectCreateRequest.projectFeedNodeRequests();
        final List<ProjectFeedNodeDto> projectFeedNodeDtos = makeProjectFeedNodeDtos(
                projectFeedNodeRequests);

        return new ProjectCreateDto(
                projectCreateRequest.feedContentId(),
                new ProjectName(projectCreateRequest.name()),
                new LimitedMemberCount(projectCreateRequest.limitedMemberCount()),
                projectFeedNodeDtos);
    }

    public static ProjectToDo convertToProjectTodo(final ProjectTodoRequest projectTodoRequest) {
        return new ProjectToDo(new ProjectTodoContent(projectTodoRequest.content()),
                new Period(projectTodoRequest.startDate(), projectTodoRequest.endDate()));
    }

    private static List<ProjectFeedNodeDto> makeProjectFeedNodeDtos(
            final List<ProjectFeedNodeRequest> projectFeedNodeRequests) {
        return projectFeedNodeRequests
                .stream()
                .map(it -> new ProjectFeedNodeDto(it.feedNodeId(), it.checkCount(), it.startDate(),
                        it.endDate()))
                .toList();
    }

    public static ProjectResponse convertProjectResponse(final Project project) {
        final ProjectFeedNodes nodes = project.getProjectFeedNodes();
        final List<ProjectFeedNodeResponse> feedNodeResponses = convertProjectNodeResponses(nodes);
        final int period = project.calculateTotalPeriod();
        return new ProjectResponse(project.getName().getValue(), project.getCurrentMemberCount(),
                project.getLimitedMemberCount().getValue(), feedNodeResponses, period);
    }

    public static List<ProjectFeedNodeResponse> convertProjectNodeResponses(final ProjectFeedNodes nodes) {
        return nodes.getValues().stream()
                .map(ProjectMapper::convertProjectNodeResponse)
                .toList();
    }

    private static ProjectFeedNodeResponse convertProjectNodeResponse(final ProjectFeedNode node) {
        return new ProjectFeedNodeResponse(node.getId(), node.getFeedNode().getTitle(), node.getStartDate(),
                node.getEndDate(), node.getMemoirCount());
    }

    public static List<ProjectFeedNodeDetailResponse> convertProjectNodeDetailResponses(
            final List<ProjectFeedNodeDetailDto> projectFeedNodeDetailDtos) {
        return projectFeedNodeDetailDtos.stream()
                .map(ProjectMapper::convertProjectNodeDetailResponse)
                .toList();
    }

    private static ProjectFeedNodeDetailResponse convertProjectNodeDetailResponse(
            final ProjectFeedNodeDetailDto projectFeedNodeDetailDto) {
        return new ProjectFeedNodeDetailResponse(projectFeedNodeDetailDto.id(),
                projectFeedNodeDetailDto.title(), projectFeedNodeDetailDto.description(),
                projectFeedNodeDetailDto.imageUrls(), projectFeedNodeDetailDto.startDate(),
                projectFeedNodeDetailDto.endDate(), projectFeedNodeDetailDto.checkCount());
    }

    public static ProjectCertifiedResponse convertProjectCertifiedResponse(final Project project,
                                                                           final boolean isJoined) {
        final ProjectFeedNodes nodes = project.getProjectFeedNodes();
        final List<ProjectFeedNodeResponse> feedNodeResponses = convertProjectNodeResponses(nodes);
        final int period = project.calculateTotalPeriod();
        return new ProjectCertifiedResponse(project.getName().getValue(), project.getCurrentMemberCount(),
                project.getLimitedMemberCount().getValue(), feedNodeResponses, period, isJoined);
    }

    public static FeedProjectsOrderType convertToProjectOrderType(
            final FeedProjectsOrderTypeDto orderType) {
        if (orderType == null) {
            return FeedProjectsOrderType.LATEST;
        }
        return FeedProjectsOrderType.valueOf(orderType.name());
    }

    public static FeedProjectResponses convertToFeedProjectResponses(
            final FeedProjectScrollDto feedProjectScrollDto) {
        final List<FeedProjectResponse> responses = feedProjectScrollDto.feedProjectDtos()
                .stream()
                .map(ProjectMapper::convertToFeedProjectResponse)
                .toList();
        return new FeedProjectResponses(responses, feedProjectScrollDto.hasNext());
    }

    private static FeedProjectResponse convertToFeedProjectResponse(
            final FeedProjectDto feedProjectDto) {
        return new FeedProjectResponse(feedProjectDto.projectId(), feedProjectDto.name(),
                feedProjectDto.status(), feedProjectDto.currentMemberCount(),
                feedProjectDto.limitedMemberCount(),
                feedProjectDto.createdAt(), feedProjectDto.startDate(),
                feedProjectDto.endDate(), convertToMemberResponse(feedProjectDto.projectLeader()));
    }

    private static MemberResponse convertToMemberResponse(final MemberDto memberDto) {
        return new MemberResponse(memberDto.id(), memberDto.name(), memberDto.imageUrl(), memberDto.position(),
                convertMemberSkillResponses(memberDto.skills()));
    }

    private static List<MemberSkillResponse> convertMemberSkillResponses(final List<MemberSkillDto> memberSkillDtos) {
        return memberSkillDtos.stream()
                .map(skill -> new MemberSkillResponse(skill.id(), skill.name()))
                .toList();
    }

    public static ProjectMemberSortType convertProjectMemberSortType(final ProjectMemberSortTypeDto sortType) {
        if (sortType == null) {
            return null;
        }
        return ProjectMemberSortType.valueOf(sortType.name());
    }

    public static List<ProjectMemberResponse> convertToProjectMemberResponses(
            final List<ProjectMemberDto> projectMemberDtos) {
        return projectMemberDtos.stream()
                .map(ProjectMapper::convertToProjectMemberResponse)
                .toList();
    }

    private static ProjectMemberResponse convertToProjectMemberResponse(final ProjectMemberDto projectMemberDto) {
        return new ProjectMemberResponse(projectMemberDto.memberId(), projectMemberDto.nickname(),
                projectMemberDto.imagePath(), projectMemberDto.participationRate(),
                projectMemberDto.position(), convertMemberSkillResponses(projectMemberDto.skills()));
    }

    public static List<ProjectTodoResponse> convertProjectTodoResponses(final ProjectToDos projectToDos,
                                                                        final List<ProjectToDoCheck> checkedTodos) {
        return projectToDos.getValues().stream()
                .map(projectToDo -> convertProjectTodoResponse(checkedTodos,projectToDo))
                .toList();
    }

    private static ProjectTodoResponse convertProjectTodoResponse(final List<ProjectToDoCheck> checkedTodos,
                                                                  final ProjectToDo projectToDo) {
        final ProjectToDoCheckResponse checkResponse = new ProjectToDoCheckResponse(
                isCheckedTodo(projectToDo.getId(), checkedTodos));
        return new ProjectTodoResponse(projectToDo.getId(),
                projectToDo.getContent(),
                projectToDo.getStartDate(), projectToDo.getEndDate(),
                checkResponse);
    }

    private static boolean isCheckedTodo(final Long targetTodoId, final List<ProjectToDoCheck> checkedTodos) {
        final List<Long> checkTodoIds = checkedTodos.stream()
                .map(projectToDoCheck -> projectToDoCheck.getProjectToDo().getId())
                .toList();
        return checkTodoIds.contains(targetTodoId);
    }

    public static MemberProjectResponse convertToMemberProjectResponse(final Project project,
                                                                       final List<MemoirDto> memoirDtos,
                                                                       final List<ProjectToDoCheck> checkedTodos) {
        final ProjectFeedNodesResponse nodeResponses = convertToProjectFeedNodesResponse(
                project.getProjectFeedNodes());
        final List<ProjectTodoResponse> todoResponses = convertProjectTodoResponsesLimit(project.getProjectToDos(),
                checkedTodos);
        final List<MemoirResponse> memoirRespons = convertToMemoirResponses(memoirDtos);

        return new MemberProjectResponse(project.getName().getValue(), project.getStatus().name(),
                project.findProjectLeader().getId(), project.getCurrentMemberCount(),
                project.getLimitedMemberCount().getValue(), project.getStartDate(), project.getEndDate(),
                project.getFeedContent().getId(), nodeResponses, todoResponses, memoirRespons);
    }

    private static ProjectFeedNodesResponse convertToProjectFeedNodesResponse(
            final ProjectFeedNodes nodes) {
        final ProjectFeedNode currentNode = nodes.getNodeByDate(LocalDate.now())
                .orElse(nodes.getNodeByDate(nodes.getProjectStartDate()).get());

        if (!nodes.hasBackNode(currentNode)) {
            return new ProjectFeedNodesResponse(
                    nodes.hasFrontNode(currentNode),
                    nodes.hasBackNode(currentNode),
                    List.of(convertProjectNodeResponse(currentNode))
            );
        }

        final ProjectFeedNode nextNode = nodes.nextNode(currentNode).get();
        return new ProjectFeedNodesResponse(nodes.hasFrontNode(currentNode), nodes.hasBackNode(nextNode),
                List.of(convertProjectNodeResponse(currentNode), convertProjectNodeResponse(nextNode)));
    }

    private static List<ProjectTodoResponse> convertProjectTodoResponsesLimit(final ProjectToDos projectToDos,
                                                                              final List<ProjectToDoCheck> checkedTodos) {
        return projectToDos.getValues()
                .stream()
                .map(projectToDo -> convertProjectTodoResponse(checkedTodos, projectToDo))
                .limit(MAX_MEMBER_PROJECT_TODO_NUMBER)
                .toList();
    }

    private static List<MemoirResponse> convertToMemoirResponses(final List<MemoirDto> memoirDtos) {
        return memoirDtos.stream()
                .map(memoir -> new MemoirResponse(memoir.id(), memoir.description(),
                        memoir.createdAt().toLocalDate()))
                .limit(MAX_MEMBER_PROJECT_MEMOIR_NUMBER)
                .toList();
    }


    public static ProjectStatus convertToProjectStatus(final ProjectStatusTypeRequest statusType) {
        return ProjectStatus.valueOf(statusType.name());
    }

    public static List<MemberProjectForListResponse> convertToMemberProjectForListResponses(
            final List<MemberProjectForListDto> memberProjectForListDtos) {
        return memberProjectForListDtos.stream()
                .map(ProjectMapper::convertToMemberProjectForListResponse)
                .toList();
    }

    private static MemberProjectForListResponse convertToMemberProjectForListResponse(
            final MemberProjectForListDto memberProjectForListDto) {
        final MemberDto memberDto = memberProjectForListDto.projectLeader();
        return new MemberProjectForListResponse(memberProjectForListDto.projectId(), memberProjectForListDto.name(),
                memberProjectForListDto.projectStatus(), memberProjectForListDto.currentMemberCount(),
                memberProjectForListDto.limitedMemberCount(),
                memberProjectForListDto.createdAt(), memberProjectForListDto.startDate(),
                memberProjectForListDto.endDate(),
                new MemberResponse(memberDto.id(), memberDto.name(), memberDto.imageUrl(),
                        memberDto.position(), convertMemberSkillResponses(memberDto.skills())));
    }

    public static List<ProjectMemoirResponse> convertToProjectMemoirResponses(
            final List<ProjectMemoirDto> memoirs) {
        return memoirs.stream()
                .map(ProjectMapper::convertToProjectMemoirResponse)
                .toList();
    }

    private static ProjectMemoirResponse convertToProjectMemoirResponse(
            final ProjectMemoirDto projectMemoirDto) {
        final MemberDto memberDto = projectMemoirDto.memberDto();
        final MemberResponse memberResponse = new MemberResponse(memberDto.id(), memberDto.name(),
                memberDto.imageUrl(), memberDto.position(), convertMemberSkillResponses(memberDto.skills()));

        final MemoirDto memoirDto = projectMemoirDto.memoirDto();
        final MemoirResponse memoirResponse = new MemoirResponse(memoirDto.id(), memoirDto.description(),
                memoirDto.createdAt().toLocalDate());

        return new ProjectMemoirResponse(memberResponse, memoirResponse);
    }

    public static FeedProjectNumberDto convertFeedProjectDto(final List<Project> projects) {
        final Map<ProjectStatus, List<Project>> projectsDividedByStatus = projects.stream()
                .collect(Collectors.groupingBy(Project::getStatus));
        return new FeedProjectNumberDto(
                projectsDividedByStatus.getOrDefault(ProjectStatus.RECRUITING, Collections.emptyList()).size(),
                projectsDividedByStatus.getOrDefault(ProjectStatus.RUNNING, Collections.emptyList()).size(),
                projectsDividedByStatus.getOrDefault(ProjectStatus.COMPLETED, Collections.emptyList()).size()
        );
    }
}
