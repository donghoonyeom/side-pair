package sidepair.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectName;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.service.scheduler.FeedScheduler;

@ExtendWith(MockitoExtension.class)
class FeedSchedulerTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TEN_DAY_LATER = TODAY.plusDays(10);

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private FeedScheduler feedScheduler;

    @Test
    void 삭제된_상태의_피드_삭제시_종료되지_않은_프로젝트가_있으면_삭제되지_않는다() {
        // given
        final Member member1 = new Member(new Email("test1@email.com"),
                new EncryptedPassword(new Password("password1!")), new Nickname("name1"), null,
                new MemberProfile(Position.BACKEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java")))));
        final Member member2 = new Member(new Email("test2@email.com"),
                new EncryptedPassword(new Password("password2!")), new Nickname("name2"), null,
                new MemberProfile(Position.FRONTEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("CSS")))));

        final FeedCategory category = new FeedCategory("이커머스");
        final FeedContent feedContent1_1 = new FeedContent("피드 본문2");
        final FeedContent feedContent1_2 = new FeedContent("피드 본문2 - 수정본");
        final FeedNode feedNode1 = new FeedNode("피드1 노드", "피드 노드 내용");
        feedContent1_1.addNodes(new FeedNodes(List.of(feedNode1)));
        feedContent1_2.addNodes(new FeedNodes(List.of(feedNode1)));
        final Feed feed1 = new Feed("피드2", "피드 설명2", 30,  member1, category);
        feed1.addContent(feedContent1_1);
        feed1.addContent(feedContent1_2);

        final Project project1_1 = new Project(new ProjectName("프로젝트2"), new LimitedMemberCount(6),
                feedContent1_1, member2);
        final Project project1_2 = new Project(new ProjectName("프로젝트2-1"), new LimitedMemberCount(6),
                feedContent1_2, member2);

        final ProjectFeedNodes projectFeedNodes1 = new ProjectFeedNodes(List.of(
                new ProjectFeedNode(new Period(TODAY, TEN_DAY_LATER), 5, feedNode1)));
        project1_1.addAllProjectFeedNodes(projectFeedNodes1);
        project1_2.addAllProjectFeedNodes(projectFeedNodes1);

        given(feedRepository.findWithFeedContentByStatus(any()))
                .willReturn(List.of(feed1));
        given(projectRepository.findByFeed(feed1))
                .willReturn(List.of(project1_1, project1_2));

        // when
        feed1.delete();
        project1_1.complete();

        // then
        assertDoesNotThrow(() -> feedScheduler.deleteFeeds());
        verify(projectRepository, never()).deleteAll(any());
        verify(feedRepository, never()).delete(any());
    }

    @Test
    void 삭제된_상태의_피드_삭제시_프로젝트가_종료된지_3개월이_지나지_않으면_삭제되지_않는다() {
        // given
        final Member member1 = new Member(new Email("test1@email.com"),
                new EncryptedPassword(new Password("password1!")), new Nickname("name1"), null,
                new MemberProfile(Position.BACKEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java")))));
        final Member member2 = new Member(new Email("test2@email.com"),
                new EncryptedPassword(new Password("password2!")), new Nickname("name2"), null,
                new MemberProfile(Position.FRONTEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("CSS")))));

        final FeedCategory category = new FeedCategory("헬스케어");
        final FeedContent feedContent1 = new FeedContent("피드 본문1");
        final FeedNode feedNode1 = new FeedNode("피드1 노드", "피드 노드 내용");
        feedContent1.addNodes(new FeedNodes(List.of(feedNode1)));

        final Feed feed1 = new Feed("피드1", "피드 설명1", 30, member1, category);
        feed1.addContent(feedContent1);

        final Project project1 = new Project(new ProjectName("프로젝트1"), new LimitedMemberCount(6), feedContent1,
                member2);
        final ProjectFeedNodes projectFeedNodes1 = new ProjectFeedNodes(List.of(
                new ProjectFeedNode(new Period(TODAY, TEN_DAY_LATER), 5, feedNode1)));
        project1.addAllProjectFeedNodes(projectFeedNodes1);

        given(feedRepository.findWithFeedContentByStatus(any()))
                .willReturn(List.of(feed1));
        given(projectRepository.findByFeed(feed1))
                .willReturn(List.of(project1));

        // when
        feed1.delete();
        project1.complete();

        // then
        assertDoesNotThrow(() -> feedScheduler.deleteFeeds());
        verify(projectRepository, never()).deleteAll(any());
        verify(feedRepository, never()).delete(any());
    }

    @Test
    void 삭제된_상태의_피드가_없는_경우_삭제되지_않는다() {
        // given
        final Member member1 = new Member(new Email("test1@email.com"),
                new EncryptedPassword(new Password("password1!")), new Nickname("name1"), null,
                new MemberProfile(Position.BACKEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java")))));
        final Member member2 = new Member(new Email("test2@email.com"),
                new EncryptedPassword(new Password("password2!")), new Nickname("name2"), null,
                new MemberProfile(Position.FRONTEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("CSS")))));

        final FeedCategory category = new FeedCategory("여행");
        final FeedContent feedContent1 = new FeedContent("피드 본문1");
        final FeedNode feedNode = new FeedNode("피드 노드", "피드 노드 내용");
        final Feed feed1 = new Feed("피드1", "피드 설명1", 30,  member1, category);
        feedContent1.addNodes(new FeedNodes(List.of(feedNode)));
        feed1.addContent(feedContent1);

        final Project project1 = new Project(new ProjectName("프로젝트1"), new LimitedMemberCount(6), feedContent1,
                member2);

        final ProjectFeedNodes projectFeedNodes = new ProjectFeedNodes(List.of(
                new ProjectFeedNode(new Period(TODAY, TEN_DAY_LATER), 5, feedNode)));
        project1.addAllProjectFeedNodes(projectFeedNodes);
        project1.complete();

        given(feedRepository.findWithFeedContentByStatus(any()))
                .willReturn(Collections.emptyList());

        // when
        // then
        assertDoesNotThrow(() -> feedScheduler.deleteFeeds());
        verify(projectRepository, never()).findByFeed(any());
        verify(feedRepository, never()).deleteAll(any());
    }
}
