package sidepair.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedContents;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.feed.FeedNodeImages;
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
import sidepair.domain.project.exeption.ProjectException;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectName;

class ProjectTest {

    private static final ProjectName PROJECT_NAME = new ProjectName("프로젝트 이름");
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(6);
    private static final LocalDate TWENTY_DAY_LAYER = TODAY.plusDays(20);
    private static final LocalDate THIRTY_DAY_LATER = TODAY.plusDays(30);

    private static Member member;

    @BeforeAll
    static void setUp() {
        final Email email = new Email("test1@example.com");
        final Password password = new Password("password1!");
        final EncryptedPassword encryptedPassword = new EncryptedPassword(password);
        final Nickname nickname = new Nickname("nickname");
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        member = new Member(1L, email, null, encryptedPassword, nickname, null, memberProfile, skills);
    }

    @Test
    void 프로젝트의_총_기간을_계산한다() {
        // given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);

        // when
        final int totalPeriod = project.calculateTotalPeriod();

        // then
        assertThat(totalPeriod)
                .isSameAs(31);
    }

    @Test
    void 프로젝트에_대기중인_인원수를_계산한다() {
        // given
        final Project project = new Project(new ProjectName("project"), new LimitedMemberCount(6),
                new FeedContent("content"), member);
        final Member member1 = new Member(2L, new Email("test2@example.com"),
                null, new EncryptedPassword(new Password("password1")), new Nickname("닉네임2"),
                null, new MemberProfile(Position.BACKEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java")))));
        final Member member2 = new Member(3L, new Email("test3@example.com"),
                null, new EncryptedPassword(new Password("password1")), new Nickname("닉네임3"),
                null,
                new MemberProfile(Position.FRONTEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("CSS")))));

        // when
        project.join(member1);
        project.join(member2);

        // then
        assertThat(project.getCurrentMemberCount()).isEqualTo(3);
    }

    @Test
    void 프로젝트에_사용자를_추가한다() {
        //given
        final Project project = new Project(PROJECT_NAME, new LimitedMemberCount(6),
                new FeedContent("피드 내용"), member);
        final Member follower = 사용자를_생성한다(2L, "test2@example.com", "닉네임");

        //when
        project.join(follower);

        //then
        final Integer currentMemberCount = project.getCurrentMemberCount();
        assertThat(currentMemberCount)
                .isEqualTo(2);
    }

    @Test
    void 모집중이_아닌_프로젝트에_사용자를_추가하면_예외가_발생한다() {
        //given
        final Project project = new Project(PROJECT_NAME, new LimitedMemberCount(6), new FeedContent("피드 내용"),
                사용자를_생성한다(2L, "test1@example.com", "닉네임"));
        project.start();

        //when, then
        assertThatThrownBy(() -> project.join(member))
                .isInstanceOf(ProjectException.class)
                .hasMessage("모집 중이지 않은 프로젝트에는 멤버를 추가할 수 없습니다.");
    }

    @Test
    void 제한_인원이_가득_찬_프로젝트에_사용자를_추가하면_예외가_발생한다() {
        //given
        final Project project = new Project(PROJECT_NAME, new LimitedMemberCount(1), new FeedContent("피드 내용"),
                사용자를_생성한다(2L, "test1@example.com", "닉네임"));

        //when,then
        assertThatThrownBy(() -> project.join(member))
                .isInstanceOf(ProjectException.class)
                .hasMessage("제한 인원이 꽉 찬 프로젝트에는 멤버를 추가할 수 없습니다.");
    }

    @Test
    void 피드_작성자가_아닌_사용자가_프로젝트를_생성하면_예외가_발생한다() {
        //given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);
        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Member 작성자가_아닌_사용자 = 사용자를_생성한다(2L, "test1@example.com", "닉네임");

        //when, then
        assertThatThrownBy(() -> Project.createProject(PROJECT_NAME, new LimitedMemberCount(6), targetFeedContent,
                작성자가_아닌_사용자))
                .isInstanceOf(ProjectException.class)
                .hasMessage("피드를 생성한 사용자가 아닙니다.");
    }

    @Test
    void 이미_참여_중인_사용자를_프로젝트에_추가하면_예외가_발생한다() {
        //given
        final Project project = new Project(PROJECT_NAME, new LimitedMemberCount(2),
                new FeedContent("피드 내용"), member);

        //when,then
        assertThatThrownBy(() -> project.join(member))
                .isInstanceOf(ProjectException.class)
                .hasMessage("이미 프로젝트에 추가한 멤버는 추가할 수 없습니다.");
    }

    @Test
    void 프로젝트의_총_회고_횟수를_구한다() {
        //given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);

        //expect
        assertThat(project.getAllMemoirCount()).isEqualTo(12);
    }

