package sidepair.service.project;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.member.Member;
import sidepair.domain.member.MemberProfile;
import sidepair.domain.member.MemberSkills;
import sidepair.domain.member.vo.Email;
import sidepair.domain.project.Memoir;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.domain.project.ProjectFeedNodes;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectPendingMember;
import sidepair.domain.project.ProjectStatus;
import sidepair.domain.project.ProjectToDoCheck;
import sidepair.domain.project.ProjectToDos;
import sidepair.persistence.member.MemberRepository;
import sidepair.persistence.project.MemoirRepository;
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectPendingMemberRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.persistence.project.ProjectToDoCheckRepository;
import sidepair.persistence.project.dto.ProjectMemberSortType;
import sidepair.service.FileService;
import sidepair.service.aop.ExceptionConvert;
import sidepair.service.dto.mamber.MemberDto;
import sidepair.service.dto.mamber.MemberSkillDto;
import sidepair.service.dto.mamber.response.MemberProjectForListResponse;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.project.MemoirDto;
import sidepair.service.dto.project.MemberProjectForListDto;
import sidepair.service.dto.project.ProjectMemoirDto;
import sidepair.service.dto.project.ProjectFeedNodeDetailDto;
import sidepair.service.dto.project.ProjectMemberDto;
import sidepair.service.dto.project.ProjectMemberSortTypeDto;
import sidepair.service.dto.project.request.ProjectStatusTypeRequest;
import sidepair.service.dto.project.response.ProjectCertifiedResponse;
import sidepair.service.dto.project.response.ProjectMemoirResponse;
import sidepair.service.dto.project.response.ProjectFeedNodeDetailResponse;
import sidepair.service.dto.project.response.ProjectMemberResponse;
import sidepair.service.dto.project.response.ProjectResponse;
import sidepair.service.dto.project.response.ProjectTodoResponse;
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.mapper.ProjectMapper;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@ExceptionConvert
public class ProjectReadService {
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectToDoCheckRepository projectToDoCheckRepository;
    private final MemoirRepository memoirRepository;
    private final ProjectPendingMemberRepository projectPendingMemberRepository;
    private final FileService fileService;

    public ProjectResponse findProject(final Long projectId) {
        final Project project = findProjectWithFeedContentById(projectId);
        return ProjectMapper.convertProjectResponse(project);
    }

    private Project findProjectWithFeedContentById(final Long projectId) {
        return projectRepository.findByIdWithFeedContent(projectId)
                .orElseThrow(() -> new NotFoundException("프로젝트 정보가 존재하지 않습니다. projectId = " + projectId));
    }

    public ProjectCertifiedResponse findProject(final String email, final Long projectId) {
        final Project project = findProjectWithFeedContentById(projectId);
        final boolean isJoined = isMemberProjectJoin(new Email(email), project);
        return ProjectMapper.convertProjectCertifiedResponse(project, isJoined);
    }

    private boolean isMemberProjectJoin(final Email email, final Project project) {
        if (project.isRecruiting()) {
            return projectPendingMemberRepository.findByProjectAndMemberEmail(project, email).isPresent();
        }
        return projectMemberRepository.findByProjectAndMemberEmail(project, email).isPresent();
    }

    public List<ProjectMemberResponse> findProjectMembers(final Long projectId,
                                                          final ProjectMemberSortTypeDto sortType) {
        final Project project = findProjectById(projectId);
        final ProjectMemberSortType projectMemberSortType = ProjectMapper.convertProjectMemberSortType(sortType);
        if (project.isRecruiting()) {
            final List<ProjectPendingMember> projectPendingMembers = projectPendingMemberRepository.findByProjectIdOrderedBySortType(
                    projectId, projectMemberSortType);
            final List<ProjectMemberDto> projectMemberDtos = makeProjectMemberDtosWithParticipationRateZero(
                    projectPendingMembers);
            return ProjectMapper.convertToProjectMemberResponses(projectMemberDtos);
        }
        final List<ProjectMember> projectMembers = projectMemberRepository.findByProjectIdOrderedBySortType(
                projectId, projectMemberSortType);
        final List<ProjectMemberDto> projectMemberDtos = makeProjectMemberDtos(projectMembers);
        return ProjectMapper.convertToProjectMemberResponses(projectMemberDtos);
    }

