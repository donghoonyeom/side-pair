package sidepair.persistence.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import sidepair.domain.project.Memoir;
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

@RepositoryTest
class MemoirRepositoryTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);
    private static final LocalDate TWENTY_DAY_LAYER = TODAY.plusDays(20);
    private static final LocalDate THIRTY_DAY_LATER = TODAY.plusDays(30);
    private static final LocalDateTime TODAY_START = TODAY.atStartOfDay();
    private static final LocalDateTime TOMORROW_START = TODAY_START.plusDays(1);
    private static final LocalDateTime DAY_AFTER_TOMORROW_START = TODAY_START.plusDays(2);

    private final MemberRepository memberRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final FeedRepository feedRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemoirRepository memoirRepository;

    public MemoirRepositoryTest(final MemberRepository memberRepository,
                                final FeedCategoryRepository feedCategoryRepository,
                                final FeedRepository feedRepository,
                                final ProjectRepository projectRepository,
                                final ProjectMemberRepository projectMemberRepository,
                                final MemoirRepository memoirRepository) {
        this.memberRepository = memberRepository;
        this.feedCategoryRepository = feedCategoryRepository;
        this.feedRepository = feedRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.memoirRepository = memoirRepository;
    }

    @Test
    void 사용자가_해당_프로젝트에서_오늘_올린_회고의_존재유무를_확인한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project = 프로젝트를_저장한다(targetFeedContent, member);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                member);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode = project.getProjectFeedNodes().getValues().get(0);
        회고를_저장한다(projectFeedNode, joinedMember);

        //when
        final boolean isUpdateToday = memoirRepository.findByProjectMemberAndDateTime(joinedMember,
                TODAY_START, TOMORROW_START).isPresent();

        final boolean isUpdateTomorrow = memoirRepository.findByProjectMemberAndDateTime(joinedMember,
                TOMORROW_START, DAY_AFTER_TOMORROW_START).isPresent();

        //then
        assertAll(
                () -> assertThat(isUpdateToday).isTrue(),
                () -> assertThat(isUpdateTomorrow).isFalse()
        );
    }

    @Test
    void 사용자가_현재_진행중인_노드에서_회고한_횟수를_확인한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project = 프로젝트를_저장한다(targetFeedContent, member);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                member);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode = project.getProjectFeedNodes().getValues().get(0);
        회고를_저장한다(projectFeedNode, joinedMember);
        회고를_저장한다(projectFeedNode, joinedMember);
        회고를_저장한다(projectFeedNode, joinedMember);
        회고를_저장한다(projectFeedNode, joinedMember);

        //when
        final int checkCount = memoirRepository.countByProjectMemberAndProjectFeedNode(joinedMember,
                projectFeedNode);

        //then
        assertThat(checkCount).isEqualTo(4);
    }

    @Test
    void 사용자가_프로젝트에서_등록한_회고_횟수를_확인한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project = 프로젝트를_저장한다(targetFeedContent, member);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                member);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode1 = project.getProjectFeedNodes().getValues().get(0);
        final ProjectFeedNode projectFeedNode2 = project.getProjectFeedNodes().getValues().get(1);

        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode2, joinedMember);
        회고를_저장한다(projectFeedNode2, joinedMember);
        회고를_저장한다(projectFeedNode2, joinedMember);

        //when
        final int checkCount = memoirRepository.countByProjectMember(joinedMember);

        //then
        assertThat(checkCount).isEqualTo(6);
    }

    @Test
    void 특정_프로젝트에서_등록된_모든_회고들을_조회한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project1 = 프로젝트를_저장한다(targetFeedContent, creator);
        final Project project2 = 프로젝트를_저장한다(targetFeedContent, creator);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project1, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project1,
                member);
        final ProjectMember otherLeader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project2,
                creator);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode1 = project1.getProjectFeedNodes().getValues().get(0);
        final ProjectFeedNode projectFeedNode2 = project1.getProjectFeedNodes().getValues().get(1);
        final ProjectFeedNode otherProjectFeedNode = project2.getProjectFeedNodes().getValues().get(0);

        final Memoir memoir1 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir2 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir3 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir4 = 회고를_저장한다(projectFeedNode2, joinedMember);
        final Memoir memoir5 = 회고를_저장한다(projectFeedNode2, joinedMember);
        final Memoir memoir6 = 회고를_저장한다(projectFeedNode2, joinedMember);
        회고를_저장한다(otherProjectFeedNode, otherLeader);
        회고를_저장한다(otherProjectFeedNode, otherLeader);

        //when
        final List<Memoir> memoirs = memoirRepository.findByProject(project1);

        assertThat(memoirs)
                .hasSize(6)
                .isEqualTo(List.of(memoir6, memoir5, memoir4, memoir3, memoir2, memoir1));
    }

    @Test
    void 프로젝트이_진행중일_때_특정_노드_동안_등록된_회고들을_조회한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project = 프로젝트를_저장한다(targetFeedContent, member);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                member);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode1 = project.getProjectFeedNodes().getValues().get(0);
        final ProjectFeedNode projectFeedNode2 = project.getProjectFeedNodes().getValues().get(1);

        final Memoir memoir1 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir2 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir3 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir4 = 회고를_저장한다(projectFeedNode2, joinedMember);
        final Memoir memoir5 = 회고를_저장한다(projectFeedNode2, joinedMember);
        final Memoir memoir6 = 회고를_저장한다(projectFeedNode2, joinedMember);

        //when
        final List<Memoir> memoirs1 = memoirRepository
                .findByRunningProjectFeedNode(projectFeedNode1);
        final List<Memoir> memoirs2 = memoirRepository
                .findByRunningProjectFeedNode(projectFeedNode2);

        assertAll(
                () -> assertThat(memoirs1)
                        .hasSize(3)
                        .isEqualTo(List.of(memoir3, memoir2, memoir1)),
                () -> assertThat(memoirs2)
                        .hasSize(3)
                        .isEqualTo(List.of(memoir6, memoir5, memoir4))
        );
    }

    @Test
    void 진행중인_프로젝트에서_특정_노드_기간이_아니면_빈_회고들_반환한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project = 프로젝트를_저장한다(targetFeedContent, member);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                member);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode1 = project.getProjectFeedNodes().getValues().get(0);
        final ProjectFeedNode projectFeedNode2 = project.getProjectFeedNodes().getValues().get(1);

        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode2, joinedMember);
        회고를_저장한다(projectFeedNode2, joinedMember);
        회고를_저장한다(projectFeedNode2, joinedMember);

        //when
        final List<Memoir> memoirs1 = memoirRepository
                .findByProjectFeedNode(null);

        assertThat(memoirs1).isEmpty();
    }

    @Test
    void 프로젝트이_완료됐을_때는_특정한_노드_동안이_아닌_모든_기간_동안_등록된_회고들을_조회한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project = 프로젝트를_저장한다(targetFeedContent, member);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                member);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode1 = project.getProjectFeedNodes().getValues().get(0);
        final ProjectFeedNode projectFeedNode2 = project.getProjectFeedNodes().getValues().get(1);

        final Memoir memoir1 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir2 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir3 = 회고를_저장한다(projectFeedNode1, joinedMember);
        final Memoir memoir4 = 회고를_저장한다(projectFeedNode2, joinedMember);
        final Memoir memoir5 = 회고를_저장한다(projectFeedNode2, joinedMember);
        final Memoir memoir6 = 회고를_저장한다(projectFeedNode2, joinedMember);

        //when
        final List<Memoir> memoirs = memoirRepository.findByProject(project);

        //then
        assertThat(memoirs)
                .isEqualTo(List.of(memoir6, memoir5, memoir4, memoir3, memoir2, memoir1));
    }

    @Test
    void 프로젝트_노드값으로_null이_들어오면_빈_리스트를_반환한다() {
        //given
        //when
        final List<Memoir> memoirs = memoirRepository.findByProjectFeedNode(null);

        //then
        assertThat(memoirs).isEmpty();
    }

    @Test
    void 프로젝트_진행_중에_특정_노드_동안_등록된_회고들을_등록한_사용자의_정보와_함께_조회한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project = 프로젝트를_저장한다(targetFeedContent, member);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                member);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode1 = project.getProjectFeedNodes().getValues().get(0);
        final ProjectFeedNode projectFeedNode2 = project.getProjectFeedNodes().getValues().get(1);

        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode2, leader);
        회고를_저장한다(projectFeedNode2, leader);
        회고를_저장한다(projectFeedNode2, leader);

        //when
        final List<Memoir> memoirs1 = memoirRepository.findByRunningProjectFeedNodeWithMemberAndMemberImage(
                projectFeedNode1);
        final List<Memoir> memoirs2 = memoirRepository.findByRunningProjectFeedNodeWithMemberAndMemberImage(
                projectFeedNode2);

        //then
        final Memoir expected1 = new Memoir("회고 본문", projectFeedNode1, joinedMember);
        final Memoir expected2 = new Memoir("회고 본문", projectFeedNode2, leader);

        assertAll(
                () -> assertThat(memoirs1).hasSize(3)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "createdAt")
                        .isEqualTo(List.of(expected1, expected1, expected1)),
                () -> assertThat(memoirs1.get(0).getProjectMember().getMember().getNickname().getValue())
                        .isEqualTo("참여자"),
                () -> assertThat(memoirs2).hasSize(3)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "createdAt")
                        .isEqualTo(List.of(expected2, expected2, expected2)),
                () -> assertThat(memoirs2.get(0).getProjectMember().getMember().getNickname().getValue())
                        .isEqualTo("사이드페어")
        );
    }

    @Test
    void 프로젝트_완료_시_모든_기간_동안_등록된_회고들을_등록한_사용자의_정보와_함께_조회한다() {
        //given
        final Member creator = 사용자를_저장한다("test@example.com", "사이드페어");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed = 피드를_저장한다(creator, category);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member member = 사용자를_저장한다("participant@example.com", "참여자");
        final Project project = 프로젝트를_저장한다(targetFeedContent, member);

        final ProjectMember leader = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), project, creator);
        final ProjectMember joinedMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), project,
                member);
        projectMemberRepository.saveAllInBatch(List.of(leader, joinedMember));

        final ProjectFeedNode projectFeedNode1 = project.getProjectFeedNodes().getValues().get(0);
        final ProjectFeedNode projectFeedNode2 = project.getProjectFeedNodes().getValues().get(1);

        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode1, joinedMember);
        회고를_저장한다(projectFeedNode2, leader);
        회고를_저장한다(projectFeedNode2, leader);
        회고를_저장한다(projectFeedNode2, leader);

        //when
        final List<Memoir> memoirs = memoirRepository.findByProjectWithMemberAndMemberImage(project);

        //then
        final Memoir expected1 = new Memoir("회고 본문", projectFeedNode1, joinedMember);
        final Memoir expected2 = new Memoir("회고 본문", projectFeedNode2, leader);

        assertThat(memoirs).hasSize(6)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt")
                .isEqualTo(List.of(expected2, expected2, expected2, expected1, expected1, expected1));
    }

    private Member 사용자를_저장한다(final String email, final String nickname) {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberImage memberImage = new MemberImage("file-name", "file-path", ImageContentType.PNG);
        final Member creator = new Member(new Email(email), new EncryptedPassword(new Password("password1!")),
                new Nickname(nickname), memberImage, memberProfile, skills);
        return memberRepository.save(creator);
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

    private Project 프로젝트를_저장한다(final FeedContent feedContent, final Member member) {
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
        return projectRepository.save(project);
    }

    private Memoir 회고를_저장한다(final ProjectFeedNode projectFeedNode, final ProjectMember joinedMember) {
        return memoirRepository.save(
                new Memoir("회고 본문", projectFeedNode, joinedMember));
    }
}
