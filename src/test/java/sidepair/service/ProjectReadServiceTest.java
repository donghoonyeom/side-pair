package sidepair.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sidepair.domain.ImageContentType;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedContents;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.feed.FeedNodeImage;
import sidepair.domain.feed.FeedNodeImages;
import sidepair.domain.feed.FeedNodes;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.MemberProfile;
import sidepair.domain.member.MemberSkill;
import sidepair.domain.member.MemberSkills;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.MemberImage;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.domain.member.vo.SkillName;
import sidepair.domain.project.Memoir;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.domain.project.ProjectFeedNodes;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectPendingMember;
import sidepair.domain.project.ProjectRole;
import sidepair.domain.project.ProjectToDo;
import sidepair.domain.project.ProjectToDoCheck;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectName;
import sidepair.domain.project.vo.ProjectTodoContent;
import sidepair.persistence.member.MemberRepository;
import sidepair.persistence.project.MemoirRepository;
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectPendingMemberRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.persistence.project.ProjectToDoCheckRepository;
import sidepair.service.dto.mamber.response.MemberProjectForListResponse;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.mamber.response.MemberResponse;
import sidepair.service.dto.mamber.response.MemberSkillResponse;
import sidepair.service.dto.project.ProjectMemberSortTypeDto;
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
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.project.ProjectReadService;