    private Project findProjectById(final Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다. projectId = " + projectId));
    }

    private List<ProjectMemberDto> makeProjectMemberDtosWithParticipationRateZero(
            final List<ProjectPendingMember> projectPendingMembers) {
        return projectPendingMembers.stream()
                .map(this::makeProjectMemberDtoWithParticipationRateZero)
                .toList();
    }

    private ProjectMemberDto makeProjectMemberDtoWithParticipationRateZero(
            final ProjectPendingMember projectPendingMember) {
        final Member member = projectPendingMember.getMember();
        final MemberProfile memberProfile = member.getMemberProfile();
        final URL memberImageUrl = fileService.generateUrl(member.getImage().getServerFilePath(), HttpMethod.GET);
        return new ProjectMemberDto(member.getId(), member.getNickname().getValue(),
                memberImageUrl.toExternalForm(), 0D, memberProfile.getPosition().name(),
                makeMemberSkillDtos(member.getSkills()));
    }

    private List<ProjectMemberDto> makeProjectMemberDtos(
            final List<ProjectMember> projectPendingMembers) {
        return projectPendingMembers.stream()
                .map(this::makeProjectMemberDto)
                .toList();
    }

    private ProjectMemberDto makeProjectMemberDto(final ProjectMember projectMember) {
        final Member member = projectMember.getMember();
        final MemberProfile memberProfile = member.getMemberProfile();
        final URL memberImageUrl = fileService.generateUrl(member.getImage().getServerFilePath(), HttpMethod.GET);
        return new ProjectMemberDto(member.getId(), member.getNickname().getValue(),
                memberImageUrl.toExternalForm(), projectMember.getParticipationRate(),
                memberProfile.getPosition().name()
                , makeMemberSkillDtos(member.getSkills()));
    }

    public List<ProjectMemoirResponse> findProjectMemoirs(final String email, final Long projectId) {
        final Project project = findProjectWithNodesById(projectId);
        validateJoinedMemberInRunningProject(project, email);
        final Optional<ProjectFeedNode> currentProjectFeedNode = findCurrentProjectNode(project);
        final List<Memoir> memoirs = findMemoirsByNodeAndProjectStatusWithMember(project,
                currentProjectFeedNode);
        final List<ProjectMemoirDto> projectMemoirDtos = makeProjectMemoirDtos(memoirs);
        return ProjectMapper.convertToProjectMemoirResponses(projectMemoirDtos);
    }

    private List<MemoirDto> makeMemoirDtos(final List<Memoir> memoirs) {
        return memoirs.stream()
                .map(this::makeMemoirDto)
                .collect(Collectors.toList());
    }

    private MemoirDto makeMemoirDto(final Memoir memoir) {
        return new MemoirDto(memoir.getId(), memoir.getDescription(),
                memoir.getCreatedAt());
    }

    public List<ProjectMemoirDto> makeProjectMemoirDtos(
            final List<Memoir> memoirs) {
        return memoirs.stream()
                .map(this::makeProjectMemoirDto)
                .toList();
    }

    private ProjectMemoirDto makeProjectMemoirDto(final Memoir memoir) {
        final ProjectMember projectMember = memoir.getProjectMember();
        final Member member = projectMember.getMember();
        final MemberProfile profile = member.getMemberProfile();

        final URL memberImageUrl = fileService.generateUrl(member.getImage().getServerFilePath(), HttpMethod.GET);

        return new ProjectMemoirDto(new MemberDto(member.getId(), member.getNickname().getValue(),
                memberImageUrl.toExternalForm(), profile.getPosition().name(),
                makeMemberSkillDtos(member.getSkills())), makeMemoirDto(memoir));
    }

