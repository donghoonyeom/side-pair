package sidepair.service.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.member.Member;
import sidepair.domain.member.vo.Email;
import sidepair.domain.project.Memoir;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.domain.project.ProjectFeedNodes;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectPendingMember;
import sidepair.domain.project.ProjectToDo;
import sidepair.domain.project.ProjectToDoCheck;
import sidepair.domain.project.vo.Period;
import sidepair.persistence.feed.FeedContentRepository;
import sidepair.persistence.member.MemberRepository;
import sidepair.persistence.project.MemoirRepository;
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.persistence.project.ProjectToDoCheckRepository;
import sidepair.service.aop.ExceptionConvert;
import sidepair.service.dto.project.ProjectCreateDto;
import sidepair.service.dto.project.ProjectFeedNodeDto;
import sidepair.service.dto.project.request.MemoirRequest;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectTodoRequest;
import sidepair.service.dto.project.response.ProjectToDoCheckResponse;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.mapper.ProjectMapper;

@Service
@Transactional
@RequiredArgsConstructor
@ExceptionConvert
public class ProjectCreateService {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final FeedContentRepository feedContentRepository;
    private final MemoirRepository memoirRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectToDoCheckRepository projectToDoCheckRepository;

    public Long create(final ProjectCreateRequest projectCreateRequest, final String memberEmail) {
        final ProjectCreateDto projectCreateDto = ProjectMapper.convertToProjectCreateDto(projectCreateRequest);
        final FeedContent feedContent = findFeedContentById(projectCreateDto.feedContentId());
        validateDeletedFeed(feedContent);
        validateNodeSizeEqual(feedContent.nodesSize(), projectCreateDto.projectFeedNodeDtosSize());
        validateFeedCreator(feedContent, memberEmail);
        final Member leader = findMemberByEmail(memberEmail);
        final ProjectFeedNodes projectFeedNodes = makeProjectFeedNodes(
                projectCreateDto.projectFeedNodeDtos(), feedContent);

        final Project project = Project.createProject(projectCreateDto.projectName(), projectCreateDto.limitedMemberCount(),
                feedContent, leader);
        project.addAllProjectFeedNodes(projectFeedNodes);
        return projectRepository.save(project).getId();
    }

