package sidepair.persistence.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sidepair.domain.ImageContentType;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedApplicant;
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
import sidepair.domain.member.vo.MemberImage;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.domain.member.vo.SkillName;
import sidepair.persistence.helper.RepositoryTest;
import sidepair.persistence.member.MemberRepository;

@RepositoryTest
class FeedApplicantRepositoryTest {

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final FeedApplicantRepository feedApplicantRepository;
    private final FeedCategoryRepository feedCategoryRepository;

    public FeedApplicantRepositoryTest(final MemberRepository memberRepository,
                                       final FeedRepository feedRepository,
                                       final FeedApplicantRepository feedApplicantRepository,
                                       final FeedCategoryRepository feedCategoryRepository) {
        this.memberRepository = memberRepository;
        this.feedRepository = feedRepository;
        this.feedApplicantRepository = feedApplicantRepository;
        this.feedCategoryRepository = feedCategoryRepository;
    }

    @Test
    void 피드와_사용자로_피드_신청서_정보가_존재하면_반환한다() {
        // given
        final Member member = 사용자를_저장한다("사이드", "test@example.com");
        final FeedCategory category = 카테고리를_저장한다("게임");
        final Feed feed = 피드를_저장한다(member, category);

        final FeedApplicant feedApplicant = new FeedApplicant("신청서", member);
        feedApplicant.updateFeed(feed);
        feedApplicantRepository.save(feedApplicant);

        // when
        final FeedApplicant findFeedApplicant = feedApplicantRepository.findByFeedAndMember(feed, member).get();

        // then
        assertThat(findFeedApplicant)
                .isEqualTo(feedApplicant);
    }

    @Test
    void 피드와_사용자로_피드_신청서_정보가_존재하지_않으면_빈값을_반환한다() {
        // given
        final Member member = 사용자를_저장한다("사이드", "test@example.com");
        final Member member2 = 사용자를_저장한다("페어", "test1@example.com");
        final FeedCategory category = 카테고리를_저장한다("커뮤니티");
        final Feed feed = 피드를_저장한다(member, category);

        final FeedApplicant feedApplicant = new FeedApplicant("신청서", member);
        feedApplicant.updateFeed(feed);
        feedApplicantRepository.save(feedApplicant);

        // when
        final Optional<FeedApplicant> findFeedApplicant = feedApplicantRepository.findByFeedAndMember(feed,
                member2);

        // then
        assertThat(findFeedApplicant)
                .isEmpty();
    }

    @Test
    void 피드에_대한_신청서_정보를_최신순으로_조회한다() {
        // given
        final Member member = 사용자를_저장한다("사이드", "test@example.com");
        final Member member2 = 사용자를_저장한다("페어", "test1@example.com");
        final Member member3 = 사용자를_저장한다("사페", "test3@example.com");
        final FeedCategory category = 카테고리를_저장한다("게임");
        final Feed feed = 피드를_저장한다(member, category);

        final FeedApplicant feedApplicant1 = new FeedApplicant("신청서1", member);
        final FeedApplicant feedApplicant2 = new FeedApplicant("신청서2", member2);
        final FeedApplicant feedApplicant3 = new FeedApplicant("신청서3", member3);
        feedApplicant1.updateFeed(feed);
        feedApplicant2.updateFeed(feed);
        feedApplicant3.updateFeed(feed);
        feedApplicantRepository.save(feedApplicant1);
        feedApplicantRepository.save(feedApplicant2);
        feedApplicantRepository.save(feedApplicant3);

        // when
        final List<FeedApplicant> feedApplicantsFirstPage = feedApplicantRepository.findFeedApplicantWithMemberByFeedOrderByLatest(
                feed, null, 2);

        final List<FeedApplicant> feedApplicantsSecondPage = feedApplicantRepository.findFeedApplicantWithMemberByFeedOrderByLatest(
                feed, feedApplicantsFirstPage.get(1).getId(), 2);

        // then
        assertAll(
                () -> assertThat(feedApplicantsFirstPage)
                        .isEqualTo(List.of(feedApplicant3, feedApplicant2)),
                () -> assertThat(feedApplicantsSecondPage)
                        .isEqualTo(List.of(feedApplicant1))
        );
    }

    @Test
    void 피드에_대한_신청서_정보가_없으면_빈_값을_반환한다() {
        // given
        final Member member = 사용자를_저장한다("사이드", "test@example.com");
        final Member member2 = 사용자를_저장한다("페어", "test1@example.com");
        final FeedCategory category = 카테고리를_저장한다("이커머스");
        final Feed feed1 = 피드를_저장한다(member, category);
        final Feed feed2 = 피드를_저장한다(member2, category);

        final FeedApplicant feedApplicant = new FeedApplicant("신청서", member);
        feedApplicant.updateFeed(feed1);
        feedApplicantRepository.save(feedApplicant);

        // when
        final List<FeedApplicant> feedApplicantsFirstPage = feedApplicantRepository.findFeedApplicantWithMemberByFeedOrderByLatest(
                feed2, null, 1);

        // then
        assertThat(feedApplicantsFirstPage).isEmpty();
    }

    private Member 사용자를_저장한다(final String name, final String email) {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(new SkillName("CSS"))));
        final MemberImage memberImage = new MemberImage("test-name", "test-path", ImageContentType.PNG);
        final Member creator = new Member(new Email(email), new EncryptedPassword(new Password("password1!")),
                new Nickname(name), memberImage, memberProfile, skills);
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
        final FeedNode feedNode2 = new FeedNode("피드 2주차", "피드 2주차 내용");
        return List.of(feedNode1, feedNode2);
    }

    private FeedContent 피드_본문을_생성한다(final List<FeedNode> feedNodes) {
        final FeedContent feedContent = new FeedContent("피드 본문");
        feedContent.addNodes(new FeedNodes(feedNodes));
        return feedContent;
    }
}
