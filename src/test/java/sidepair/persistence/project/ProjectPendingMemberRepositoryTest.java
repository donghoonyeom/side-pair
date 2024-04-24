package sidepair.persistence.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.domain.project.ProjectFeedNodes;
import sidepair.domain.project.ProjectPendingMember;
import sidepair.domain.project.ProjectRole;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectName;
import sidepair.persistence.feed.FeedCategoryRepository;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.helper.RepositoryTest;
import sidepair.persistence.member.MemberRepository;
import sidepair.persistence.project.dto.ProjectMemberSortType;

@RepositoryTest
class ProjectPendingMemberRepositoryTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);
    private static final LocalDate TWENTY_DAY_LAYER = TODAY.plusDays(20);
    private static final LocalDate THIRTY_DAY_LATER = TODAY.plusDays(30);

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final ProjectRepository projectRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final ProjectPendingMemberRepository projectPendingMemberRepository;

    public ProjectPendingMemberRepositoryTest(final MemberRepository memberRepository,
                                               final FeedCategoryRepository feedCategoryRepository,
                                               final FeedRepository feedRepository,
                                               final ProjectRepository projectRepository,
                                               final ProjectPendingMemberRepository projectPendingMemberRepository) {
        this.memberRepository = memberRepository;
        this.feedCategoryRepository = feedCategoryRepository;
        this.feedRepository = feedRepository;
        this.projectRepository = projectRepository;
        this.projectPendingMemberRepository = projectPendingMemberRepository;
    }

    @Test
    void 프로젝트와_사용자_아이디로_프로젝트_사용자_대기_목록을_조회한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final ProjectPendingMember expected = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 19, 12, 0, 0), savedProject, creator);

        // when
        final Optional<ProjectPendingMember> findProjectPendingMember = projectPendingMemberRepository.findByProjectAndMemberEmail(
                savedProject, new Email("test@example.com"));

        // then
        assertThat(findProjectPendingMember.get())
                .usingRecursiveComparison()
                .ignoringFields("id", "joinedAt")
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트와_사용자_아이디로_프로젝트_사용자_대기_목록_조회시_없으면_빈값을_반환한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        // when
        final Optional<ProjectPendingMember> findProjectPendingMember = projectPendingMemberRepository.findByProjectAndMemberEmail(
                savedProject, new Email("test1@example.com"));

        // then
        assertThat(findProjectPendingMember)
                .isEmpty();
    }

    @Test
    void 프로젝트로_사용자_대기_목록과_멤버를_함께_조회한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member member1 = 사용자를_생성한다("test1@example.com", "password2!", "name1");
        final Member member2 = 사용자를_생성한다("test2@example.com", "password3!", "name2");
        final Member member3 = 사용자를_생성한다("test3@example.com", "password4!", "name3");

        final ProjectPendingMember projectPendingMember = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), savedProject, creator);
        final ProjectPendingMember projectPendingMember1 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member1);
        final ProjectPendingMember projectPendingMember2 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member2);
        final ProjectPendingMember projectPendingMember3 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member3);
        projectPendingMemberRepository.saveAll(
                List.of(projectPendingMember1, projectPendingMember2, projectPendingMember3));

        final List<ProjectPendingMember> expected = List.of(projectPendingMember, projectPendingMember1,
                projectPendingMember2, projectPendingMember3);

        // when
        final List<ProjectPendingMember> findProjectPendingMembers = projectPendingMemberRepository.findAllByProject(
                project);

        // then
        assertThat(findProjectPendingMembers)
                .usingRecursiveComparison()
                .ignoringFields("id", "joinedAt")
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트에_참가한다() {
        //given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member follower = 사용자를_생성한다("test2@example.com", "password!2", "name");

        //when
        savedProject.join(follower);

        //then
        final List<ProjectPendingMember> projectPendingMembers = projectPendingMemberRepository.findByProject(
                project);

        final List<Member> members = projectPendingMembers.stream()
                .map(ProjectPendingMember::getMember)
                .toList();

        Assertions.assertAll(
                () -> assertThat(projectPendingMembers).hasSize(2),
                () -> assertThat(members).contains(follower)
        );
    }

    @Test
    void 프로젝트_아이디로_프로젝트_사용자를_조회하고_들어온지_오래된_순서대로_정렬한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member member1 = 사용자를_생성한다("test1@example.com", "password2!", "name1");
        final Member member2 = 사용자를_생성한다("test2@example.com", "password3!", "name2");
        final Member member3 = 사용자를_생성한다("test3@example.com", "password4!", "name3");

        final ProjectPendingMember projectPendingMember0 = project.getProjectPendingMembers().getValues().get(0);
        final ProjectPendingMember projectPendingMember1 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member1);
        final ProjectPendingMember projectPendingMember2 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member2);
        final ProjectPendingMember projectPendingMember3 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member3);
        projectPendingMemberRepository.saveAll(
                List.of(projectPendingMember1, projectPendingMember2, projectPendingMember3));
        final List<ProjectPendingMember> expected = List.of(projectPendingMember0, projectPendingMember1,
                projectPendingMember2, projectPendingMember3);

        // when
        final List<ProjectPendingMember> projectPendingMembers = projectPendingMemberRepository.findByProjectIdOrderedBySortType(
                savedProject.getId(), ProjectMemberSortType.JOINED_ASC);

        // then
        assertThat(projectPendingMembers)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디로_프로젝트_사용자를_조회하고_마지막으로_들어온_순서대로_정렬한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member member1 = 사용자를_생성한다("test1@example.com", "password2!", "name1");
        final Member member2 = 사용자를_생성한다("test2@example.com", "password3!", "name2");
        final Member member3 = 사용자를_생성한다("test3@example.com", "password4!", "name3");

        final ProjectPendingMember projectPendingMember0 = project.getProjectPendingMembers().getValues().get(0);
        final ProjectPendingMember projectPendingMember1 = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), savedProject, member1);
        final ProjectPendingMember projectPendingMember2 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member2);
        final ProjectPendingMember projectPendingMember3 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member3);
        final ProjectPendingMember savedProjectPendingMember1 = projectPendingMemberRepository.save(
                projectPendingMember1);
        final ProjectPendingMember savedProjectPendingMember2 = projectPendingMemberRepository.save(
                projectPendingMember2);
        final ProjectPendingMember savedProjectPendingMember3 = projectPendingMemberRepository.save(
                projectPendingMember3);
        final List<ProjectPendingMember> expected = List.of(savedProjectPendingMember3, savedProjectPendingMember2,
                savedProjectPendingMember1, projectPendingMember0);

        // when
        final List<ProjectPendingMember> projectPendingMembers = projectPendingMemberRepository.findByProjectIdOrderedBySortType(
                savedProject.getId(), ProjectMemberSortType.JOINED_DESC);

        // then
        assertThat(projectPendingMembers)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디로_프로젝트_사용자를_조회하고_정렬조건을_회고율순_또는_입력하지_않은경우_참여한순으로_정렬한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member member1 = 사용자를_생성한다("test1@example.com", "password2!", "name1");
        final Member member2 = 사용자를_생성한다("test2@example.com", "password3!", "name2");
        final Member member3 = 사용자를_생성한다("test3@example.com", "password4!", "name3");

        final ProjectPendingMember projectPendingMember0 = project.getProjectPendingMembers().getValues().get(0);
        final ProjectPendingMember projectPendingMember1 = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), savedProject, member1);
        final ProjectPendingMember projectPendingMember2 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member2);
        final ProjectPendingMember projectPendingMember3 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), savedProject, member3);
        projectPendingMemberRepository.saveAll(
                List.of(projectPendingMember1, projectPendingMember2, projectPendingMember3));
        final List<ProjectPendingMember> expected = List.of(projectPendingMember0, projectPendingMember1,
                projectPendingMember2, projectPendingMember3);

        // when
        final List<ProjectPendingMember> projectPendingMembers1 = projectPendingMemberRepository.findByProjectIdOrderedBySortType(
                savedProject.getId(), ProjectMemberSortType.PARTICIPATION_RATE);
        final List<ProjectPendingMember> projectPendingMembers2 = projectPendingMemberRepository.findByProjectIdOrderedBySortType(
                savedProject.getId(), null);

        // then
        assertThat(projectPendingMembers1)
                .isEqualTo(expected);
        assertThat(projectPendingMembers2)
                .isEqualTo(expected);
    }

    private Member 크리에이터를_저장한다() {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final Member creator = new Member(new Email("test@example.com"),
                new EncryptedPassword(new Password("password1!")), new Nickname("페어"),
                new MemberImage("originalFileName", "serverFilePath", ImageContentType.PNG), memberProfile, skills);
        return memberRepository.save(creator);
    }

    private Member 사용자를_생성한다(final String email, final String password, final String nickname) {
        final MemberProfile memberProfile = new MemberProfile(Position.DESIGNER);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("CSS"))));
        final Member member = new Member(new Email(email), new EncryptedPassword(new Password(password)),
                new Nickname(nickname), new MemberImage("originalFileName", "serverFilePath", ImageContentType.PNG),
                memberProfile, skills);
        return memberRepository.save(member);
    }

    private FeedCategory 카테고리를_저장한다(final String name) {
        final FeedCategory feedCategory = new FeedCategory(name);
        return feedCategoryRepository.save(feedCategory);
    }

    private Feed 피드를_저장한다(final Member creator, final FeedCategory category) {
        final List<FeedNode> feedNodes = 피드_노드들을_생성한다();
        final FeedContent feedContent = 피드_본문을_생성한다(feedNodes);
        final Feed feed = new Feed("피드 제목", "피드 소개글", 10, creator, category);
        feed.addContent(feedContent);
        return feedRepository.save(feed);
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

    private Project 프로젝트를_생성한다(final FeedContent feedContent, final Member member) {
        final Project project = new Project(new ProjectName("프로젝트"), new LimitedMemberCount(6),
                feedContent, member);
        final List<FeedNode> feedNodes = feedContent.getNodes().getValues();

        final FeedNode firstFeedNode = feedNodes.get(0);
        final ProjectFeedNode firstProjectFeedNode = new ProjectFeedNode(
                new Period(TODAY, TEN_DAY_LATER),
                10, firstFeedNode);

        final FeedNode secondFeedNode = feedNodes.get(1);
        final ProjectFeedNode secondProjectFeedNode = new ProjectFeedNode(
                new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER),
                10, secondFeedNode);

        final ProjectFeedNodes projectFeedNodes = new ProjectFeedNodes(
                List.of(firstProjectFeedNode, secondProjectFeedNode));
        project.addAllProjectFeedNodes(projectFeedNodes);
        return project;
    }
}
