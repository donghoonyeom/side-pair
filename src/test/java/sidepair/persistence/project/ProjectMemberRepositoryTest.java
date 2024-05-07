package sidepair.persistence.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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
import sidepair.domain.project.ProjectMember;
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
class ProjectMemberRepositoryTest {
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);
    private static final LocalDate TWENTY_DAY_LAYER = TODAY.plusDays(20);
    private static final LocalDate THIRTY_DAY_LATER = TODAY.plusDays(30);

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final ProjectRepository projectRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberRepositoryTest(final MemberRepository memberRepository,
                                       final FeedRepository feedRepository,
                                       final ProjectRepository projectRepository,
                                       final FeedCategoryRepository feedCategoryRepository,
                                       final ProjectMemberRepository projectMemberRepository) {
        this.memberRepository = memberRepository;
        this.feedRepository = feedRepository;
        this.projectRepository = projectRepository;
        this.feedCategoryRepository = feedCategoryRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Test
    void 프로젝트와_사용자_아이디로_프로젝트_사용자_목록을_조회한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 19, 12, 0, 0), savedProject, creator);
        final ProjectMember expected = projectMemberRepository.save(projectMember);

        // when
        final Optional<ProjectMember> findProjectMember = projectMemberRepository.findByProjectAndMemberEmail(
                savedProject, new Email("test@example.com"));

        // then
        assertThat(findProjectMember.get())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트과_사용자_아이디로_프로젝트_사용자_목록_조회시_없으면_빈값을_반환한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        // when
        final Optional<ProjectMember> findProjectMember = projectMemberRepository.findByProjectAndMemberEmail(
                savedProject, new Email("test2@example.com"));

        // then
        assertThat(findProjectMember)
                .isEmpty();
    }

    @Test
    void 프로젝트으로_사용자_목록과_멤버를_함께_조회한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member member1 = 사용자를_생성한다("participant1@example.com", "password2!", "name1");
        final Member member2 = 사용자를_생성한다("participant2@example.com", "password3!", "name2");
        final Member member3 = 사용자를_생성한다("participant3@example.com", "password4!", "name3");

        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 19, 12, 0, 0), savedProject, member1);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER,
                LocalDateTime.of(2023, 7, 20, 12, 0, 0), savedProject, member2);
        final ProjectMember projectMember3 = new ProjectMember(ProjectRole.FOLLOWER,
                LocalDateTime.of(2023, 7, 21, 12, 0, 0), savedProject, member3);
        final List<ProjectMember> expected = projectMemberRepository.saveAll(
                List.of(projectMember1, projectMember2, projectMember3));

        // when
        final List<ProjectMember> findProjectMembers = projectMemberRepository.findAllByProject(
                project);

        // then
        assertThat(findProjectMembers)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디로_프로젝트_사용자를_조회하고_들어온지_오래된_순서대로_정렬한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member member1 = 사용자를_생성한다("participant1@example.com", "password2!", "name1");
        final Member member2 = 사용자를_생성한다("participant2@example.com", "password3!", "name2");
        final Member member3 = 사용자를_생성한다("participant3@example.com", "password4!", "name3");

        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 19, 12, 0, 0), savedProject, member1);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER,
                LocalDateTime.of(2023, 7, 20, 12, 0, 0), savedProject, member2);
        final ProjectMember projectMember3 = new ProjectMember(ProjectRole.FOLLOWER,
                LocalDateTime.of(2023, 7, 21, 12, 0, 0), savedProject, member3);
        final List<ProjectMember> expected = projectMemberRepository.saveAll(
                List.of(projectMember1, projectMember2, projectMember3));

        // when
        final List<ProjectMember> projectMembers = projectMemberRepository.findByProjectIdOrderedBySortType(
                savedProject.getId(), ProjectMemberSortType.JOINED_ASC);

        // then
        assertThat(projectMembers)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디로_프로젝트_사용자를_조회하고_마지막으로_들어온_순서대로_정렬한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member member1 = 사용자를_생성한다("participant1@example.com", "password2!", "name1");
        final Member member2 = 사용자를_생성한다("participant2@example.com", "password3!", "name2");
        final Member member3 = 사용자를_생성한다("participant3@example.com", "password4!", "name3");

        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 19, 12, 0, 0), savedProject, member1);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER,
                LocalDateTime.of(2023, 7, 20, 12, 0, 0), savedProject, member2);
        final ProjectMember projectMember3 = new ProjectMember(ProjectRole.FOLLOWER,
                LocalDateTime.of(2023, 7, 21, 12, 0, 0), savedProject, member3);
        final ProjectMember savedProjectMember1 = projectMemberRepository.save(projectMember1);
        final ProjectMember savedProjectMember2 = projectMemberRepository.save(projectMember2);
        final ProjectMember savedProjectMember3 = projectMemberRepository.save(projectMember3);

        // when
        final List<ProjectMember> projectMembers = projectMemberRepository.findByProjectIdOrderedBySortType(
                savedProject.getId(), ProjectMemberSortType.JOINED_DESC);

        // then
        assertThat(projectMembers)
                .isEqualTo(List.of(savedProjectMember3, savedProjectMember2, savedProjectMember1));
    }

    @Test
    void 프로젝트_아이디로_프로젝트_사용자를_조회하고_달성률이_높은_순대로_정렬한다() {
        // given
        final Member creator = 크리에이터를_저장한다();
        final FeedCategory category = 카테고리를_저장한다("게임");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        final Project savedProject = projectRepository.save(project);

        final Member member1 = 사용자를_생성한다("participant1@example.com", "password2!", "name1");
        final Member member2 = 사용자를_생성한다("participant2@example.com", "password3!", "name2");
        final Member member3 = 사용자를_생성한다("participant3@example.com", "password4!", "name3");

        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 19, 12, 0, 0), savedProject, member1);
        projectMember1.updateParticipationRate(30.0);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER,
                LocalDateTime.of(2023, 7, 20, 12, 0, 0), savedProject, member2);
        projectMember2.updateParticipationRate(70.0);
        final ProjectMember projectMember3 = new ProjectMember(ProjectRole.FOLLOWER,
                LocalDateTime.of(2023, 7, 21, 12, 0, 0), savedProject, member3);
        projectMember3.updateParticipationRate(10.0);
        final List<ProjectMember> expected = projectMemberRepository.saveAll(
                List.of(projectMember2, projectMember1, projectMember3));

        // when
        final List<ProjectMember> projectMembers1 = projectMemberRepository.findByProjectIdOrderedBySortType(
                savedProject.getId(), ProjectMemberSortType.PARTICIPATION_RATE);
        final List<ProjectMember> projectMembers2 = projectMemberRepository.findByProjectIdOrderedBySortType(
                savedProject.getId(), null);

        // then
        assertThat(projectMembers1)
                .isEqualTo(expected);
        assertThat(projectMembers2)
                .isEqualTo(expected);
    }

    @Test
    void 프로젝트_아이디와_사용자_아이디로_프로젝트_멤버를_조회한다() {
        // given
        final Member creator = 사용자를_생성한다("participant@example.com", "password!1", "name1");
        final FeedCategory category = 카테고리를_저장한다("여가");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);

        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);
        projectRepository.save(project);

        final ProjectMember projectMember = new ProjectMember(
                ProjectRole.LEADER, LocalDateTime.now(), project, creator
        );
        projectMemberRepository.save(projectMember);

        // when
        final ProjectMember findProjectMember = projectMemberRepository.findProjectMember(project.getId(),
                creator.getEmail()).get();

        // then
        Assertions.assertThat(findProjectMember)
                .usingRecursiveComparison()
                .ignoringFields("joinedAt")
                .isEqualTo(projectMember);
    }

    private Member 크리에이터를_저장한다() {
        final MemberImage memberImage = new MemberImage("originalFileName", "serverFilePath", ImageContentType.JPG);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final Member creator = new Member(1L, new Email("test@example.com"), null,
                new EncryptedPassword(new Password("password1!")), new Nickname("생성자"), memberImage,
                memberProfile, skills);
        return memberRepository.save(creator);
    }

    private Member 사용자를_생성한다(final String email, final String password, final String nickname) {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(new SkillName("Java"))));
        final MemberImage memberImage = new MemberImage("file-name", "file-path", ImageContentType.PNG);
        final Member creator = new Member(new Email(email), new EncryptedPassword(new Password(password)),
                new Nickname(nickname), memberImage, memberProfile, skills);
        return memberRepository.save(creator);
    }

    private FeedCategory 카테고리를_저장한다(final String name) {
        final FeedCategory feedCategory = new FeedCategory(name);
        return feedCategoryRepository.save(feedCategory);
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

    private Feed 피드를_저장한다(final Member creator, final FeedCategory category) {
        final List<FeedNode> feedNodes = 피드_노드들을_생성한다();
        final FeedContent feedContent = 피드_본문을_생성한다(feedNodes);
        final Feed feed = new Feed("피드 제목", "피드 소개글", 10, creator, category);
        feed.addContent(feedContent);
        return feedRepository.save(feed);
    }

    private Project 프로젝트를_생성한다(final FeedContent feedContent, final Member member) {
        final Project project = new Project(new ProjectName("프로젝트"), new LimitedMemberCount(6),
                feedContent, member);
        final List<FeedNode> feedNodes = feedContent.getNodes().getValues();

        final FeedNode firstFeedNode = feedNodes.get(0);
        final ProjectFeedNode firstProjectFeedNode = new ProjectFeedNode(
                new Period(TODAY, TEN_DAY_LATER), 10, firstFeedNode);

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