    private FeedContent findFeedContentById(final Long feedContentId) {
        return feedContentRepository.findByIdWithFeed(feedContentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 피드입니다."));
    }

    private void validateDeletedFeed(final FeedContent feedContent) {
        final Feed feed = feedContent.getFeed();
        if (feed.isDeleted()) {
            throw new BadRequestException("삭제된 피드에 대해 프로젝트를 생성할 수 없습니다.");
        }
    }

    private void validateFeedCreator(final FeedContent feedContent, final String email) {
        final Member creator = findMemberByEmail(email);
        if (feedContent.isNotFeedCreator(creator)) {
            throw new ForbiddenException("피드를 생성한 사용자가 아닙니다.");
        }
    }

    private void validateNodeSizeEqual(final int feedNodesSize, final int projectFeedNodeDtosSize) {
        if (feedNodesSize != projectFeedNodeDtosSize) {
            throw new BadRequestException("모든 노드에 대해 기간이 설정돼야 합니다.");
        }
    }

    private ProjectFeedNodes makeProjectFeedNodes(final List<ProjectFeedNodeDto> projectFeedNodeDtos,
                                                  final FeedContent feedContent) {
        final List<ProjectFeedNode> projectFeedNodes = projectFeedNodeDtos.stream()
                .map(it -> makeProjectFeedNode(feedContent, it))
                .toList();
        return new ProjectFeedNodes(projectFeedNodes);
    }

    private ProjectFeedNode makeProjectFeedNode(final FeedContent feedContent,
                                                final ProjectFeedNodeDto it) {
        return new ProjectFeedNode(new Period(it.startDate(), it.endDate()), it.memoirCount(),
                findFeedNode(feedContent, it.feedNodeId()));
    }

    private FeedNode findFeedNode(final FeedContent feedContent, final Long feedNodeId) {
        return feedContent.findFeedNodeById(feedNodeId)
                .orElseThrow(() -> new NotFoundException("피드에 존재하지 않는 노드입니다."));
    }

    private Member findMemberByEmail(final String memberEmail) {
        return memberRepository.findByEmail(new Email(memberEmail))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    private void checkProjectLeader(final Member member, final Project project, final String errorMessage) {
        if (project.isNotLeader(member)) {
            throw new BadRequestException(errorMessage);
        }
    }

    public Long createMemoir(final String email, final Long projectId,
                             final MemoirRequest memoirRequest) {
        final Project project = findProjectById(projectId);
        final ProjectMember projectMember = findProjectMemberByProjectAndEmail(project, email);
        final ProjectFeedNode currentNode = getNodeByDate(project);
        final int currentMemberMemoirCount = memoirRepository.countByProjectMemberAndProjectFeedNode(
                projectMember, currentNode);
        validateMemoirCount(currentMemberMemoirCount, projectMember, currentNode);
        updateParticipationRate(project, projectMember, currentMemberMemoirCount);

        final Memoir memoir = new Memoir(memoirRequest.description(), currentNode, projectMember);

        return memoirRepository.save(memoir).getId();
    }

    public Long addProjectTodo(final Long projectId, final String email,
                               final ProjectTodoRequest projectTodoRequest) {
        final Member member = findMemberByEmail(email);
        final Project project = findProjectById(projectId);
        checkProjectCompleted(project);
        checkProjectLeader(member, project, "프로젝트의 리더만 투두리스트를 추가할 수 있습니다.");
        final ProjectToDo projectToDo = ProjectMapper.convertToProjectTodo(projectTodoRequest);
        project.addProjectTodo(projectToDo);
        projectRepository.save(project);
        return project.findLastProjectTodo().getId();
    }

    private Project findProjectById(final Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다. projectId = " + projectId));
    }

    private void checkProjectCompleted(final Project project) {
        if (project.isCompleted()) {
            throw new BadRequestException("이미 종료된 프로젝트입니다.");
        }
    }

    private void updateParticipationRate(final Project project, final ProjectMember projectMember,
                                         final int pastMemoirCount) {
        final int wholeMemoirCount = project.getAllMemoirCount();
        final int memberMemoirCount = pastMemoirCount + 1;
        final Double participationRate = 100 * memberMemoirCount / (double) wholeMemoirCount;
        projectMember.updateParticipationRate(participationRate);
    }

    public ProjectToDoCheckResponse checkProjectTodo(final Long projectId, final Long todoId,
                                                     final String email) {
        final Email memberEmail = new Email(email);
        final Project project = findProjectWithTodos(projectId);
        final ProjectToDo projectToDo = findProjectTodoById(todoId, project);
        final ProjectMember projectMember = findProjectMember(memberEmail, project);

        final boolean isAlreadyChecked = projectToDoCheckRepository.findByProjectIdAndTodoAndMemberEmail(
                projectId, projectToDo, memberEmail).isPresent();
        if (isAlreadyChecked) {
            projectToDoCheckRepository.deleteByProjectMemberAndToDoId(projectMember, todoId);
            return new ProjectToDoCheckResponse(false);
        }
        final ProjectToDoCheck projectToDoCheck = new ProjectToDoCheck(projectMember, projectToDo);
        projectToDoCheckRepository.save(projectToDoCheck);
        return new ProjectToDoCheckResponse(true);
    }

    private Project findProjectWithTodos(final Long projectId) {
        return projectRepository.findByIdWithTodos(projectId)
                .orElseThrow(() -> new NotFoundException("프로젝트가 존재하지 않습니다. projectId = " + projectId));
    }

    private ProjectToDo findProjectTodoById(final Long todoId, final Project project) {
        return project.findProjectTodoByTodoId(todoId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 투두입니다. todoId = " + todoId));
    }

    private void validateMemoirCount(final int memberMemoirCount, final ProjectMember member,
                                     final ProjectFeedNode projectFeedNode) {
        validateNodeMemoirCount(memberMemoirCount, projectFeedNode);
        validateTodayMemoirCount(member);
    }

    private ProjectMember findProjectMember(final Email memberEmail, final Project project) {
        return projectMemberRepository.findByProjectAndMemberEmail(project, memberEmail)
                .orElseThrow(() -> new NotFoundException(
                        "프로젝트에 회원이 존재하지 않습니다. projectId = " + project.getId() + " memberEmail = "
                                + memberEmail.getValue()));
    }

    private ProjectMember findProjectMemberByProjectAndEmail(final Project project, final String email) {
        return projectMemberRepository.findByProjectAndMemberEmail(project, new Email(email))
                .orElseThrow(() -> new NotFoundException("프로젝트에 해당 사용자가 존재하지 않습니다. 사용자 아이디 = " + email));
    }

    private ProjectFeedNode getNodeByDate(final Project project) {
        return project.findNodeByDate(LocalDate.now())
                .orElseThrow(() -> new BadRequestException("회고는 노드 기간 내에만 작성할 수 있습니다."));
    }

    private void validateNodeMemoirCount(final int memberMemoirCount,
                                         final ProjectFeedNode projectFeedNode) {
        if (memberMemoirCount >= projectFeedNode.getMemoirCount()) {
            throw new BadRequestException(
                    "이번 노드에는 최대 " + projectFeedNode.getMemoirCount() + "번만 회고를 등록할 수 있습니다.");
        }
    }

    public void startProject(final String memberEmail, final Long projectId) {
        final Member member = findMemberByEmail(memberEmail);
        final Project project = findProjectById(projectId);
        checkProjectLeader(member, project, "프로젝트의 리더만 프로젝트를 시작할 수 있습니다.");
        validateProjectStart(project);
        final List<ProjectPendingMember> projectPendingMembers = project.getProjectPendingMembers().getValues();
        saveProjectMemberFromPendingMembers(projectPendingMembers, project);
        project.start();
    }

    private void validateProjectStart(final Project project) {
        if (project.cannotStart()) {
            throw new BadRequestException("프로젝트의 시작 날짜가 되지 않았습니다.");
        }
    }

    private void validateTodayMemoirCount(final ProjectMember member) {
        final LocalDate today = LocalDate.now();
        final LocalDateTime todayStart = today.atStartOfDay();
        final LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        if (memoirRepository.findByProjectMemberAndDateTime(member, todayStart, todayEnd).isPresent()) {
            throw new BadRequestException("이미 오늘 회고를 등록하였습니다.");
        }
    }

    private void saveProjectMemberFromPendingMembers(final List<ProjectPendingMember> projectPendingMembers,
                                                     final Project project) {
        final List<ProjectMember> projectMembers = makeProjectMembers(projectPendingMembers);
        project.addAllProjectMembers(projectMembers);
        project.deleteAllPendingMembers();
    }

    private List<ProjectMember> makeProjectMembers(final List<ProjectPendingMember> projectPendingMembers) {
        return projectPendingMembers.stream()
                .map(this::makeProjectMember)
                .toList();
    }

    private ProjectMember makeProjectMember(final ProjectPendingMember projectPendingMember) {
        return new ProjectMember(projectPendingMember.getRole(),
                projectPendingMember.getJoinedAt(), projectPendingMember.getProject(),
                projectPendingMember.getMember());
    }

    public void leave(final String email, final Long projectId) {
        final Member member = findMemberByEmail(email);
        final Project project = findProjectById(projectId);
        validateStatus(project);
        project.leave(member);
        if (project.isEmptyProject()) {
            projectRepository.delete(project);
        }
    }

    private void validateStatus(final Project project) {
        if (project.isRunning()) {
            throw new BadRequestException("진행중인 프로젝트에서는 나갈 수 없습니다.");
        }
    }
}
