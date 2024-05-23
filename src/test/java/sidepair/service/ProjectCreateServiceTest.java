package sidepair.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sidepair.domain.project.ProjectStatus.RUNNING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
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
import sidepair.domain.feed.FeedStatus;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.MemberProfile;
import sidepair.domain.member.MemberSkill;
import sidepair.domain.member.MemberSkills;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.domain.member.vo.SkillName;
import sidepair.domain.project.Memoir;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.domain.project.ProjectFeedNodes;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectRole;
import sidepair.domain.project.ProjectToDo;
import sidepair.domain.project.ProjectToDoCheck;
import sidepair.domain.project.exeption.ProjectException;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectName;
import sidepair.domain.project.vo.ProjectTodoContent;
import sidepair.persistence.feed.FeedApplicantRepository;
import sidepair.persistence.feed.FeedContentRepository;
import sidepair.persistence.member.MemberRepository;
import sidepair.persistence.project.MemoirRepository;
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.persistence.project.ProjectToDoCheckRepository;
import sidepair.service.dto.project.request.MemoirRequest;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;
import sidepair.service.dto.project.request.ProjectTodoRequest;
import sidepair.service.dto.project.response.ProjectToDoCheckResponse;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.NotFoundException;
import sidepair.service.project.ProjectCreateService;