    @Test
    void 프로젝트가_시작하기_전에_참여_멤버를_확인한다() {
        //given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);

        final Member 참여자 = 사용자를_생성한다(2L, "test1@example.com", "팔로워");
        project.join(참여자);

        //expect
        assertAll(
                () -> assertThat(project.isProjectMember(참여자)).isTrue(),
                () -> assertThat(project.getCurrentMemberCount()).isEqualTo(2)
        );
    }

    @Test
    void 프로젝트가_시작한_후에_참여_멤버를_확인한다() {
        //given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);

        final Member 참여자 = 사용자를_생성한다(2L, "test1@example.com", "팔로워");
//        project.join(참여자);
        project.start();

        //expect
        assertAll(
                () -> assertThat(project.isProjectMember(참여자)).isFalse(),
                () -> assertThat(project.getCurrentMemberCount()).isEqualTo(0)
        );
    }

    @Test
    void 프로젝트를_나간다() {
        //given
        final Project project = new Project(PROJECT_NAME, new LimitedMemberCount(2),
                new FeedContent("피드 내용"), member);

        // when
        project.leave(member);

        // then
        assertThat(project.isEmptyProject()).isTrue();
    }

    @Test
    void 프로젝트에_참여하지_않은_멤버가_나가면_예외가_발생한다() {
        //given
        final Project project = new Project(PROJECT_NAME, new LimitedMemberCount(2),
                new FeedContent("피드 내용"), member);

        final Member notJoinMember = new Member(new Email("test2@example.com"),
                new EncryptedPassword(new Password("password2!")),
                new Nickname("name2"), null, null, null);

        // when
        // then
        assertThatThrownBy(() -> project.leave(notJoinMember))
                .isInstanceOf(ProjectException.class);
    }

    @Test
    void 프로젝트가_종료된지_2개월_이상_지나지_않으면_false를_반환한다() {
        //given
        final Member creator = 크리에이터를_생성한다();
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project = 프로젝트를_생성한다(targetFeedContent, creator);

        // when
        final boolean result = project.isCompletedAfterMonths(2);

        // then
        assertThat(result).isEqualTo(false);
    }

    private Member 크리에이터를_생성한다() {
        final MemberProfile memberProfile = new MemberProfile(Position.FRONTEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("HTML"))));
        return new Member(new Email("test@example.com"), new EncryptedPassword(new Password("password1!")),
                new Nickname("사이드페어"), null, memberProfile, skills);
    }

    private Member 사용자를_생성한다(final Long id, final String email, final String nickname) {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        return new Member(id, new Email(email), null, new EncryptedPassword(new Password("password1!")),
                new Nickname(nickname), null, memberProfile, skills);
    }

    private Feed 피드를_생성한다(final Member creator) {
        final FeedCategory category = new FeedCategory("게임");
        final List<FeedNode> feedNodes = 피드_노드들을_생성한다();
        final FeedContent feedContent = 피드_본문을_생성한다(feedNodes);
        final Feed feed = new Feed("피드 제목", "피드 소개글", 6, creator, category);
        feed.addContent(feedContent);
        return feed;
    }

    private List<FeedNode> 피드_노드들을_생성한다() {
        final FeedNode feedNode1 = new FeedNode("피드 1주차", "피드 1주차 내용");
        feedNode1.addImages(new FeedNodeImages(Collections.emptyList()));
        final FeedNode feedNode2 = new FeedNode("피드 2주차", "피드 2주차 내용");
        return List.of(feedNode1, feedNode2);
    }

    private FeedContent 피드_본문을_생성한다(final List<FeedNode> feedNodes) {
        final FeedContent feedContent = new FeedContent("피드 본문");
        feedContent.addNodes(new FeedNodes(feedNodes));
        return feedContent;
    }

    private Project 프로젝트를_생성한다(final FeedContent feedContent, final Member creator) {
        final Project project = Project.createProject(new ProjectName("프로젝트"),
                new LimitedMemberCount(6), feedContent, creator);
        final List<FeedNode> feedNodes = feedContent.getNodes().getValues();

        final FeedNode firstFeedNode = feedNodes.get(0);
        final ProjectFeedNode firstProjectFeedNode = new ProjectFeedNode(
                new Period(TODAY, TEN_DAY_LATER), 6, firstFeedNode);

        final FeedNode secondFeedNode = feedNodes.get(1);
        final ProjectFeedNode secondProjectFeedNode = new ProjectFeedNode(
                new Period(TWENTY_DAY_LAYER, THIRTY_DAY_LATER), 6, secondFeedNode);

        final ProjectFeedNodes projectFeedNodes = new ProjectFeedNodes(
                List.of(firstProjectFeedNode, secondProjectFeedNode));
        project.addAllProjectFeedNodes(projectFeedNodes);
        return project;
    }
}