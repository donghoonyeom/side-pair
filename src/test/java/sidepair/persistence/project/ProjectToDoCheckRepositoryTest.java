package sidepair.persistence.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.feed.FeedNodes;
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
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.domain.project.ProjectFeedNodes;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectRole;
import sidepair.domain.project.ProjectToDo;
import sidepair.domain.project.ProjectToDoCheck;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectName;
import sidepair.domain.project.vo.ProjectTodoContent;
import sidepair.persistence.feed.FeedCategoryRepository;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.helper.RepositoryTest;
import sidepair.persistence.member.MemberRepository;

@RepositoryTest
class ProjectToDoCheckRepositoryTest {
    private static final LocalDate TODAY = LocalDate.now();

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final ProjectToDoCheckRepository projectToDoCheckRepository;

    public ProjectToDoCheckRepositoryTest(final MemberRepository memberRepository,
                                           final FeedRepository feedRepository,
                                           final ProjectRepository projectRepository,
                                           final ProjectMemberRepository projectMemberRepository,
                                           final FeedCategoryRepository feedCategoryRepository,
                                           final ProjectToDoCheckRepository projectToDoCheckRepository) {
        this.memberRepository = memberRepository;
        this.feedRepository = feedRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.feedCategoryRepository = feedCategoryRepository;
        this.projectToDoCheckRepository = projectToDoCheckRepository;
    }

    @Test
    void 프로젝트_아이디와_투두_아이디와_사용자_아이디로_프로젝트_투두_체크_현황을_반환한다() {
        // given
        final Member creator = 사용자를_생성한다("name1", "test1@example.com",  "password!1");
        final FeedCategory category = 카테고리를_저장한다("헬스케어");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = 피드_노드를_생성한다("피드 2주차", "피드 2주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1, feedNode2));
        피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(11), TODAY.plusDays(20),
                feedNode2);
        final Member projectPendingMember = 사용자를_생성한다("name2", "test2@example.com", "password!2");
        final Project project = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)), projectPendingMember);

        final LocalDate today = LocalDate.now();
        final LocalDate threeDaysAfter = today.plusDays(3);
        final ProjectToDo firstProjectTodo = new ProjectToDo(
                new ProjectTodoContent("투두1"), new Period(today, threeDaysAfter));
        final ProjectToDo secondProjectTodo = new ProjectToDo(
                new ProjectTodoContent("투두2"), new Period(today, threeDaysAfter));
        project.addProjectTodo(firstProjectTodo);
        project.addProjectTodo(secondProjectTodo);
        projectRepository.save(project);

        final ProjectMember projectMember = new ProjectMember(
                ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        projectMemberRepository.save(projectMember);

        final ProjectToDoCheck firstProjectToDoCheck = new ProjectToDoCheck(projectMember, firstProjectTodo);
        final ProjectToDoCheck secondProjectToDoCheck = new ProjectToDoCheck(projectMember, secondProjectTodo);
        projectToDoCheckRepository.saveAll(List.of(firstProjectToDoCheck, secondProjectToDoCheck));

        // when
        final ProjectToDoCheck findProjectTodoCheck = projectToDoCheckRepository.findByProjectIdAndTodoAndMemberEmail(
                project.getId(), firstProjectTodo, new Email("test1@example.com")).get();

        // then
        assertThat(findProjectTodoCheck)
                .usingRecursiveComparison()
                .isEqualTo(firstProjectToDoCheck);
    }

    @Test
    void 프로젝트_아이디와_사용자_아이디로_프로젝트_투두_체크_현황을_반환한다() {
        // given
        final Member creator = 사용자를_생성한다("name1", "test1@example.com",  "password!1");
        final FeedCategory category = 카테고리를_저장한다("헬스케어");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = 피드_노드를_생성한다("피드 2주차", "피드 2주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1, feedNode2));
        피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(11), TODAY.plusDays(20),
                feedNode2);
        final Member projectPendingMember = 사용자를_생성한다("name2", "test2@example.com", "password!2");
        final Project project = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)), projectPendingMember);

        final LocalDate today = LocalDate.now();
        final LocalDate threeDaysAfter = today.plusDays(3);
        final ProjectToDo firstProjectTodo = new ProjectToDo(
                new ProjectTodoContent("투두1"), new Period(today, threeDaysAfter));
        final ProjectToDo secondProjectTodo = new ProjectToDo(
                new ProjectTodoContent("투두2"), new Period(today, threeDaysAfter));
        project.addProjectTodo(firstProjectTodo);
        project.addProjectTodo(secondProjectTodo);
        projectRepository.save(project);

        final ProjectMember projectMember = new ProjectMember(
                ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        projectMemberRepository.save(projectMember);

        final ProjectToDoCheck firstProjectToDoCheck = new ProjectToDoCheck(projectMember, firstProjectTodo);
        final ProjectToDoCheck secondProjectToDoCheck = new ProjectToDoCheck(projectMember, secondProjectTodo);
        projectToDoCheckRepository.saveAll(List.of(firstProjectToDoCheck, secondProjectToDoCheck));

        // when
        final List<ProjectToDoCheck> findProjectTodoCheck = projectToDoCheckRepository.findByProjectIdAndMemberEmail(
                project.getId(), new Email("test1@example.com"));

        // then
        assertThat(findProjectTodoCheck)
                .usingRecursiveComparison()
                .isEqualTo(List.of(firstProjectToDoCheck, secondProjectToDoCheck));
    }

    private FeedCategory 카테고리를_저장한다(final String name) {
        final FeedCategory feedCategory = new FeedCategory(name);
        return feedCategoryRepository.save(feedCategory);
    }

    private Member 사용자를_생성한다(final String nickname,final String email, final String password) {
        final MemberProfile memberProfile = new MemberProfile(Position.FRONTEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(new SkillName("JavaScript"))));
        final Member creator = new Member(new Email(email),
                new EncryptedPassword(new Password(password)), new Nickname(nickname), null, memberProfile, skills);
        return memberRepository.save(creator);
    }

    private FeedNode 피드_노드를_생성한다(final String title, final String content) {
        return new FeedNode(title, content);
    }

    private FeedContent 피드_본문을_생성한다(final List<FeedNode> feedNodes) {
        final FeedContent feedContent = new FeedContent("피드 본문");
        feedContent.addNodes(new FeedNodes(feedNodes));
        return feedContent;
    }

    private Feed 피드를_생성한다(final Member creator, final FeedCategory category,
                          final FeedContent feedContent) {
        final Feed feed = new Feed("피드 제목", "피드 소개글", 30, creator, category);
        feed.addContent(feedContent);
        return feedRepository.save(feed);
    }

    private ProjectFeedNode 프로젝트_피드_노드를_생성한다(final LocalDate startDate, final LocalDate endDate,
                                            final FeedNode feedNode) {
        return new ProjectFeedNode(new Period(startDate, endDate), 1, feedNode);
    }

    private Project 프로젝트를_생성한다(final String name, final Integer limitedMemberCount, final FeedContent feedContent,
                               final ProjectFeedNodes projectFeedNodes, final Member member) {
        final Project project = new Project(new ProjectName(name), new LimitedMemberCount(limitedMemberCount),
                feedContent, member);
        project.addAllProjectFeedNodes(projectFeedNodes);
        return project;
    }
}