@ExtendWith(MockitoExtension.class)
class ProjectCreateServiceTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);
    private static final LocalDate TWENTY_DAY_LATER = TODAY.plusDays(20);

    private static final FeedNode FEED_NODE = new FeedNode(1L, "title", "content");
    private static final FeedContent FEED_CONTENT = new FeedContent(1L, "content");
    private static final FeedContent DELETED_FEED_CONTENT = new FeedContent(2L, "content2");
    private static final FeedNodes FEED_CONTENTS = new FeedNodes(new ArrayList<>(List.of(FEED_NODE)));

    private static final Member MEMBER = new Member(new Email("test2@email.com"),
            new EncryptedPassword(new Password("password!2")),
            new Nickname("name2"), null,
            new MemberProfile(Position.BACKEND),
            new MemberSkills(
                    List.of(new MemberSkill(1L, new SkillName("Java")))));

    private static final Feed FEED = new Feed("feed", "introduction", 30,
            MEMBER, new FeedCategory("이커머스"));

    private static final Feed DELETED_FEED = new Feed("feed", "introduction", 30,
            FeedStatus.DELETED, MEMBER, new FeedCategory("이커머스"));

    private static Member member;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private FeedContentRepository feedContentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProjectToDoCheckRepository projectToDoCheckRepository;

    @Mock
    private FeedApplicantRepository feedApplicantRepository;

    @Mock
    private MemoirRepository memoirRepository;

    @InjectMocks
    private ProjectCreateService projectCreateService;

    @BeforeAll
    static void setUp() {
        FEED_CONTENT.addNodes(FEED_CONTENTS);
        FEED.addContent(FEED_CONTENT);
        DELETED_FEED.addContent(DELETED_FEED_CONTENT);
        final Email email = new Email("test@email.com");
        final Password password = new Password("password1!");
        final EncryptedPassword encryptedPassword = new EncryptedPassword(password);
        final Nickname nickname = new Nickname("nickname");
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(new SkillName("Java"))));
        member = new Member(email, encryptedPassword, nickname, null, memberProfile, skills);
    }

    @Test
    void 정상적으로_프로젝트를_생성한다() {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                6, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));

        given(feedContentRepository.findByIdWithFeed(anyLong()))
                .willReturn(Optional.of(FEED_CONTENT));
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));
        given(projectRepository.save(any()))
                .willReturn(new Project(1L, null, null, null, null));

        //when
        assertDoesNotThrow(() -> projectCreateService.create(request, member.getEmail().getValue()));
    }

    @Test
    void 프로젝트_생성_시_삭제된_피드이면_예외를_던진다() {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                6, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));

        given(feedContentRepository.findByIdWithFeed(anyLong()))
                .willReturn(Optional.of(DELETED_FEED_CONTENT));

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.create(request, member.getEmail().getValue()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 프로젝트_생성_시_존재하지_않은_피드_컨텐츠가_들어올때_예외를_던진다() {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                6, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));

        given(feedContentRepository.findByIdWithFeed(anyLong()))
                .willReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.create(request, member.getEmail().getValue()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트_생성_시_피드_컨텐츠의_노드사이즈와_요청의_노드사이즈가_다를때_예외를_던진다() {
        //given
        final List<ProjectFeedNodeRequest> wrongSizeProjectFeedNodeRequest = new ArrayList<>(List.of(
                new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER),
                new ProjectFeedNodeRequest(2L, 10, TODAY, TEN_DAY_LATER)));
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                6, wrongSizeProjectFeedNodeRequest);

        given(feedContentRepository.findByIdWithFeed(anyLong()))
                .willReturn(Optional.of(FEED_CONTENT));

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.create(request, member.getEmail().getValue()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 프로젝트_생성_시_피드에_존재하지_않는_노드가_요청으로_들어올때_예외를_던진다() {
        //given
        final long wrongFeedNodId = 2L;
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                6,
                new ArrayList<>(List.of(new ProjectFeedNodeRequest(wrongFeedNodId, 10, TODAY, TEN_DAY_LATER))));

        given(feedContentRepository.findByIdWithFeed(anyLong()))
                .willReturn(Optional.of(FEED_CONTENT));

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.create(request, member.getEmail().getValue()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트_생성_시_존재하지_않은_회원의_Email이_들어올때_예외를_던진다() {
        //given
        final ProjectCreateRequest request = new ProjectCreateRequest(1L, "name",
                6, new ArrayList<>(List.of(new ProjectFeedNodeRequest(1L, 10, TODAY, TEN_DAY_LATER))));

        given(feedContentRepository.findByIdWithFeed(anyLong()))
                .willReturn(Optional.of(FEED_CONTENT));
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.create(request, member.getEmail().getValue()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 정상적으로_프로젝트에_투두리스트를_추가한다() {
        //given
        final Member creator = 사용자를_생성한다(1L, "test1@email.com", "password1!", "생성자");
        final Feed feed = 피드를_생성한다(creator);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final int limitedMemberCount = 6;
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, limitedMemberCount);

        project.addProjectTodo(
                new ProjectToDo(new ProjectTodoContent("projectTodoContent"), new Period(TODAY, TEN_DAY_LATER)));

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(creator));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));

        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("projectContent", TODAY,
                TEN_DAY_LATER);

        //when
        //then
        assertDoesNotThrow(() -> projectCreateService.addProjectTodo(1L, "test1@email.com", projectTodoRequest));
    }

    @Test
    void 프로젝트에_투두리스트_추가시_회원을_찾지_못할_경우_예외를_던진다() {
        //given
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.empty());

        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("projectContent", TODAY,
                TEN_DAY_LATER);

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.addProjectTodo(1L, "test1@email.com", projectTodoRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트에_투두리스트_추가시_프로젝트를_찾지_못할_경우_예외를_던진다() {
        //given
        final Member creator = 사용자를_생성한다(1L, "test1@email.com", "password1!", "생성자");
        final Feed feed = 피드를_생성한다(creator);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final int limitedMemberCount = 6;
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, limitedMemberCount);

        project.addProjectTodo(
                new ProjectToDo(new ProjectTodoContent("projectTodoContent"), new Period(TODAY, TEN_DAY_LATER)));

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(creator));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("projectContent", TODAY,
                TEN_DAY_LATER);

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.addProjectTodo(1L, "test1@email.com", projectTodoRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트에_투두리스트_추가시_종료된_프로젝트일_경우_예외를_던진다() {
        //given
        final Member creator = 사용자를_생성한다(1L, "test1@email.com", "password1!", "생성자");
        final Feed feed = 피드를_생성한다(creator);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final int limitedMemberCount = 6;
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, limitedMemberCount);

        project.addProjectTodo(
                new ProjectToDo(new ProjectTodoContent("projectTodoContent"), new Period(TODAY, TEN_DAY_LATER)));
        project.complete();

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(creator));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));

        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("projectContent", TODAY,
                TEN_DAY_LATER);

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.addProjectTodo(1L, "test1@email.com", projectTodoRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 프로젝트에_투두리스트_추가시_리더가_아닐_경우_예외를_던진다() {
        //given
        final Member creator = 사용자를_생성한다(1L, "test1@email.com", "password1!", "생성자");
        final Feed feed = 피드를_생성한다(creator);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final int limitedMemberCount = 6;
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, limitedMemberCount);

        project.addProjectTodo(
                new ProjectToDo(new ProjectTodoContent("projectTodoContent"), new Period(TODAY, TEN_DAY_LATER)));

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));

        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest("projectContent", TODAY,
                TEN_DAY_LATER);

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.addProjectTodo(1L, "test2@email.com", projectTodoRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 프로젝트에_투두리스트_추가시_프로젝트_컨텐츠가_250글자가_넘을때_예외를_던진다() {
        //given
        final Member creator = 사용자를_생성한다(1L, "test1@email.com", "password1!", "생성자");
        final Feed feed = 피드를_생성한다(creator);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final int limitedMemberCount = 6;
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, limitedMemberCount);

        project.addProjectTodo(
                new ProjectToDo(new ProjectTodoContent("projectTodoContent"), new Period(TODAY, TEN_DAY_LATER)));

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(creator));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));

        final String projectTodoContent = "a".repeat(251);
        final ProjectTodoRequest projectTodoRequest = new ProjectTodoRequest(projectTodoContent, TODAY,
                TEN_DAY_LATER);

        //when
        //then
        assertThatThrownBy(() -> projectCreateService.addProjectTodo(1L, "test1@email.com", projectTodoRequest))
                .isInstanceOf(ProjectException.class);
    }

    @Test
    void 프로젝트를_시작한다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(creator));
        when(projectRepository.findById(any()))
                .thenReturn(Optional.of(project));

        // when
        projectCreateService.startProject("test@email.com", 1L);

        // then
        assertThat(project.getStatus()).isEqualTo(RUNNING);
    }

    @Test
    void 프로젝트_시작시_존재하지_않는_사용자면_예외가_발생한다() {
        // given
        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectCreateService.startProject("test@email.com", 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트_시작시_존재하지_않는_프로젝트이면_예외가_발생한다() {
        // given
        final Member member = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(member));
        when(projectRepository.findById(any()))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectCreateService.startProject("test@email.com", 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트를_시작하는_사용자가_프로젝트의_리더가_아니면_예외가_발생한다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Member follower = 사용자를_생성한다(2L, "test1@email.com", "password2!", "사이드");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(follower));
        when(projectRepository.findById(any()))
                .thenReturn(Optional.of(project));

        // expected
        assertThatThrownBy(() -> projectCreateService.startProject("test@email.com", 1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 프로젝트_시작시_시작날짜가_아직_지나지_않았으면_예외가_발생한다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 시작_날짜가_미래인_프로젝트를_생성한다(1L, creator, targetFeedContent, 6);

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(creator));
        when(projectRepository.findById(any()))
                .thenReturn(Optional.of(project));

        // expected
        assertThatThrownBy(() -> projectCreateService.startProject("test@email.com", 1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 회고_등록을_요청한다() {
        // given
        final MemoirRequest request = 회고_요청_DTO를_생성한다();

        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        final ProjectMember projectLeader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        projectMemberRepository.save(projectLeader);
        final ProjectFeedNode projectFeedNode = project.getProjectFeedNodes().getValues().get(0);
        final Memoir memoir = 회고를_생성한다(projectFeedNode, projectLeader);

        when(projectRepository.findById(anyLong()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.of(projectLeader));
        when(memoirRepository.findByProjectMemberAndDateTime(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(memoirRepository.countByProjectMemberAndProjectFeedNode(any(), any()))
                .thenReturn(0);
        when(memoirRepository.save(any()))
                .thenReturn(memoir);

        // when
        final Long response = projectCreateService.createMemoir("test@email.com", 1L, request);

        // then
        assertAll(
                () -> assertThat(projectLeader.getParticipationRate()).isEqualTo(100 / (double) 10)
        );
    }

    @Test
    void 회고_등록시_노드_기간에_해당하지_않으면_예외가_발생한다() {
        // given
        final MemoirRequest request = 회고_요청_DTO를_생성한다();

        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 시작_날짜가_미래인_프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        final ProjectMember projectLeader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        projectMemberRepository.save(projectLeader);

        when(projectRepository.findById(anyLong()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.of(projectLeader));

        // expected
        assertThatThrownBy(
                () -> projectCreateService.createMemoir("test@email.com", 1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("회고는 노드 기간 내에만 작성할 수 있습니다.");
    }

    @Test
    void 하루에_두_번_이상_회고_등록_요청_시_예외를_반환한다() {
        // given
        final MemoirRequest request = 회고_요청_DTO를_생성한다();

        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        final ProjectMember projectLeader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        projectMemberRepository.save(projectLeader);
        final ProjectFeedNode projectFeedNode = project.getProjectFeedNodes().getValues().get(0);
        final Memoir memoir = 회고를_생성한다(projectFeedNode, projectLeader);

        when(projectRepository.findById(any()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.of(projectLeader));
        when(memoirRepository.findByProjectMemberAndDateTime(any(), any(), any()))
                .thenReturn(Optional.of(memoir));

        //expect
        assertThatThrownBy(
                () -> projectCreateService.createMemoir("test@email.com", 1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 오늘 회고를 등록하였습니다.");
    }

    @Test
    void 프로젝트_노드에서_허가된_인증_횟수보다_많은_회고_등록_요청_시_예외를_반환한다() {
        // given
        final MemoirRequest request = 회고_요청_DTO를_생성한다();

        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        final ProjectMember projectLeader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        projectMemberRepository.save(projectLeader);
        final ProjectFeedNode projectFeedNode = project.getProjectFeedNodes().getValues().get(0);

        when(projectRepository.findById(any()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.of(projectLeader));
        when(memoirRepository.countByProjectMemberAndProjectFeedNode(any(), any()))
                .thenReturn(projectFeedNode.getMemoirCount());

        //expect
        assertThatThrownBy(
                () -> projectCreateService.createMemoir("test@email.com", 1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이번 노드에는 최대 " + projectFeedNode.getMemoirCount() + "번만 회고를 등록할 수 있습니다.");
    }

    @Test
    void 회고_등록_요청_시_존재하지_않는_프로젝트이라면_예외를_반환한다() {
        // given
        final MemoirRequest request = 회고_요청_DTO를_생성한다();

        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        final ProjectMember projectLeader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        projectMemberRepository.save(projectLeader);

        when(projectRepository.findById(any()))
                .thenReturn(Optional.empty());

        //expect
        assertThatThrownBy(
                () -> projectCreateService.createMemoir("test@email.com", 1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 프로젝트입니다. projectId = 1");
    }

    @Test
    void 회고_등록_요청_시_사용자가_참여하지_않은_프로젝트이라면_예외를_반환한다() {
        // given
        final MemoirRequest request = 회고_요청_DTO를_생성한다();

        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);

        final ProjectMember projectLeader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        projectMemberRepository.save(projectLeader);

        when(projectRepository.findById(any()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.empty());

        //expect
        assertThatThrownBy(
                () -> projectCreateService.createMemoir("test@email.com", 1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("프로젝트에 해당 사용자가 존재하지 않습니다. 사용자 아이디 = " + "test@email.com");
    }

    @Test
    void 투두리스트를_체크한다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        project.addProjectTodo(new ProjectToDo(
                1L, new ProjectTodoContent("투두 1"), new Period(TODAY, TODAY.plusDays(3))
        ));
        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);

        when(projectRepository.findByIdWithTodos(anyLong()))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.of(projectMember));

        when(projectToDoCheckRepository.findByProjectIdAndTodoAndMemberEmail(any(), any(), any()))
                .thenReturn(Optional.empty());

        // when
        final ProjectToDoCheckResponse checkResponse = projectCreateService.checkProjectTodo(1L, 1L, "test@email.com");

        // then
        assertThat(checkResponse)
                .isEqualTo(new ProjectToDoCheckResponse(true));
    }

    @Test
    void 투두리스트_체크시_체크_이력이_있으면_제거한다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        final ProjectToDo projectToDo = new ProjectToDo(
                1L, new ProjectTodoContent("투두 1"), new Period(TODAY, TODAY.plusDays(3)));
        project.addProjectTodo(projectToDo);

        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project,
                creator);
        final ProjectToDoCheck projectToDoCheck = new ProjectToDoCheck(projectMember, projectToDo);

        when(projectRepository.findByIdWithTodos(anyLong()))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.of(projectMember));

        when(projectToDoCheckRepository.findByProjectIdAndTodoAndMemberEmail(any(), any(), any()))
                .thenReturn(Optional.of(projectToDoCheck));

        // when
        final ProjectToDoCheckResponse checkResponse = projectCreateService.checkProjectTodo(1L, 1L, "test@email.com");

        // then
        assertThat(checkResponse)
                .isEqualTo(new ProjectToDoCheckResponse(false));
    }

    @Test
    void 투두리스트_체크시_프로젝트이_존재하지_않으면_예외가_발생한다() {
        // given
        when(projectRepository.findByIdWithTodos(anyLong()))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectCreateService.checkProjectTodo(1L, 1L, "test@email.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("프로젝트가 존재하지 않습니다. projectId = 1");
    }

    @Test
    void 투두리스트_체크시_해당_투두가_존재하지_않으면_예외가_발생한다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        project.addProjectTodo(new ProjectToDo(
                1L, new ProjectTodoContent("투두 1"), new Period(TODAY, TODAY.plusDays(3))));

        when(projectRepository.findByIdWithTodos(anyLong()))
                .thenReturn(Optional.of(project));

        // expected
        assertThatThrownBy(() -> projectCreateService.checkProjectTodo(1L, 2L, "test@email.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 투두입니다. todoId = 2");
    }

    @Test
    void 투두리스트_체크시_프로젝트에_사용자가_없으면_예외가_발생한다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        project.addProjectTodo(new ProjectToDo(
                1L, new ProjectTodoContent("투두 1"), new Period(TODAY, TODAY.plusDays(3))));

        when(projectRepository.findByIdWithTodos(anyLong()))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectAndMemberEmail(any(), any()))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> projectCreateService.checkProjectTodo(1L, 1L, "test@email.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("프로젝트에 회원이 존재하지 않습니다. projectId = 1 memberEmail = test@email.com");
    }

    @Test
    void 프로젝트를_나간다() {
        // given
        final Project project = new Project(1L, new ProjectName("프로젝트"), new LimitedMemberCount(3),
                new FeedContent("content"), MEMBER);

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(MEMBER));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));

        // when
        // then
        assertDoesNotThrow(() -> projectCreateService.leave("test2@email.com", 1L));

    }

    @Test
    void 프로젝트를_나갈때_존재하지_않는_회원일_경우_예외가_발생한다() {
        // given
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> projectCreateService.leave("test2@email.com", 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트를_나갈때_존재하지_않는_프로젝트일_경우_예외가_발생한다() {
        // given
        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> projectCreateService.leave("test2@email.com", 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트를_나갈때_프로젝트이_진행중이면_예외가_발생한다() {
        // given
        final Project project = new Project(1L, new ProjectName("프로젝트"), new LimitedMemberCount(3),
                new FeedContent("content"), MEMBER);

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));

        // when
        project.start();

        // then
        assertThatThrownBy(() -> projectCreateService.leave("test2@email.com", 1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 프로젝트를_나갈때_프로젝트에_남아있는_사용자가_없으면_프로젝트이_삭제된다() {
        // given
        final Project project = new Project(1L, new ProjectName("프로젝트"), new LimitedMemberCount(3),
                new FeedContent("content"), MEMBER);

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(member));
        given(projectRepository.findById(anyLong()))
                .willReturn(Optional.of(project));

        // when
        projectCreateService.leave("test2@email.com", 1L);

        // then
        verify(projectRepository, times(1)).delete(project);
    }

    private Member 사용자를_생성한다(final Long memberId, final String email, final String password, final String nickname) {
        final MemberProfile memberProfile = new MemberProfile(Position.FRONTEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(new SkillName("CSS"))));

        return new Member(memberId, new Email(email), null, new EncryptedPassword(new Password(password)),
                new Nickname(nickname), null, memberProfile, skills);
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

    private Project 프로젝트를_생성한다(final Long projectId, final Member creator, final FeedContent feedContent,
                               final Integer limitedMemberCount) {
        final Project project = new Project(projectId, new ProjectName("프로젝트 이름"),
                new LimitedMemberCount(limitedMemberCount), feedContent, creator);
        project.addAllProjectFeedNodes(프로젝트_피드_노드들을_생성한다(feedContent.getNodes()));
        return project;
    }

    private Project 시작_날짜가_미래인_프로젝트를_생성한다(final Long projectId, final Member creator,
                                          final FeedContent feedContent, final Integer limitedMemberCount) {
        final Project project = new Project(projectId, new ProjectName("프로젝트 이름"),
                new LimitedMemberCount(limitedMemberCount), feedContent, creator);
        final ProjectFeedNode projectFeedNode = new ProjectFeedNode(
                new Period(TEN_DAY_LATER, TWENTY_DAY_LATER), 5, feedContent.getNodes().getValues().get(0));
        project.addAllProjectFeedNodes(
                new ProjectFeedNodes(List.of(projectFeedNode)));
        return project;
    }

    private ProjectFeedNodes 프로젝트_피드_노드들을_생성한다(final FeedNodes feedNodes) {
        return new ProjectFeedNodes(List.of(
                new ProjectFeedNode(new Period(TODAY, TEN_DAY_LATER), 5, feedNodes.getValues().get(0)),
                new ProjectFeedNode(new Period(TEN_DAY_LATER.plusDays(1), TWENTY_DAY_LATER), 5,
                        feedNodes.getValues().get(1)))
        );
    }

    private MemoirRequest 회고_요청_DTO를_생성한다() {
        return new MemoirRequest(
                "회고글 설명");
    }

    private Memoir 회고를_생성한다(final ProjectFeedNode projectFeedNode, final ProjectMember joinedMember) {
        return new Memoir("회고글 설명", projectFeedNode, joinedMember);
    }
}
