package sidepair.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sidepair.domain.project.ProjectStatus.RECRUITING;
import static sidepair.domain.project.ProjectStatus.RUNNING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectPendingMemberRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.service.scheduler.ProjectScheduler;

@ExtendWith(MockitoExtension.class)
class ProjectSchedulerTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);
    private static final LocalDate TWENTY_DAY_LATER = TODAY.plusDays(20);

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectPendingMemberRepository projectPendingMemberRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectScheduler projectScheduler;

    @Test
    void 프로젝트의_시작날짜가_되면_프로젝트의_상태가_진행중으로_변경된다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "사이드페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project1 = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        final Project project2 = 프로젝트를_생성한다(2L, creator, targetFeedContent, 6);

        final Member follower1 = 사용자를_생성한다(2L, "test1@email.com", "password2!", "name1");
        final Member follower2 = 사용자를_생성한다(3L, "test2@email.com", "password3!", "name2");
        final Member follower3 = 사용자를_생성한다(4L, "test3@email.com", "password4!", "name3");

        프로젝트_대기자를_생성한다(project2, creator, ProjectRole.FOLLOWER);
        프로젝트_대기자를_생성한다(project1, follower1, ProjectRole.FOLLOWER);
        프로젝트_대기자를_생성한다(project1, follower2, ProjectRole.FOLLOWER);

        project1.join(follower1);
        project1.join(follower2);
        project2.join(follower3);

        when(projectRepository.findAllRecruitingProjectsByStartDateEarlierThan(LocalDate.now()))
                .thenReturn(List.of(project1));

        // when
        projectScheduler.startProjects();

        // then
        assertAll(
                () -> assertThat(project1.getStatus()).isEqualTo(RUNNING),
                () -> assertThat(project2.getStatus()).isEqualTo(RECRUITING)
        );
    }

    @Test
    void 프로젝트의_시작날짜가_아직_지나지_않았다면_프로젝트의_상태가_변경되지_않는다() {
        // given
        final Member creator = 사용자를_생성한다(1L, "test@email.com", "password1!", "사이드페어");
        final Feed feed = 피드를_생성한다(creator);

        final FeedContents feedContents = feed.getContents();
        final FeedContent targetFeedContent = feedContents.getValues().get(0);
        final Project project1 = 프로젝트를_생성한다(1L, creator, targetFeedContent, 6);
        final Project project2 = 프로젝트를_생성한다(2L, creator, targetFeedContent, 6);

        final Member follower1 = 사용자를_생성한다(2L, "test1@email.com", "password2!", "name1");
        final Member follower2 = 사용자를_생성한다(3L, "test2@email.com", "password3!", "name2");
        final Member follower3 = 사용자를_생성한다(4L, "test3@email.com", "password4!", "name3");

        project1.join(follower1);
        project1.join(follower2);
        project2.join(follower3);

        when(projectRepository.findAllRecruitingProjectsByStartDateEarlierThan(LocalDate.now()))
                .thenReturn(Collections.emptyList());

        // when
        projectScheduler.startProjects();

        // then
        verify(projectPendingMemberRepository, never()).deleteAllByIdIn(anyList());

        assertAll(
                () -> assertThat(project1.getStatus()).isEqualTo(RECRUITING),
                () -> assertThat(project2.getStatus()).isEqualTo(RECRUITING)
        );
    }

    private Member 사용자를_생성한다(final Long memberId, final String email, final String password, final String nickname) {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));

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

    private ProjectFeedNodes 프로젝트_피드_노드들을_생성한다(final FeedNodes feedNodes) {
        return new ProjectFeedNodes(List.of(
                new ProjectFeedNode(new Period(TODAY, TEN_DAY_LATER), 5, feedNodes.getValues().get(0)),
                new ProjectFeedNode(new Period(TEN_DAY_LATER.plusDays(1), TWENTY_DAY_LATER), 5,
                        feedNodes.getValues().get(1)))
        );
    }

    private ProjectPendingMember 프로젝트_대기자를_생성한다(final Project project, final Member follower,
                                               final ProjectRole role) {
        return new ProjectPendingMember(role, LocalDateTime.of(2023, 7, 19, 12, 0, 0), project, follower);
    }
}