    private List<Memoir> findMemoirsByNodeAndProjectStatusWithMember(final Project project,
                                                                        final Optional<ProjectFeedNode> currentProjectFeedNode) {
        if (project.isCompleted()) {
            return memoirRepository.findByProjectWithMemberAndMemberImage(project);
        }
        if (project.isRunning() && currentProjectFeedNode.isPresent()) {
            return memoirRepository.findByRunningProjectFeedNodeWithMemberAndMemberImage(
                    currentProjectFeedNode.get());
        }
        return Collections.emptyList();
    }

    private List<Memoir> findMemoirsByNodeAndProjectStatus(final Project project,
                                                              final Optional<ProjectFeedNode> currentProjectFeedNode) {
        if (project.isCompleted()) {
            return memoirRepository.findByProject(project);
        }
        if (project.isRunning() && currentProjectFeedNode.isPresent()) {
            return memoirRepository.findByRunningProjectFeedNode(currentProjectFeedNode.get());
        }
        return Collections.emptyList();
    }


    public List<ProjectTodoResponse> findAllProjectTodo(final Long projectId, final String email) {
        final ProjectToDos projectToDos = findProjectTodosByProjectId(projectId);
        validateProjectMember(projectId, email);
        final List<ProjectToDoCheck> checkedTodos = findMemberCheckedProjectToDoIds(projectId, email);
        return ProjectMapper.convertProjectTodoResponses(projectToDos, checkedTodos);
    }

    private void validateProjectMember(final Long projectId, final String email) {
        if (projectMemberRepository.findProjectMember(projectId, new Email(email)).isEmpty()) {
            throw new ForbiddenException(
                    "프로젝트에 참여하지 않은 사용자입니다. projectId = " + projectId + " memberEmail = " + email);
        }
    }

