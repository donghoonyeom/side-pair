package sidepair.persistence.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
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
import sidepair.domain.project.ProjectStatus;
import sidepair.domain.project.ProjectToDo;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectName;
import sidepair.domain.project.vo.ProjectTodoContent;
import sidepair.persistence.feed.FeedCategoryRepository;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.helper.RepositoryTest;
import sidepair.persistence.member.MemberRepository;
import sidepair.persistence.project.dto.FeedProjectsOrderType;

@RepositoryTest
class ProjectRepositoryTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);
    private static final LocalDate TWENTY_DAY_LAYER = TODAY.plusDays(20);
    private static final LocalDate THIRTY_DAY_LATER = TODAY.plusDays(30);

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final ProjectRepository projectRepository;
    private final FeedCategoryRepository feedCategoryRepository;

    public ProjectRepositoryTest(final MemberRepository memberRepository,
                                  final FeedRepository feedRepository,
                                  final ProjectRepository projectRepository,
                                  final FeedCategoryRepository feedCategoryRepository) {
        this.memberRepository = memberRepository;
        this.feedRepository = feedRepository;
        this.projectRepository = projectRepository;
        this.feedCategoryRepository = feedCategoryRepository;
    }

    @Test
    void 프로젝트_아이디로_프로젝트_정보를_조회한다() {
        //given
        final Member creator = 사용자를_생성한다("name1", "test1@example.com", "password!1");
        final FeedCategory category = 카테고리를_저장한다("커뮤니티");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = 피드_노드를_생성한다("피드 2주차", "피드 2주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1, feedNode2));
        피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(11), TODAY.plusDays(20),
                feedNode2);
        final Member projectPendingMember1 = 사용자를_생성한다("name2", "test2@exmample.com", "password!2");
        final Project project = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)), projectPendingMember1);
        projectRepository.save(project);

        // when
        final Project findProject = projectRepository.findByIdWithFeedContent(project.getId())
                .get();

        // then
        assertThat(findProject)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(project);
    }

    @Test
    void 프로젝트_최신순으로_조회한다() {
        //given
        final Member creator = 사용자를_생성한다("name1", "test1@example.com", "password!1");
        final FeedCategory category = 카테고리를_저장한다("커뮤니티");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = 피드_노드를_생성한다("피드 2주차", "피드 2주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1, feedNode2));
        final Feed feed = 피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(11), TODAY.plusDays(20),
                feedNode2);
        final Member projectPendingMember1 = 사용자를_생성한다("name2", "test2@example.com", "password!2");
        final Project project1 = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)), projectPendingMember1);
        projectRepository.save(project1);

        final ProjectFeedNode projectFeedNode3 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode4 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(11), TODAY.plusDays(20),
                feedNode2);
        final Member projectPendingMember2 = 사용자를_생성한다("name3", "test3@example.com", "password!3");
        final Project project2 = 프로젝트를_생성한다("project2", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode3, projectFeedNode4)), projectPendingMember2);
        projectRepository.save(project2);

        // when
        final List<Project> projects1 = projectRepository.findProjectsByFeedAndCond(feed,
                FeedProjectsOrderType.LATEST, null, 1);
        final List<Project> projects2 = projectRepository.findProjectsByFeedAndCond(feed,
                FeedProjectsOrderType.LATEST, project2.getId(), 10);

        assertThat(projects1)
                .isEqualTo(List.of(project2, project1));
        assertThat(projects2)
                .isEqualTo(List.of(project1));
    }

    @Test
    void 프로젝트_마감임박_순으로_조회한다() {
        //given
        final Member creator = 사용자를_생성한다("name1", "test1@example.com", "password!1");
        final FeedCategory category = 카테고리를_저장한다("커뮤니티");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = 피드_노드를_생성한다("피드 2주차", "피드 2주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1, feedNode2));
        final Feed feed = 피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10), feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(11), TODAY.plusDays(20),
                feedNode2);
        final Member projectPendingMember1 = 사용자를_생성한다("name2", "test2@example.com", "password!2");
        final Project project1 = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)), projectPendingMember1);
        projectRepository.save(project1);

        final ProjectFeedNode projectFeedNode3 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(1), TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode4 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(11), TODAY.plusDays(20),
                feedNode2);
        final Member projectPendingMember3 = 사용자를_생성한다("name4", "test4@example.com", "password!4");
        final Project project2 = 프로젝트를_생성한다("project2", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode3, projectFeedNode4)), projectPendingMember3);
        projectRepository.save(project2);

        // when
        final List<Project> projects1 = projectRepository.findProjectsByFeedAndCond(feed,
                FeedProjectsOrderType.CLOSE_TO_DEADLINE, null, 1);
        final List<Project> projects2 = projectRepository.findProjectsByFeedAndCond(feed,
                FeedProjectsOrderType.CLOSE_TO_DEADLINE, project1.getId(), 10);

        // then
        assertThat(projects1).isEqualTo(List.of(project1, project2));
        assertThat(projects2).isEqualTo(List.of(project2));
    }

    @Test
    void 프로젝트의_노드의_시작날짜가_오늘인_프로젝트_조회한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1));
        피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode3 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(10), TODAY.plusDays(20),
                feedNode1);

        final Project project1 = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1)), creator);
        final Project project2 = 프로젝트를_생성한다("project2", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode2)), creator);
        final Project project3 = 프로젝트를_생성한다("project3", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode3)), creator);

        final Project savedProject1 = projectRepository.save(project1);
        final Project savedProject2 = projectRepository.save(project2);
        projectRepository.save(project3);

        // when
        final List<Project> findProjects = projectRepository.findAllRecruitingProjectsByStartDateEarlierThan(LocalDate.now());

        // then
        assertThat(findProjects)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(savedProject1, savedProject2));
    }

    @Test
    void 투두리스트와_함께_프로젝트_조회한다() {
        final Member creator = 사용자를_생성한다("name1", "test1@example.com", "password!1");
        final FeedCategory category = 카테고리를_저장한다("커뮤니티");
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
        project.addProjectTodo(new ProjectToDo(
                new ProjectTodoContent("투두1"), new Period(today, threeDaysAfter)
        ));
        project.addProjectTodo(new ProjectToDo(
                new ProjectTodoContent("투두2"), new Period(today, threeDaysAfter)
        ));
        projectRepository.save(project);

        // when
        final Project findProject = projectRepository.findByIdWithTodos(project.getId()).get();

        // then
        assertThat(findProject)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(project);
    }

    @Test
    void 프로젝트_아이디로_프로젝트과_피드컨텐츠_프로젝트노드_투두_정보를_조회한다() {
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final FeedNode feedNode = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode));
        피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode);

        final Project project1 = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1)), creator);
        final ProjectToDo projectToDo1 = new ProjectToDo(new ProjectTodoContent("할 일 목록"),
                new Period(TODAY, TEN_DAY_LATER));
        project1.addProjectTodo(projectToDo1);
        final Project savedProject1 = projectRepository.save(project1);

        final Project project2 = 프로젝트를_생성한다("project2", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode2)), creator);
        final ProjectToDo projectToDo2 = new ProjectToDo(new ProjectTodoContent("투두 컨텐츠"),
                new Period(TEN_DAY_LATER, TWENTY_DAY_LAYER));
        project2.addProjectTodo(projectToDo2);
        final Project savedProject2 = projectRepository.save(project2);

        // when
        final Project findProject1 = projectRepository.findByIdWithContentAndTodos(project1.getId())
                .get();
        final Project findProject2 = projectRepository.findByIdWithContentAndTodos(project2.getId())
                .get();

        //then
        assertAll(
                () -> assertThat(findProject1)
                        .usingRecursiveComparison()
                        .ignoringFields("id")
                        .isEqualTo(savedProject1),
                () -> assertThat(findProject2)
                        .usingRecursiveComparison()
                        .ignoringFields("id")
                        .isEqualTo(savedProject2)
        );
    }

    @Test
    void 사용자가_참가한_모든_프로젝트들을_조회한다() {
        //given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1));
        피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode3 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(10), TODAY.plusDays(20),
                feedNode1);
        final ProjectFeedNode projectFeedNode4 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(10), TODAY.plusDays(20),
                feedNode1);

        final Project project1 = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1)), creator);
        final Project project2 = 프로젝트를_생성한다("project2", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode2)), creator);
        final Project project3 = 프로젝트를_생성한다("project3", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode3)), creator);
        final Project project4 = 프로젝트를_생성한다("project3", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode3)), creator);

        final Member member = 사용자를_생성한다("팔로워", "test2@example.com", "password2@");
        project1.join(member);
        project2.join(member);
        project3.join(member);
        project2.start();
        project3.complete();

        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);

        //when
        final List<Project> creatorMemberProjects = projectRepository.findByMember(creator);
        final List<Project> followerMemberProjects = projectRepository.findByMember(member);

        //then
        assertAll(
                () -> assertThat(creatorMemberProjects)
                        .usingRecursiveComparison()
                        .isEqualTo(List.of(project1, project2, project3, project4)),
                () -> assertThat(followerMemberProjects)
                        .usingRecursiveComparison()
                        .isEqualTo(List.of(project1, project2, project3))
        );
    }

    @Test
    void 사용자가_참가한_프로젝트들을_상태에_따라_조회한다() {
        //given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1));
        피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TODAY, TODAY.plusDays(10),
                feedNode1);
        final ProjectFeedNode projectFeedNode3 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(10), TODAY.plusDays(20),
                feedNode1);
        final ProjectFeedNode projectFeedNode4 = 프로젝트_피드_노드를_생성한다(TODAY.plusDays(10), TODAY.plusDays(20),
                feedNode1);

        final Project project1 = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1)), creator);
        final Project project2 = 프로젝트를_생성한다("project2", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode2)), creator);
        final Project project3 = 프로젝트를_생성한다("project3", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode3)), creator);
        final Project project4 = 프로젝트를_생성한다("project3", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode3)), creator);

        final Member member = 사용자를_생성한다("팔로워", "test2@example.com", "password2@");
        project1.join(member);
        project2.join(member);
        project3.join(member);
        project4.join(member);
        project2.start();
        project3.complete();

        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);

        //when
        final List<Project> memberRecruitingProjects = projectRepository.findByMemberAndStatus(member,
                ProjectStatus.RECRUITING);
        final List<Project> memberRunningProjects = projectRepository.findByMemberAndStatus(member,
                ProjectStatus.RUNNING);
        final List<Project> memberCompletedProjects = projectRepository.findByMemberAndStatus(member,
                ProjectStatus.COMPLETED);

        //then
        assertAll(
                () -> assertThat(memberRecruitingProjects)
                        .usingRecursiveComparison()
                        .isEqualTo(List.of(project1, project4)),
                () -> assertThat(memberRunningProjects)
                        .usingRecursiveComparison()
                        .isEqualTo(List.of(project2)),
                () -> assertThat(memberCompletedProjects)
                        .usingRecursiveComparison()
                        .isEqualTo(List.of(project3))
        );
    }

    @Test
    void 노드와_함께_프로젝트_조회한다() {
        final Member creator = 사용자를_생성한다("name1", "test1@example.com", "password!1");
        final FeedCategory category = 카테고리를_저장한다("커뮤니티");
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
        projectRepository.save(project);

        // when
        final Project findProject = projectRepository.findByIdWithNodes(project.getId()).get();

        // then
        assertThat(findProject)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(project);
    }

    @Test
    void 피드에_생성된_모든_프로젝트_조회한다() {
        //given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final FeedNode feedNode = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode));
        final Feed feed = 피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TEN_DAY_LATER,
                feedNode);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TEN_DAY_LATER, TWENTY_DAY_LAYER,
                feedNode);

        final Project project1 = 프로젝트를_생성한다("project1", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1)), creator);
        final Project project2 = 프로젝트를_생성한다("project2", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode2)), creator);
        final Project project3 = 프로젝트를_생성한다("project3", 6, feedContent,
                new ProjectFeedNodes(List.of(projectFeedNode1)), creator);
        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);

        // when
        final List<Project> projects = projectRepository.findByFeed(feed);

        // then
        assertThat(projects)
                .isEqualTo(List.of(project1, project2, project3));
    }

    @Test
    void 피드로_프로젝트_조회한다() {
        // given
        final Member creator = 사용자를_생성한다("name1", "test1@example.com", "password!1");
        final FeedCategory category = 카테고리를_저장한다("커뮤니티");
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = 피드_노드를_생성한다("피드 2주차", "피드 2주차 내용");

        final FeedContent feedContent1 = 피드_본문을_생성한다(List.of(feedNode1, feedNode2));
        final Feed feed1 = 피드를_생성한다(creator, category, feedContent1);

        final FeedNode feedNode3 = 피드_노드를_생성한다("피드 1주차 입니다.", "피드 1주차 내용");
        final FeedNode feedNode4 = 피드_노드를_생성한다("피드 2주차 입니다.", "피드 2주차 내용");

        final FeedContent feedContent2 = 피드_본문을_생성한다(List.of(feedNode3, feedNode4));
        피드를_생성한다(creator, category, feedContent2);

        final Member member = 사용자를_생성한다("name2", "test2@example.com", "password!2");

        final ProjectFeedNode projectFeedNode1 = 프로젝트_피드_노드를_생성한다(TODAY, TEN_DAY_LATER, feedNode1);
        final ProjectFeedNode projectFeedNode2 = 프로젝트_피드_노드를_생성한다(TWENTY_DAY_LAYER, THIRTY_DAY_LATER,
                feedNode2);
        final Project project1 = 프로젝트를_생성한다("project1", 6, feedContent1,
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)), member);
        final Project project2 = 프로젝트를_생성한다("project2", 6, feedContent1,
                new ProjectFeedNodes(List.of(projectFeedNode1, projectFeedNode2)), member);

        final ProjectFeedNode projectFeedNode3 = 프로젝트_피드_노드를_생성한다(TODAY, TEN_DAY_LATER, feedNode3);
        final ProjectFeedNode projectFeedNode4 = 프로젝트_피드_노드를_생성한다(TWENTY_DAY_LAYER, THIRTY_DAY_LATER,
                feedNode4);
        final Project project3 = 프로젝트를_생성한다("project3", 6, feedContent2,
                new ProjectFeedNodes(List.of(projectFeedNode3, projectFeedNode4)), member);

        projectRepository.saveAll(List.of(project1, project2, project3));

        // when
        final List<Project> projects = projectRepository.findByFeed(feed1);

        // then
        assertThat(projects).isEqualTo(List.of(project1, project2));
    }

    @Test
    void 시작날짜가_오늘보다_작거나_같은_모집중인_프로젝트들을_조회한다() {
        //given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final FeedNode feedNode = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode));
        피드를_생성한다(creator, category, feedContent);

        final ProjectFeedNode todayProjectFeedNode = 프로젝트_피드_노드를_생성한다(TODAY, TEN_DAY_LATER,
                feedNode);
        final ProjectFeedNode afterTodayProjectFeedNode = 프로젝트_피드_노드를_생성한다(TEN_DAY_LATER, TWENTY_DAY_LAYER,
                feedNode);

        final Project todayStartProject1 = 프로젝트를_생성한다("project1", 4, feedContent,
                new ProjectFeedNodes(List.of(todayProjectFeedNode)), creator);
        final Project futureStartProject = 프로젝트를_생성한다("project2", 5, feedContent,
                new ProjectFeedNodes(List.of(afterTodayProjectFeedNode)), creator);
        final Project todayStartProject2 = 프로젝트를_생성한다("project3", 6, feedContent,
                new ProjectFeedNodes(List.of(todayProjectFeedNode)), creator);
        projectRepository.saveAll(List.of(todayStartProject1, futureStartProject, todayStartProject2));

        // when
        final List<Project> projects = projectRepository.findAllRecruitingProjectsByStartDateEarlierThan(LocalDate.now());

        // then
        assertThat(projects).isEqualTo(List.of(todayStartProject1, todayStartProject2));
    }

    private Member 크리에이터를_저장한다() {
        final MemberProfile memberProfile = new MemberProfile(Position.FRONTEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("JavaScript"))));
        final Member creator = new Member(new Email("test@example.com"),
                new EncryptedPassword(new Password("password1!")), new Nickname("사이드페어"), null,
                memberProfile, skills);
        return memberRepository.save(creator);
    }

    private Member 사용자를_생성한다(final String nickname, final String email, final String password) {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final Member creator = new Member(new Email(email),
                new EncryptedPassword(new Password(password)), new Nickname(nickname), null, memberProfile, skills);
        return memberRepository.save(creator);
    }

    private FeedCategory 카테고리를_저장한다(final String name) {
        final FeedCategory feedCategory = new FeedCategory(name);
        return feedCategoryRepository.save(feedCategory);
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
