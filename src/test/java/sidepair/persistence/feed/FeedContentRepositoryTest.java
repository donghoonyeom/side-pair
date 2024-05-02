package sidepair.persistence.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.feed.FeedContent;
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
import sidepair.persistence.helper.RepositoryTest;
import sidepair.persistence.member.MemberRepository;

@RepositoryTest
class FeedContentRepositoryTest {

    private final MemberRepository memberRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final FeedRepository feedRepository;
    private final FeedContentRepository feedContentRepository;

    public FeedContentRepositoryTest(final MemberRepository memberRepository,
                                     final FeedCategoryRepository feedCategoryRepository,
                                     final FeedRepository feedRepository,
                                     final FeedContentRepository feedContentRepository) {
        this.memberRepository = memberRepository;
        this.feedCategoryRepository = feedCategoryRepository;
        this.feedRepository = feedRepository;
        this.feedContentRepository = feedContentRepository;
    }

    @Test
    void 피드_컨텐츠를_피드과_함께_조회한다() {
        // given
        final Feed feed = 피드를_생성한다();
        final Feed savedFeed = feedRepository.save(feed);
        final Long feedContentId = savedFeed.getContents().getValues().get(0).getId();

        // when
        final FeedContent feedContent = feedContentRepository.findByIdWithFeed(feedContentId).get();

        // then
        assertAll(
                () -> assertThat(feedContent).isEqualTo(savedFeed.getContents().getValues().get(0)),
                () -> assertThat(feedContent.getFeed()).isEqualTo(savedFeed)
        );
    }

    @Test
    void 피드의_가장_최근_컨텐츠를_조회한다() {
        // given
        final Feed savedFeed = feedRepository.save(피드를_생성한다());
        final FeedContent oldFeedContent = feedContentRepository.findFirstByFeedOrderByCreatedAtDesc(
                savedFeed).get();

        final FeedContent newFeedContent = new FeedContent("피드 제목");
        savedFeed.addContent(newFeedContent);

        // when
        final FeedContent expectedFeedContent = feedContentRepository.findFirstByFeedOrderByCreatedAtDesc(
                savedFeed).get();

        // then
        assertAll(
                () -> assertThat(oldFeedContent).isNotEqualTo(expectedFeedContent),
                () -> assertThat(expectedFeedContent).isEqualTo(newFeedContent)
        );
    }

    private Feed 피드를_생성한다() {
        final Member creator = 사용자를_생성한다();
        final FeedCategory category = 피드_카테고리를_생성한다();
        final FeedContent content = new FeedContent("피드 제목");

        final Feed feed = new Feed("피드 제목", "피드 설명", 100, creator, category);
        feed.addContent(content);

        return feed;
    }

    private Member 사용자를_생성한다() {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(
                List.of(new MemberSkill(1L, new SkillName("Java"))));
        final Member member = new Member(new Email("test@email.com"),
                new EncryptedPassword(new Password("password1!")), new Nickname("두두"), null, memberProfile, skills);

        return memberRepository.save(member);
    }

    private FeedCategory 피드_카테고리를_생성한다() {
        final FeedCategory category = new FeedCategory("게임");
        return feedCategoryRepository.save(category);
    }
}