@ExtendWith(MockitoExtension.class)
class ProjectReadServiceTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);
    private static final LocalDate TWENTY_DAY_LAYER = TODAY.plusDays(20);
    private static final LocalDate THIRTY_DAY_LATER = TODAY.plusDays(30);

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectPendingMemberRepository projectPendingMemberRepository;

    @Mock
    private ProjectToDoCheckRepository projectToDoCheckRepository;

    @Mock
    private MemoirRepository memoirRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ProjectReadService projectReadService;

    @Test
    void 프로젝트_아이디로_프로젝트_정보를_조회한다() {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(creator, targetFeedContent);

        when(projectRepository.findByIdWithFeedContent(any()))
                .thenReturn(Optional.of(project));

        // when
        final ProjectResponse projectResponse = projectReadService.findProject(project.getId());
        final ProjectResponse expected = 예상하는_프로젝트_응답을_생성한다();

        // then
        assertThat(projectResponse)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_조회시_프로젝트_아이디가_유효하지_않으면_예외가_발생한다() {
        // given
        when(projectRepository.findByIdWithFeedContent(any()))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectReadService.findProject(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 모집중인_프로젝트에_대해서_프로젝트_아이디와_사용자_아이디로_프로젝트_대기_목록_조회시_참여하는_사용자면_참여여부가_true로_반환된다() {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(creator, targetFeedContent);

        final ProjectPendingMember projectPendingMember = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 19, 12, 0, 0), project, creator);

        when(projectRepository.findByIdWithFeedContent(any()))
                .thenReturn(Optional.of(project));
        when(projectPendingMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.of(projectPendingMember));

        // when
        final ProjectCertifiedResponse projectResponse = projectReadService.findProject(
                creator.getEmail().getValue(), project.getId());
        final ProjectCertifiedResponse expected = 예상하는_로그인된_사용자의_프로젝트_응답을_생성한다(true, 1);

        // then
        assertThat(projectResponse)
                .isEqualTo(expected);
    }

    @Test
    void 모집중인_프로젝트에_대해서_프로젝트_아이디와_사용자_아이디로_프로젝트_대기_목록_조회시_참여하지_않는_사용자면_참여여부가_false로_반환된다() {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(creator, targetFeedContent);

        when(projectRepository.findByIdWithFeedContent(any()))
                .thenReturn(Optional.of(project));
        when(projectPendingMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.empty());

        // when
        final ProjectCertifiedResponse projectResponse = projectReadService.findProject(
                creator.getEmail().getValue(), project.getId());
        final ProjectCertifiedResponse expected = 예상하는_로그인된_사용자의_프로젝트_응답을_생성한다(false, 1);

        // then
        assertThat(projectResponse)
                .isEqualTo(expected);
    }

    @Test
    void 모집중이지_않은_프로젝트에_대해서_프로젝트_아이디와_사용자_아이디로_프로젝트_사용자_목록_조회시_참여하는_사용자면_참여여부가_true로_반환된다() {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(creator, targetFeedContent);
        project.start();

        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 19, 12, 0, 0), project, creator);

        when(projectRepository.findByIdWithFeedContent(any()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.of(projectMember));

        // when
        final ProjectCertifiedResponse projectResponse = projectReadService.findProject(
                creator.getEmail().getValue(), project.getId());
        final ProjectCertifiedResponse expected = 예상하는_로그인된_사용자의_프로젝트_응답을_생성한다(true, 0);

        // then
        assertThat(projectResponse)
                .isEqualTo(expected);
    }

    @Test
    void 모집중이지_않은_프로젝트에_대해서_프로젝트_아이디와_사용자_아이디로_프로젝트_사용자_목록_조회시_참여하지_않는_사용자면_참여여부가_false로_반환된다() {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(creator, targetFeedContent);
        project.start();

        when(projectRepository.findByIdWithFeedContent(any()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.empty());

        // when
        final ProjectCertifiedResponse projectResponse = projectReadService.findProject(
                creator.getEmail().getValue(), project.getId());
        final ProjectCertifiedResponse expected = 예상하는_로그인된_사용자의_프로젝트_응답을_생성한다(false, 0);

        // then
        assertThat(projectResponse)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디와_사용자_아이디로_프로젝트_대기_목록_조회시_프로젝트_아이디가_유효하지_않으면_예외가_발생한다() {
        // given
        when(projectRepository.findByIdWithFeedContent(any()))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectReadService.findProject("test@email.com", 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 정상적으로_진행중인_프로젝트의_참여자를_조회한다() throws MalformedURLException {
        //given
        final Member creator = 사용자를_생성한다(1L);
        final Member follower = 사용자를_생성한다(2L);

        final Feed feed = 피드를_생성한다(creator);

        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));
        project.start();

        final ProjectMember projectMemberCreator = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(),
                project, creator);
        final ProjectMember projectMemberFollower = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(),
                project, follower);

        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));
        given(projectMemberRepository.findByProjectIdOrderedBySortType(anyLong(), any()))
                .willReturn(List.of(projectMemberCreator, projectMemberFollower));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        //when
        final List<ProjectMemberResponse> result = projectReadService.findProjectMembers(1L,
                ProjectMemberSortTypeDto.JOINED_DESC);

        //then
        final ProjectMemberResponse expectedProjectMemberResponse1 = new ProjectMemberResponse(1L, "name1",
                "http://example.com/serverFilePath", 0.0, Position.FRONTEND.name(),
                List.of(new MemberSkillResponse(1L, "HTML")));
        final ProjectMemberResponse expectedProjectMemberResponse2 = new ProjectMemberResponse(2L, "name1",
                "http://example.com/serverFilePath", 0.0, Position.FRONTEND.name(),
                List.of(new MemberSkillResponse(1L, "HTML")));
        assertThat(result)
                .isEqualTo(List.of(expectedProjectMemberResponse1, expectedProjectMemberResponse2));
        verify(projectPendingMemberRepository, never()).findByProjectIdOrderedBySortType(anyLong(), any());
    }

    @Test
    void 정상적으로_완료된_프로젝트의_참여자를_조회한다() throws MalformedURLException {
        //given
        final Member creator = 사용자를_생성한다(1L);
        final Member follower = 사용자를_생성한다(2L);

        final Feed feed = 피드를_생성한다(creator);

        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));
        project.complete();

        final ProjectMember projectMemberCreator = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(),
                project, creator);
        final ProjectMember projectMemberFollower = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(),
                project, follower);

        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));
        given(projectMemberRepository.findByProjectIdOrderedBySortType(anyLong(), any()))
                .willReturn(List.of(projectMemberCreator, projectMemberFollower));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        //when
        final List<ProjectMemberResponse> result = projectReadService.findProjectMembers(1L,
                ProjectMemberSortTypeDto.JOINED_DESC);

        //then
        final ProjectMemberResponse expectedProjectMemberResponse1 = new ProjectMemberResponse(1L, "name1",
                "http://example.com/serverFilePath", 0.0, Position.FRONTEND.name(),
                List.of(new MemberSkillResponse(1L, "HTML")));
        final ProjectMemberResponse expectedProjectMemberResponse2 = new ProjectMemberResponse(2L, "name1",
                "http://example.com/serverFilePath", 0.0, Position.FRONTEND.name(),
                List.of(new MemberSkillResponse(1L, "HTML")));
        assertThat(result)
                .isEqualTo(List.of(expectedProjectMemberResponse1, expectedProjectMemberResponse2));
        verify(projectPendingMemberRepository, never()).findByProjectIdOrderedBySortType(anyLong(), any());
    }

    @Test
    void 정상적으로_모집중인_프로젝트의_참여자를_조회한다() throws MalformedURLException {
        //given
        final Member creator = 사용자를_생성한다(1L);
        final Member follower = 사용자를_생성한다(2L);

        final Feed feed = 피드를_생성한다(creator);

        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));

        final ProjectPendingMember projectMemberCreator = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), project, creator);
        final ProjectPendingMember projectMemberFollower = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), project, follower);

        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));
        given(projectPendingMemberRepository.findByProjectIdOrderedBySortType(anyLong(), any()))
                .willReturn(List.of(projectMemberCreator, projectMemberFollower));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        //when
        final List<ProjectMemberResponse> result = projectReadService.findProjectMembers(1L,
                ProjectMemberSortTypeDto.JOINED_DESC);

        //then
        final ProjectMemberResponse expectedProjectMemberResponse1 = new ProjectMemberResponse(1L, "name1",
                "http://example.com/serverFilePath", 0.0, Position.FRONTEND.name(),
                List.of(new MemberSkillResponse(1L, "HTML")));
        final ProjectMemberResponse expectedProjectMemberResponse2 = new ProjectMemberResponse(2L, "name1",
                "http://example.com/serverFilePath", 0.0, Position.FRONTEND.name(),
                List.of(new MemberSkillResponse(1L, "HTML")));
        assertThat(result)
                .isEqualTo(List.of(expectedProjectMemberResponse1, expectedProjectMemberResponse2));
        verify(projectMemberRepository, never()).findByProjectIdOrderedBySortType(anyLong(), any());
    }

    @Test
    void 존재하지_않는_프로젝트일_경우_예외를_던진다() {
        //given
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> projectReadService.findProjectMembers(1L,
                ProjectMemberSortTypeDto.JOINED_DESC))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트의_전체_투두리스트를_조회한다() {
        // given
        final Member creator = 사용자를_생성한다(1L);
        final Feed feed = 피드를_생성한다(creator);
        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));

        final ProjectToDo firstProjectTodo = new ProjectToDo(1L, new ProjectTodoContent("투두 1"),
                new Period(TODAY, TEN_DAY_LATER));
        final ProjectToDo secondProjectTodo = new ProjectToDo(2L, new ProjectTodoContent("투두 2"),
                new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER));
        project.addProjectTodo(firstProjectTodo);
        project.addProjectTodo(secondProjectTodo);

        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        when(projectMemberRepository.findProjectMember(anyLong(), any()))
                .thenReturn(Optional.of(projectMember));
        when(projectRepository.findByIdWithTodos(1L))
                .thenReturn(Optional.of(project));
        when(projectToDoCheckRepository.findByProjectIdAndMemberEmail(anyLong(), any()))
                .thenReturn(List.of(
                        new ProjectToDoCheck(projectMember, firstProjectTodo)
                ));

        // when
        final List<ProjectTodoResponse> responses = projectReadService.findAllProjectTodo(1L, "test@email.com");
        final List<ProjectTodoResponse> expected = List.of(
                new ProjectTodoResponse(1L, "투두 1", TODAY, TEN_DAY_LATER, new ProjectToDoCheckResponse(true)),
                new ProjectTodoResponse(2L, "투두 2", TWENTY_DAY_LAYER, THIRTY_DAY_LATER,
                        new ProjectToDoCheckResponse(false)));

        // then
        assertThat(responses)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트의_투두리스트_조회시_존재하지_않는_프로젝트이면_예외가_발생한다() {
        // given
        when(projectRepository.findByIdWithTodos(1L))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectReadService.findAllProjectTodo(1L, "test@email.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트의_투두리스트_조회시_프로젝트에_참여하지_않은_사용자면_예외가_발생한다() {
        // given
        final Member creator = 사용자를_생성한다(1L);
        final Feed feed = 피드를_생성한다(creator);
        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));

        final ProjectToDo firstProjectTodo = new ProjectToDo(1L, new ProjectTodoContent("투두 1"),
                new Period(TODAY, TEN_DAY_LATER));
        final ProjectToDo secondProjectTodo = new ProjectToDo(2L, new ProjectTodoContent("투두 2"),
                new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER));
        project.addProjectTodo(firstProjectTodo);
        project.addProjectTodo(secondProjectTodo);

        when(projectRepository.findByIdWithTodos(1L))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findProjectMember(anyLong(), any()))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectReadService.findAllProjectTodo(1L, "test@email.com"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 진행중인_사용자_단일_프로젝트를_조회한다() throws MalformedURLException {
        // given
        final Member creator = 사용자를_생성한다(1L);
        final Feed feed = 피드를_생성한다(creator);
        final FeedNode feedNode1 = new FeedNode("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = new FeedNode("피드 2주차", "피드 2주차 내용");
        final FeedNode feedNode3 = new FeedNode("피드 3주차", "피드 3주차 내용");
        final FeedNode feedNode4 = new FeedNode("피드 4주차", "피드 4주차 내용");
        final FeedNodes feedNodes = new FeedNodes(
                List.of(feedNode1, feedNode2, feedNode3, feedNode4));
        final FeedContent feedContent = new FeedContent("피드 본문");
        feedContent.addNodes(feedNodes);
        feed.addContent(feedContent);

        final ProjectFeedNode projectFeedNode1 = new ProjectFeedNode(
                new Period(TODAY, TODAY.plusDays(10)), 5, feedNode1);
        final ProjectFeedNode projectFeedNode2 = new ProjectFeedNode(
                new Period(TODAY.plusDays(11), TODAY.plusDays(20)), 5, feedNode2);

        final Project project = new Project(1L, new ProjectName("project"), new LimitedMemberCount(6),
                feedContent, creator);
        project.start();

        project.addAllProjectFeedNodes(
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)));
        project.addAllProjectMembers(
                List.of(new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator)));

        final List<Memoir> memoirs = 회고글_목록을_생성한다(projectFeedNode1, creator, project);
        given(projectRepository.findByIdWithContentAndTodos(anyLong()))
                .willReturn(Optional.of(project));
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(creator));
        given(memoirRepository.findByRunningProjectFeedNode(any()))
                .willReturn(memoirs);

        final MemberProjectResponse expected = new MemberProjectResponse(project.getName().getValue(),
                project.getStatus().name(), creator.getId(), project.getCurrentMemberCount(),
                project.getLimitedMemberCount().getValue(), project.getStartDate(), project.getEndDate(),
                feedContent.getId(), new ProjectFeedNodesResponse(false, true,
                List.of(
                        new ProjectFeedNodeResponse(projectFeedNode1.getId(), feedNode1.getTitle(),
                                projectFeedNode1.getStartDate(),
                                projectFeedNode1.getEndDate(), projectFeedNode1.getMemoirCount()),
                        new ProjectFeedNodeResponse(projectFeedNode2.getId(), feedNode2.getTitle(),
                                projectFeedNode2.getStartDate(),
                                projectFeedNode2.getEndDate(), projectFeedNode2.getMemoirCount())
                )), Collections.emptyList(),
                List.of(
                        new MemoirResponse(1L, "회고글 내용", LocalDate.now()),
                        new MemoirResponse(2L, "회고글 내용", LocalDate.now()),
                        new MemoirResponse(3L, "회고글 내용", LocalDate.now()),
                        new MemoirResponse(4L, "회고글 내용", LocalDate.now())
                ));

        //when
        final MemberProjectResponse response = projectReadService.findMemberProject("test1@email.com", 1L);

        //then
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("memoirs.id", "memoirs.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 모집중인_사용자_단일_프로젝트_조회시_회고글이_빈_응답을_반환한다() {
        // given
        final Member member1 = 사용자를_생성한다(1L);
        final FeedNode feedNode1 = new FeedNode("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = new FeedNode("피드 2주차", "피드 2주차 내용");
        final FeedNode feedNode3 = new FeedNode("피드 3주차", "피드 3주차 내용");
        final FeedNode feedNode4 = new FeedNode("피드 4주차", "피드 4주차 내용");
        final FeedNodes feedNodes = new FeedNodes(
                List.of(feedNode1, feedNode2, feedNode3, feedNode4));
        final FeedContent feedContent = new FeedContent("피드 본문");
        final Feed feed = 피드를_생성한다(member1);
        feedContent.addNodes(feedNodes);
        feed.addContent(feedContent);

        final ProjectFeedNode projectFeedNode1 = new ProjectFeedNode(
                new Period(TODAY, TODAY.plusDays(10)), 5, feedNode1);
        final ProjectFeedNode projectFeedNode2 = new ProjectFeedNode(
                new Period(TODAY.plusDays(11), TODAY.plusDays(20)), 5, feedNode2);
        final ProjectFeedNode projectFeedNode3 = new ProjectFeedNode(
                new Period(TODAY.plusDays(21), TODAY.plusDays(30)), 5, feedNode1);
        final ProjectFeedNode projectFeedNode4 = new ProjectFeedNode(
                new Period(TODAY.plusDays(31), TODAY.plusDays(40)), 5, feedNode2);

        final Project project = new Project(1L, new ProjectName("project"), new LimitedMemberCount(6),
                feedContent, member1);
        project.addAllProjectFeedNodes(
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2, projectFeedNode3,
                        projectFeedNode4)));

        given(projectRepository.findByIdWithContentAndTodos(anyLong()))
                .willReturn(Optional.of(project));
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member1));

        final MemberProjectResponse expected = new MemberProjectResponse(project.getName().getValue(),
                project.getStatus().name(), member1.getId(), project.getCurrentMemberCount(),
                project.getLimitedMemberCount().getValue(), project.getStartDate(), project.getEndDate(),
                feedContent.getId(), new ProjectFeedNodesResponse(false, true,
                List.of(
                        new ProjectFeedNodeResponse(projectFeedNode1.getId(), feedNode1.getTitle(),
                                projectFeedNode1.getStartDate(),
                                projectFeedNode1.getEndDate(), projectFeedNode1.getMemoirCount()),
                        new ProjectFeedNodeResponse(projectFeedNode2.getId(), feedNode2.getTitle(),
                                projectFeedNode2.getStartDate(),
                                projectFeedNode2.getEndDate(), projectFeedNode2.getMemoirCount())
                )), Collections.emptyList(), Collections.emptyList());

        //when
        final MemberProjectResponse response = projectReadService.findMemberProject("test1@email.com", 1L);

        //then
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void 종료된_사용자_단일_프로젝트를_조회시_전체_회고글을_대상으로_반환한다() throws MalformedURLException {
        // given
        final Member member = 사용자를_생성한다(1L);
        final FeedNode feedNode1 = new FeedNode("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = new FeedNode("피드 2주차", "피드 2주차 내용");
        final FeedNode feedNode3 = new FeedNode("피드 3주차", "피드 3주차 내용");
        final FeedNode feedNode4 = new FeedNode("피드 4주차", "피드 4주차 내용");
        final FeedNodes feedNodes = new FeedNodes(
                List.of(feedNode1, feedNode2, feedNode3, feedNode4));
        final FeedContent feedContent = new FeedContent("피드 본문");
        final Feed feed = 피드를_생성한다(member);
        feedContent.addNodes(feedNodes);
        feed.addContent(feedContent);

        final ProjectFeedNode projectFeedNode1 = new ProjectFeedNode(
                new Period(TODAY, TODAY.plusDays(10)), 5, feedNode1);
        final ProjectFeedNode projectFeedNode2 = new ProjectFeedNode(
                new Period(TODAY.plusDays(11), TODAY.plusDays(20)), 5, feedNode2);
        final ProjectFeedNode projectFeedNode3 = new ProjectFeedNode(
                new Period(TODAY.plusDays(21), TODAY.plusDays(30)), 5, feedNode1);
        final ProjectFeedNode projectFeedNode4 = new ProjectFeedNode(
                new Period(TODAY.plusDays(31), TODAY.plusDays(40)), 5, feedNode2);

        final Project project = new Project(1L, new ProjectName("project"), new LimitedMemberCount(6),
                feedContent, member);
        project.start();
        project.complete();
        project.addAllProjectFeedNodes(
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)));
        project.addAllProjectMembers(
                List.of(new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, member)));

        final List<Memoir> memoirs = 회고글_목록을_생성한다(projectFeedNode1, member, project);
        given(projectRepository.findByIdWithContentAndTodos(anyLong()))
                .willReturn(Optional.of(project));
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));
        given(memoirRepository.findByProject(any()))
                .willReturn(memoirs);

        final MemberProjectResponse expected = new MemberProjectResponse(project.getName().getValue(),
                project.getStatus().name(), member.getId(), project.getCurrentMemberCount(),
                project.getLimitedMemberCount().getValue(), project.getStartDate(), project.getEndDate(),
                feedContent.getId(), new ProjectFeedNodesResponse(false, true,
                List.of(
                        new ProjectFeedNodeResponse(projectFeedNode1.getId(), feedNode1.getTitle(),
                                projectFeedNode1.getStartDate(),
                                projectFeedNode1.getEndDate(), projectFeedNode1.getMemoirCount()),
                        new ProjectFeedNodeResponse(projectFeedNode2.getId(), feedNode2.getTitle(),
                                projectFeedNode2.getStartDate(),
                                projectFeedNode2.getEndDate(), projectFeedNode2.getMemoirCount())
                )), Collections.emptyList(),
                List.of(
                        new MemoirResponse(1L, "회고글 내용", LocalDate.now()),
                        new MemoirResponse(2L, "회고글 내용", LocalDate.now()),
                        new MemoirResponse(3L, "회고글 내용", LocalDate.now()),
                        new MemoirResponse(4L, "회고글 내용", LocalDate.now())
                ));

        //when
        final MemberProjectResponse response = projectReadService.findMemberProject("test1@email.com", 1L);

        //then
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("memoirs.id", "memoirs.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 사용자_단일_목록_조회_시_유효하지_않은_프로젝트_아이디일_경우_예외를_반환한다() {
        //given
        when(projectRepository.findByIdWithContentAndTodos(anyLong()))
                .thenThrow(new NotFoundException("프로젝트 정보가 존재하지 않습니다. projectId = 1"));

        //when, then
        assertThatThrownBy(() -> projectReadService.findMemberProject("test1@email.com", 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("프로젝트 정보가 존재하지 않습니다. projectId = 1");

    }

    @Test
    void 사용자_단일_목록_조회_시_유효하지_않은_아이디일_경우_예외를_반환한다() {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(creator, targetFeedContent);

        when(projectRepository.findByIdWithContentAndTodos(anyLong()))
                .thenReturn(Optional.of(project));
        when(memberRepository.findByEmail(any()))
                .thenThrow(new NotFoundException("존재하지 않는 회원입니다."));

        // when, then
        assertThatThrownBy(() -> projectReadService.findMemberProject("test2@email.com", 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    void 사용자_단일_목록_조회_시_사용자가_참여하지_않은_프로젝트일_경우_예외를_반환한다() {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Member member = 사용자를_생성한다(2L);
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(creator, targetFeedContent);

        when(projectRepository.findByIdWithContentAndTodos(anyLong()))
                .thenReturn(Optional.of(project));
        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(member));

        // when, then
        assertThatThrownBy(() -> projectReadService.findMemberProject("test2@email.com", 1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("해당 프로젝트에 참여하지 않은 사용자입니다.");
    }

    @Test
    void 사용자_프로젝트_목록을_조회한다() throws MalformedURLException {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project1 = 프로젝트를_생성한다(creator, targetFeedContent);
        프로젝트를_생성한다(creator, targetFeedContent);
        final Project project3 = 프로젝트를_생성한다(creator, targetFeedContent);
        프로젝트를_생성한다(creator, targetFeedContent);

        final Member member = 사용자를_생성한다(2L);
        project1.join(member);
        project3.join(member);

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(member));
        when(projectRepository.findByMember(any()))
                .thenReturn(List.of(project1, project3));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final List<MemberProjectForListResponse> expected = List.of(
                new MemberProjectForListResponse(1L, "프로젝트", "RECRUITING", 2,
                        6, LocalDateTime.now(), TODAY,
                        THIRTY_DAY_LATER, new MemberResponse(creator.getId(), creator.getNickname().getValue(),
                        "http://example.com/serverFilePath", Position.BACKEND.name(),
                        List.of(new MemberSkillResponse(1L, "Java")))),
                new MemberProjectForListResponse(2L, "프로젝트", "RECRUITING", 2,
                        6, LocalDateTime.now(), TODAY, THIRTY_DAY_LATER,
                        new MemberResponse(creator.getId(), creator.getNickname().getValue(),
                                "http://example.com/serverFilePath", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java"))))
        );

        //when
        final List<MemberProjectForListResponse> response = projectReadService.findMemberProjects("test1@email.com");

        //then
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("projectId", "createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 사용자_프로젝트_목룩_조회_중_참여한_프로젝트가_없으면_빈_리스트를_반환한다() {
        // given
        final Member creator = 크리에이터를_생성한다();

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(creator));
        when(projectRepository.findByMember(any()))
                .thenReturn(Collections.emptyList());

        // when
        final List<MemberProjectForListResponse> response = projectReadService.findMemberProjects("test1@email.com");

        // then
        assertThat(response).isEmpty();
    }

    @Test
    void 사용자_프로젝트_목록_중_모집_중인_상태만_조회한다() throws MalformedURLException {
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project1 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project2 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project3 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project4 = 프로젝트를_생성한다(creator, targetFeedContent);

        final Member member = 사용자를_생성한다(2L);
        project1.join(member);
        project2.join(member);
        project3.join(member);
        project4.join(member);

        project3.start();
        project4.complete();

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(member));
        when(projectRepository.findByMemberAndStatus(any(), any()))
                .thenReturn(List.of(project1, project2));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final List<MemberProjectForListResponse> expected = List.of(
                new MemberProjectForListResponse(1L, "프로젝트", "RECRUITING", 2,
                        6, LocalDateTime.now(), TODAY, THIRTY_DAY_LATER,
                        new MemberResponse(creator.getId(), creator.getNickname().getValue(),
                                "http://example.com/serverFilePath", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java")))),
                new MemberProjectForListResponse(2L, "프로젝트", "RECRUITING", 2,
                        6, LocalDateTime.now(), TODAY, THIRTY_DAY_LATER,
                        new MemberResponse(creator.getId(), creator.getNickname().getValue(),
                                "http://example.com/serverFilePath", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java"))))
        );

        //when
        final List<MemberProjectForListResponse> response = projectReadService.findMemberProjectsByStatusType(
                "test2@email.com", ProjectStatusTypeRequest.RECRUITING);

        //then
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("projectId", "createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 사용자_프로젝트_목록_중_진행_중인_상태만_조회한다() throws MalformedURLException {
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project1 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project2 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project3 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project4 = 프로젝트를_생성한다(creator, targetFeedContent);

        final Member member = 사용자를_생성한다(2L);
        project1.join(member);
        project2.join(member);
        project3.join(member);
        project4.join(member);

        project3.start();
        project4.start();

        project3.addAllProjectMembers(List.of(
                new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project3, creator),
                new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project3, member)));
        project4.addAllProjectMembers(List.of(
                new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project3, creator),
                new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project3, member)));

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(member));
        when(projectRepository.findByMemberAndStatus(any(), any()))
                .thenReturn(List.of(project3, project4));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final List<MemberProjectForListResponse> expected = List.of(
                new MemberProjectForListResponse(3L, "프로젝트", "RUNNING", 2,
                        6, LocalDateTime.now(), TODAY, THIRTY_DAY_LATER,
                        new MemberResponse(creator.getId(), creator.getNickname().getValue(),
                                "http://example.com/serverFilePath", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java")))),
                new MemberProjectForListResponse(4L, "프로젝트", "RUNNING", 2,
                        6, LocalDateTime.now(), TODAY, THIRTY_DAY_LATER,
                        new MemberResponse(creator.getId(), creator.getNickname().getValue(),
                                "http://example.com/serverFilePath", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java"))))
        );

        //when
        final List<MemberProjectForListResponse> response = projectReadService.findMemberProjectsByStatusType(
                "test2@email.com", ProjectStatusTypeRequest.RUNNING);

        //then
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("projectId", "createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 사용자_프로젝트_목록_중_종료된_상태만_조회한다() throws MalformedURLException {
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project1 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project2 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project3 = 프로젝트를_생성한다(creator, targetFeedContent);
        final Project project4 = 프로젝트를_생성한다(creator, targetFeedContent);

        final Member member = 사용자를_생성한다(2L);
        project1.join(member);
        project2.join(member);
        project3.join(member);
        project4.join(member);

        project3.complete();
        project4.complete();

        project3.addAllProjectMembers(List.of(
                new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project3, creator),
                new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project3, member)));
        project4.addAllProjectMembers(List.of(
                new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project3, creator),
                new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project3, member)));

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(member));
        when(projectRepository.findByMemberAndStatus(any(), any()))
                .thenReturn(List.of(project3, project4));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final List<MemberProjectForListResponse> expected = List.of(
                new MemberProjectForListResponse(3L, "프로젝트", "COMPLETED", 2,
                        6, LocalDateTime.now(), TODAY, THIRTY_DAY_LATER,
                        new MemberResponse(creator.getId(), creator.getNickname().getValue(),
                                "http://example.com/serverFilePath", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java")))),
                new MemberProjectForListResponse(4L, "프로젝트", "COMPLETED", 2,
                        6, LocalDateTime.now(), TODAY, THIRTY_DAY_LATER,
                        new MemberResponse(creator.getId(), creator.getNickname().getValue(),
                                "http://example.com/serverFilePath", Position.BACKEND.name(),
                                List.of(new MemberSkillResponse(1L, "Java"))))
        );

        //when
        final List<MemberProjectForListResponse> response = projectReadService.findMemberProjectsByStatusType(
                "test2@email.com", ProjectStatusTypeRequest.COMPLETED);

        //then
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("projectId", "createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트의_전체_노드를_조회한다() throws MalformedURLException {
        // given
        final Member creator = 사용자를_생성한다(1L);
        final Feed feed = 피드를_생성한다(creator);
        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));

        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        when(projectMemberRepository.findProjectMember(anyLong(), any()))
                .thenReturn(Optional.of(projectMember));
        when(projectRepository.findByIdWithNodes(1L))
                .thenReturn(Optional.of(project));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        // when
        final List<ProjectFeedNodeDetailResponse> responses = projectReadService.findAllProjectNodes(1L,
                "test@email.com");
        final List<ProjectFeedNodeDetailResponse> expected = List.of(
                new ProjectFeedNodeDetailResponse(1L, "피드 1주차", "피드 1주차 내용",
                        List.of("http://example.com/serverFilePath", "http://example.com/serverFilePath"), TODAY,
                        TEN_DAY_LATER, 10),
                new ProjectFeedNodeDetailResponse(2L, "피드 2주차", "피드 2주차 내용",
                        Collections.emptyList(), TWENTY_DAY_LAYER, THIRTY_DAY_LATER, 2)
        );

        // then
        assertThat(responses)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트의_노드_조회시_프로젝트에_참여하지_않은_사용자면_예외가_발생한다() {
        // given
        final Member creator = 사용자를_생성한다(1L);
        final Feed feed = 피드를_생성한다(creator);
        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));

        when(projectRepository.findByIdWithNodes(1L))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findProjectMember(anyLong(), any()))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectReadService.findAllProjectNodes(1L, "test@email.com"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 프로젝트의_노드_조회시_존재하지_않는_프로젝트이면_예외가_발생한다() {
        // given
        when(projectRepository.findByIdWithNodes(1L))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectReadService.findAllProjectNodes(1L, "test@email.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 진행중인_프로젝트의_회고글을_전체_조회한다() throws MalformedURLException {
        // given
        final Member creator = 사용자를_생성한다(1L);
        final Member follower = 사용자를_생성한다(2L);

        final Feed feed = 피드를_생성한다(creator);

        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));
        project.start();

        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                follower);
        final ProjectFeedNode projectFeedNode = project.getProjectFeedNodes().getValues().get(0);

        final Memoir memoir1 = 회고글을_생성한다("description1", projectFeedNode, projectMember1);
        final Memoir memoir2 = 회고글을_생성한다("description2", projectFeedNode, projectMember1);
        final Memoir memoir3 = 회고글을_생성한다("description3", projectFeedNode, projectMember2);

        given(projectRepository.findByIdWithNodes(anyLong()))
                .willReturn(Optional.of(project));
        given(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .willReturn(Optional.of(projectMember1));
        given(memoirRepository.findByRunningProjectFeedNodeWithMemberAndMemberImage(any()))
                .willReturn(List.of(memoir3, memoir2, memoir1));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        // when
        final List<ProjectMemoirResponse> responses = projectReadService.findProjectMemoirs("test@email.com", 1L);

        // then
        final ProjectMemoirResponse projectMemoirResponse1 = new ProjectMemoirResponse(
                new MemberResponse(1L, "name1", "http://example.com/serverFilePath", Position.FRONTEND.name(),
                        List.of(new MemberSkillResponse(1L, "HTML"))),
                new MemoirResponse(1L, "description1", LocalDate.now()));
        final ProjectMemoirResponse projectMemoirResponse2 = new ProjectMemoirResponse(
                new MemberResponse(1L, "name1", "http://example.com/serverFilePath", Position.FRONTEND.name(),
                        List.of(new MemberSkillResponse(1L, "HTML"))),
                new MemoirResponse(2L, "description2", LocalDate.now()));
        final ProjectMemoirResponse projectMemoirResponse3 = new ProjectMemoirResponse(
                new MemberResponse(2L, "name1", "http://example.com/serverFilePath", Position.FRONTEND.name(),
                        List.of(new MemberSkillResponse(1L, "HTML"))),
                new MemoirResponse(3L, "description3", LocalDate.now()));
        final List<ProjectMemoirResponse> expected = List.of(projectMemoirResponse3,
                projectMemoirResponse2, projectMemoirResponse1);

        assertThat(responses).usingRecursiveComparison()
                .ignoringFields("memoir.id", "memoir.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 모집중인_프로젝트의_회고글을_조회시_빈_값을_반환한다() {
        // given
        final Member creator = 사용자를_생성한다(1L);

        final Feed feed = 피드를_생성한다(creator);

        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));
        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);

        given(projectRepository.findByIdWithNodes(anyLong()))
                .willReturn(Optional.of(project));
        given(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .willReturn(Optional.of(projectMember));

        // when
        final List<ProjectMemoirResponse> responses = projectReadService.findProjectMemoirs("test@email.com", 1L);

        // then
        final List<ProjectMemoirResponse> expected = Collections.emptyList();

        assertThat(responses).isEqualTo(expected);
    }

    @Test
    void 종료된_프로젝트의_회고글을_전체_조회시_모든_기간의_회고글을_대상으로_반환한다() throws MalformedURLException {
        // given
        final Member creator = 사용자를_생성한다(1L);
        final Member follower = 사용자를_생성한다(2L);

        final Feed feed = 피드를_생성한다(creator);

        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));
        project.complete();
        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                follower);
        final ProjectFeedNode projectFeedNode = project.getProjectFeedNodes().getValues().get(0);

        final Memoir memoir1 = 회고글을_생성한다("description1", projectFeedNode, projectMember1);
        final Memoir memoir2 = 회고글을_생성한다("description2", projectFeedNode, projectMember1);
        final Memoir memoir3 = 회고글을_생성한다("description3", projectFeedNode, projectMember2);

        given(projectRepository.findByIdWithNodes(anyLong()))
                .willReturn(Optional.of(project));
        given(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .willReturn(Optional.of(projectMember1));
        given(memoirRepository.findByProjectWithMemberAndMemberImage(any()))
                .willReturn(List.of(memoir3, memoir2, memoir1));
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        // when
        final List<ProjectMemoirResponse> responses = projectReadService.findProjectMemoirs("test@email.com", 1L);

        // then
        final ProjectMemoirResponse projectMemoirResponse1 = new ProjectMemoirResponse(
                new MemberResponse(1L, "name1", "http://example.com/serverFilePath",
                        Position.FRONTEND.name(), List.of(new MemberSkillResponse(1L, "HTML"))),
                new MemoirResponse(1L, "description1", LocalDate.now()));
        final ProjectMemoirResponse projectMemoirResponse2 = new ProjectMemoirResponse(
                new MemberResponse(1L, "name1", "http://example.com/serverFilePath",
                        Position.FRONTEND.name(), List.of(new MemberSkillResponse(1L, "HTML"))),
                new MemoirResponse(2L, "description2", LocalDate.now()));
        final ProjectMemoirResponse projectMemoirResponse3 = new ProjectMemoirResponse(
                new MemberResponse(2L, "name1", "http://example.com/serverFilePath",
                        Position.FRONTEND.name(), List.of(new MemberSkillResponse(1L, "HTML"))),
                new MemoirResponse(3L, "description3", LocalDate.now()));
        final List<ProjectMemoirResponse> expected = List.of(projectMemoirResponse3,
                projectMemoirResponse2, projectMemoirResponse1);

        assertThat(responses).usingRecursiveComparison()
                .ignoringFields("memoir.id", "memoir.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트의_회고글을_전체_조회시_현재_진행중인_노드가_없으면_빈_리스트를_반환한다() {
        // given
        final Member creator = 사용자를_생성한다(1L);
        final Member follower = 사용자를_생성한다(2L);

        final Feed feed = 피드를_생성한다(creator);

        final Project project = 진행중인_노드가_없는_프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));
        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                follower);
        final ProjectFeedNode projectFeedNode = project.getProjectFeedNodes().getValues().get(0);

        회고글을_생성한다("description1", projectFeedNode, projectMember1);
        회고글을_생성한다("description2", projectFeedNode, projectMember1);
        회고글을_생성한다("description3", projectFeedNode, projectMember2);

        given(projectRepository.findByIdWithNodes(anyLong()))
                .willReturn(Optional.of(project));
        given(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .willReturn(Optional.of(projectMember1));

        // when
        final List<ProjectMemoirResponse> responses = projectReadService.findProjectMemoirs("test@email.com", 1L);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    void 프로젝트의_회고글을_전체_조회할_때_존재하지_않는_프로젝트이면_예외가_발생한다() {
        // given
        given(projectRepository.findByIdWithNodes(anyLong()))
                .willThrow(new NotFoundException("존재하지 않는 프로젝트입니다. projectId = 1"));

        // when
        // then
        assertThatThrownBy(() -> projectReadService.findProjectMemoirs("test@email.com", 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트의_회고글을_전체_조회할_때_프로젝트에_참여하지_않은_회원이면_예외가_발생한다() {
        // given
        final Member creator = 사용자를_생성한다(1L);

        final Feed feed = 피드를_생성한다(creator);

        final Project project = 프로젝트를_생성한다(creator, feed.getContents().getValues().get(0));

        given(projectRepository.findByIdWithNodes(anyLong()))
                .willReturn(Optional.of(project));
        given(projectRepository.findByIdWithNodes(anyLong()))
                .willThrow(new ForbiddenException("프로젝트에 참여하지 않은 회원입니다."));

        // when
        // then
        assertThatThrownBy(() -> projectReadService.findProjectMemoirs("test@email.com", 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    private Member 크리에이터를_생성한다() {
        final MemberImage memberImage = new MemberImage("originalFileName", "default-member-image",
                ImageContentType.JPG);
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        return new Member(1L, new Email("test@email.com"), null, new EncryptedPassword(new Password("password1!")),
                new Nickname("사이드페어"), memberImage, memberProfile, skills);
    }

    private Member 사용자를_생성한다(final Long id) {
        return new Member(id, new Email("test1@email.com"),
                null, new EncryptedPassword(new Password("password1")), new Nickname("name1"),
                new MemberImage("originalFileName", "serverFilePath", ImageContentType.JPEG),
                new MemberProfile(Position.FRONTEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("HTML")))));
    }

    private Feed 피드를_생성한다(final Member creator) {
        final FeedCategory category = new FeedCategory("게임");
        final List<FeedNode> feedNodes = 피드_노드들을_생성한다();
        final FeedContent feedContent = 피드_본문을_생성한다(feedNodes);
        final Feed feed = new Feed("피드 제목", "피드 소개글", 10, creator, category);
        feed.addContent(feedContent);
        return feed;
    }

    private List<FeedNode> 피드_노드들을_생성한다() {
        final FeedNode feedNode1 = new FeedNode("피드 1주차", "피드 1주차 내용");
        feedNode1.addImages(new FeedNodeImages(노드_이미지들을_생성한다()));
        final FeedNode feedNode2 = new FeedNode("피드 2주차", "피드 2주차 내용");
        return List.of(feedNode1, feedNode2);
    }

    private FeedContent 피드_본문을_생성한다(final List<FeedNode> feedNodes) {
        final FeedContent feedContent = new FeedContent("피드 본문");
        feedContent.addNodes(new FeedNodes(feedNodes));
        return feedContent;
    }

    private List<FeedNodeImage> 노드_이미지들을_생성한다() {
        return List.of(
                new FeedNodeImage("node-image1.png", "node-image1-save-path", ImageContentType.PNG),
                new FeedNodeImage("node-image2.png", "node-image2-save-path", ImageContentType.PNG)
        );
    }

    private Project 프로젝트를_생성한다(final Member member, final FeedContent feedContent) {
        final Project project = new Project(new ProjectName("프로젝트"), new LimitedMemberCount(6),
                feedContent, member);
        final List<FeedNode> feedNodes = feedContent.getNodes().getValues();

        final FeedNode firstFeedNode = feedNodes.get(0);
        final ProjectFeedNode firstProjectFeedNode = new ProjectFeedNode(
                1L, new Period(TODAY, TEN_DAY_LATER), 10, firstFeedNode);

        final FeedNode secondFeedNode = feedNodes.get(1);
        final ProjectFeedNode secondProjectFeedNode = new ProjectFeedNode(
                2L, new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER), 2, secondFeedNode);

        final ProjectFeedNodes projectFeedNodes = new ProjectFeedNodes(
                List.of(firstProjectFeedNode, secondProjectFeedNode));
        project.addAllProjectFeedNodes(projectFeedNodes);
        return project;
    }

    private Project 진행중인_노드가_없는_프로젝트를_생성한다(final Member member, final FeedContent feedContent) {
        final Project project = new Project(new ProjectName("프로젝트"), new LimitedMemberCount(6),
                feedContent, member);
        final List<FeedNode> feedNodes = feedContent.getNodes().getValues();

        final FeedNode firstFeedNode = feedNodes.get(0);
        final ProjectFeedNode firstProjectFeedNode = new ProjectFeedNode(
                1L, new Period(TEN_DAY_LATER, TWENTY_DAY_LAYER), 10, firstFeedNode);

        final FeedNode secondFeedNode = feedNodes.get(1);
        final ProjectFeedNode secondProjectFeedNode = new ProjectFeedNode(
                2L, new Period(THIRTY_DAY_LATER, THIRTY_DAY_LATER.plusDays(10)), 2, secondFeedNode);

        final ProjectFeedNodes projectFeedNodes = new ProjectFeedNodes(
                List.of(firstProjectFeedNode, secondProjectFeedNode));
        project.addAllProjectFeedNodes(projectFeedNodes);
        return project;
    }

    private static ProjectResponse 예상하는_프로젝트_응답을_생성한다() {
        final List<ProjectFeedNodeResponse> projectNodeResponses = List.of(
                new ProjectFeedNodeResponse(1L, "피드 1주차", TODAY, TEN_DAY_LATER, 10),
                new ProjectFeedNodeResponse(2L, "피드 2주차", TWENTY_DAY_LAYER, THIRTY_DAY_LATER, 2));
        return new ProjectResponse("프로젝트", 1, 6, projectNodeResponses, 31);
    }

    private static ProjectCertifiedResponse 예상하는_로그인된_사용자의_프로젝트_응답을_생성한다(final Boolean isJoined,
                                                                         final int currentMemberCount) {
        final List<ProjectFeedNodeResponse> projectNodeResponses = List.of(
                new ProjectFeedNodeResponse(1L, "피드 1주차", TODAY, TEN_DAY_LATER, 10),
                new ProjectFeedNodeResponse(2L, "피드 2주차", TWENTY_DAY_LAYER, THIRTY_DAY_LATER, 2));
        return new ProjectCertifiedResponse("프로젝트", currentMemberCount, 6, projectNodeResponses, 31, isJoined);
    }

    private Memoir 회고글을_생성한다(final String description, final ProjectFeedNode projectFeedNode,
                             final ProjectMember projectMember) {
        return new Memoir(description, projectFeedNode, projectMember, LocalDateTime.now());
    }

    private List<Memoir> 회고글_목록을_생성한다(final ProjectFeedNode node, final Member member,
                                      final Project project) {
        return List.of(
                new Memoir("회고글 내용", node,
                        new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, member),
                        LocalDateTime.now()),
                new Memoir("회고글 내용", node,
                        new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, member),
                        LocalDateTime.now()),
                new Memoir("회고글 내용", node,
                        new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, member),
                        LocalDateTime.now()),
                new Memoir("회고글 내용", node,
                        new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, member),
                        LocalDateTime.now()),
                new Memoir("회고글 내용", node,
                        new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, member),
                        LocalDateTime.now())
        );
    }
}