    private ProjectToDos findProjectTodosByProjectId(final Long projectId) {
        return projectRepository.findByIdWithTodos(projectId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다. projectId = " + projectId))
                .getProjectToDos();
    }

    private List<ProjectToDoCheck> findMemberCheckedProjectToDoIds(final Long projectId, final String email) {
        return projectToDoCheckRepository.findByProjectIdAndMemberEmail(projectId, new Email(email));
    }

    public MemberProjectResponse findMemberProject(final String email, final Long projectId) {
        final Project project = findMemberProjectById(projectId);
        final Member member = findMemberByEmail(new Email(email));
        validateMemberInProject(project, member);

        final Optional<ProjectFeedNode> currentProjectFeedNode = findCurrentProjectNode(project);
        final List<Memoir> memoirs = findMemoirsByNodeAndProjectStatus(project, currentProjectFeedNode);
        final List<ProjectToDoCheck> checkedTodos = findMemberCheckedProjectToDoIds(projectId, email);
        final List<MemoirDto> memoirDtos = makeMemoirDtos(memoirs);
        return ProjectMapper.convertToMemberProjectResponse(project, memoirDtos, checkedTodos);
    }

    private Project findMemberProjectById(final Long projectId) {
        return projectRepository.findByIdWithContentAndTodos(projectId)
                .orElseThrow(() -> new NotFoundException("프로젝트 정보가 존재하지 않습니다. projectId = " + projectId));
    }

    private Member findMemberByEmail(final Email email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    private void validateMemberInProject(final Project project, final Member member) {
        if (!project.isProjectMember(member)) {
            throw new ForbiddenException("해당 프로젝트에 참여하지 않은 사용자입니다.");
        }
    }

    private Optional<ProjectFeedNode> findCurrentProjectNode(final Project project) {
        return project.findNodeByDate(LocalDate.now());
    }

    public List<MemberProjectForListResponse> findMemberProjects(final String email) {
        final Member member = findMemberByEmail(new Email(email));
        final List<Project> memberProjects = projectRepository.findByMember(member);
        final List<MemberProjectForListDto> memberProjectForListDtos = makeMemberProjectForListDto(
                memberProjects);
        return ProjectMapper.convertToMemberProjectForListResponses(memberProjectForListDtos);
    }

    private List<MemberProjectForListDto> makeMemberProjectForListDto(final List<Project> memberProjects) {
        return memberProjects.stream()
                .map(this::makeMemberProjectForListDto)
                .toList();
    }

    private MemberProjectForListDto makeMemberProjectForListDto(final Project project) {
        final Member leader = project.findProjectLeader();
        final MemberProfile profile = leader.getMemberProfile();
        final URL leaderImageUrl = fileService.generateUrl(leader.getImage().getServerFilePath(), HttpMethod.GET);
        return new MemberProjectForListDto(project.getId(), project.getName().getValue(),
                project.getStatus().name(), project.getCurrentMemberCount(),
                project.getLimitedMemberCount().getValue(),
                project.getCreatedAt(), project.getStartDate(), project.getEndDate(),
                new MemberDto(leader.getId(), leader.getNickname().getValue(), leaderImageUrl.toExternalForm(),
                        profile.getPosition().name(), makeMemberSkillDtos(leader.getSkills())));
    }

    private List<MemberSkillDto> makeMemberSkillDtos(final MemberSkills memberSkills) {
        return memberSkills.getValues()
                .stream()
                .map(it -> new MemberSkillDto(it.getId(), it.getName().getValue()))
                .toList();
    }

    public List<MemberProjectForListResponse> findMemberProjectsByStatusType(final String email,
                                                                             final ProjectStatusTypeRequest projectStatusTypeRequest) {
        final Member member = findMemberByEmail(new Email(email));
        final ProjectStatus projectStatus = ProjectMapper.convertToProjectStatus(projectStatusTypeRequest);
        final List<Project> memberProjects = projectRepository.findByMemberAndStatus(member, projectStatus);
        final List<MemberProjectForListDto> memberProjectForListDtos = makeMemberProjectForListDto(
                memberProjects);
        return ProjectMapper.convertToMemberProjectForListResponses(memberProjectForListDtos);
    }

    public List<ProjectFeedNodeDetailResponse> findAllProjectNodes(final Long projectId,
                                                                   final String email) {
        final Project project = findProjectWithNodesByProjectId(projectId);
        final ProjectFeedNodes projectNodes = project.getProjectFeedNodes();
        validateProjectMember(projectId, email);
        final List<ProjectFeedNodeDetailDto> projectFeedNodeDetailDtos = makeProjectNodeDetailDtos(
                projectNodes);

        return ProjectMapper.convertProjectNodeDetailResponses(projectFeedNodeDetailDtos);
    }

    private Project findProjectWithNodesByProjectId(final Long projectId) {
        return projectRepository.findByIdWithNodes(projectId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다. projectId = " + projectId));
    }

    public List<ProjectFeedNodeDetailDto> makeProjectNodeDetailDtos(
            final ProjectFeedNodes nodes) {
        return nodes.getValues().stream()
                .map(this::makeProjectNodeDetailResponse)
                .toList();
    }

    private ProjectFeedNodeDetailDto makeProjectNodeDetailResponse(final ProjectFeedNode node) {
        final FeedNode feedNode = node.getFeedNode();
        return new ProjectFeedNodeDetailDto(node.getId(), feedNode.getTitle(), feedNode.getContent(),
                makeFeedNodeImageUrls(feedNode), node.getStartDate(), node.getEndDate(), node.getMemoirCount());
    }

    private List<String> makeFeedNodeImageUrls(final FeedNode feedNode) {
        return feedNode.getFeedNodeImages()
                .getValues()
                .stream()
                .map(it -> fileService.generateUrl(it.getServerFilePath(), HttpMethod.GET).toExternalForm())
                .toList();
    }

    private Project findProjectWithNodesById(final Long projectId) {
        return projectRepository.findByIdWithNodes(projectId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다. projectId = " + projectId));
    }

    private void validateJoinedMemberInRunningProject(final Project project, final String email) {
        if (projectMemberRepository.findByProjectAndMemberEmail(project, new Email(email))
                .isEmpty()) {
            throw new ForbiddenException("프로젝트에 참여하지 않은 회원입니다.");
        }
    }
}
