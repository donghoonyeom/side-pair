package sidepair.persistence.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sidepair.feed.domain.Feed;
import sidepair.feed.domain.FeedCategory;
import sidepair.feed.domain.FeedContent;
import sidepair.feed.domain.FeedNode;
import sidepair.feed.domain.FeedNodes;
import sidepair.feed.domain.FeedStatus;
import sidepair.feed.domain.FeedTag;
import sidepair.feed.domain.FeedTags;
import sidepair.feed.domain.vo.FeedTagName;
import sidepair.global.domain.ImageContentType;
import sidepair.member.domain.EncryptedPassword;
import sidepair.member.domain.Member;
import sidepair.member.domain.MemberProfile;
import sidepair.member.domain.MemberSkill;
import sidepair.member.domain.MemberSkills;
import sidepair.member.domain.Position;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.MemberImage;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;
import sidepair.member.domain.vo.SkillName;
import sidepair.persistence.dto.FeedOrderType;
import sidepair.persistence.dto.FeedSearchDto;
import sidepair.persistence.helper.RepositoryTest;
import sidepair.persistence.member.MemberRepository;

@RepositoryTest
class FeedRepositoryTest {
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final FeedCategoryRepository feedCategoryRepository;

    public FeedRepositoryTest(final MemberRepository memberRepository,
                                 final FeedRepository feedRepository,
                                 final FeedCategoryRepository feedCategoryRepository) {
        this.memberRepository = memberRepository;
        this.feedRepository = feedRepository;
        this.feedCategoryRepository = feedCategoryRepository;
    }

    @Test
    void 피드을_저장한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory category = 카테고리를_생성한다("헬스케어");
        final Feed feed = new Feed("피드 제목", "피드 소개글", 10, creator, category);

        // when
        final Feed savedFeed = feedRepository.save(feed);

        // then
        assertThat(savedFeed).usingRecursiveComparison()
                .isEqualTo(feed);
    }

    @Test
    void 단일_피드을_조회한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory category = 카테고리를_생성한다("헬스케어");
        final Feed savedFeed = 피드을_저장한다("피드 제목", creator, category);

        // when
        final Feed expectedFeed = feedRepository.findFeedById(savedFeed.getId()).get();

        assertThat(expectedFeed)
                .usingRecursiveComparison()
                .isEqualTo(savedFeed);
    }

    @Test
    void 카테고리_값이_null이라면_삭제되지_않은_전체_피드을_최신순으로_조회한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory gameCategory = 카테고리를_생성한다("게임");
        final FeedCategory travelCategory = 카테고리를_생성한다("이커머스");

        final Feed gameFeed = 피드을_저장한다("게임 피드", creator, gameCategory);
        final Feed gameFeed2 = 피드을_저장한다("게임 피드2", creator, gameCategory);
        final Feed travelFeed = 피드을_저장한다("이커머스 피드", creator, travelCategory);
        삭제된_피드을_저장한다("이커머스 피드2", creator, travelCategory);

        final FeedCategory category = null;
        final FeedOrderType orderType = FeedOrderType.LATEST;

        // when
        final List<Feed> firstFeedRequest = feedRepository.findFeedsByCategory(category, orderType,
                null, 2);

        // then
        assertAll(
                () -> assertThat(firstFeedRequest.size()).isEqualTo(3),
                () -> assertThat(firstFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(travelFeed, gameFeed2, gameFeed))
        );
    }

    @Test
    void 카테고리_값으로_1이상의_유효한_값이_들어오면_해당_카테고리의_삭제되지_않은_피드을_최신순으로_조회한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory gameCategory = 카테고리를_생성한다("게임");
        final FeedCategory travelCategory = 카테고리를_생성한다("이커머스");

        final Feed gameFeed = 피드을_저장한다("게임 피드", creator, gameCategory);
        final Feed gameFeed2 = 피드을_저장한다("게임 피드2", creator, gameCategory);
        삭제된_피드을_저장한다("게임 피드3", creator, gameCategory);
        삭제된_피드을_저장한다("게임 피드4", creator, travelCategory);

        final FeedOrderType orderType = FeedOrderType.LATEST;

        // when
        final List<Feed> firstFeedRequest = feedRepository.findFeedsByCategory(gameCategory, orderType,
                null, 10);

        // then
        assertAll(
                () -> assertThat(firstFeedRequest.size()).isEqualTo(2),
                () -> assertThat(firstFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(gameFeed2, gameFeed))
        );
    }

    @Test
    void 카테고리_조건_없이_주어진_피드_이전의_데이터를_최신순으로_조회한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory gameCategory = 카테고리를_생성한다("게임");
        final FeedCategory travelCategory = 카테고리를_생성한다("이커머스");

        final Feed gameFeed = 피드을_저장한다("게임 피드", creator, gameCategory);
        final Feed gameFeed2 = 피드을_저장한다("게임 피드2", creator, gameCategory);
        final Feed travelFeed = 피드을_저장한다("이커머스 피드", creator, travelCategory);
        삭제된_피드을_저장한다("이커머스 피드2", creator, travelCategory);

        final FeedCategory category = null;
        final FeedOrderType orderType = FeedOrderType.LATEST;

        // when
        final List<Feed> firstFeedRequest = feedRepository.findFeedsByCategory(category, orderType,
                null, 2);
        final List<Feed> secondFeedRequest = feedRepository.findFeedsByCategory(category, orderType,
                gameFeed2.getId(), 10);

        // then
        assertAll(
                () -> assertThat(firstFeedRequest.size()).isEqualTo(3),
                () -> assertThat(firstFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(travelFeed, gameFeed2, gameFeed)),

                () -> assertThat(secondFeedRequest.size()).isEqualTo(1),
                () -> assertThat(secondFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(gameFeed))
        );
    }

    @Test
    void 피드을_제목으로_검색한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory category = 카테고리를_생성한다("헬스케어");

        final Feed feed1 = 피드을_저장한다("피드", creator, category);
        final Feed feed2 = 피드을_저장한다("일피드", creator, category);
        final Feed feed3 = 피드을_저장한다(" 피드일", creator, category);
        final Feed feed4 = 피드을_저장한다("일피 드일", creator, category);
        피드을_저장한다("로드", creator, category);
        삭제된_피드을_저장한다("피드", creator, category);

        final FeedOrderType orderType = FeedOrderType.LATEST;
        final FeedSearchDto searchRequest = FeedSearchDto.create(null, " 피 드 ", null);

        // when
        final List<Feed> firstFeedRequest = feedRepository.findFeedsByCond(searchRequest, orderType,
                null, 2);
        final List<Feed> secondFeedRequest = feedRepository.findFeedsByCond(searchRequest, orderType,
                feed3.getId(), 3);

        // then
        assertAll(
                () -> assertThat(firstFeedRequest.size()).isEqualTo(3),
                () -> assertThat(firstFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(feed4, feed3, feed2)),

                () -> assertThat(secondFeedRequest.size()).isEqualTo(2),
                () -> assertThat(secondFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(feed2, feed1))
        );
    }

    @Test
    void 피드을_크리에이터_닉네임으로_검색한다() {
        // given
        final Member creator1 = 사용자를_생성한다("test@email.com", "페어");
        final Member creator2 = 사용자를_생성한다("test2@email.com", "페어2");
        final FeedCategory category = 카테고리를_생성한다("헬스케어");

        final Feed feed1 = 피드을_저장한다("피드", creator1, category);
        final Feed feed2 = 피드을_저장한다("피드", creator1, category);
        피드을_저장한다("피드", creator2, category);
        final Feed feed4 = 피드을_저장한다("피드", creator1, category);
        피드을_저장한다("피드", creator2, category);
        삭제된_피드을_저장한다("피드", creator1, category);

        final FeedOrderType orderType = FeedOrderType.LATEST;
        final FeedSearchDto searchRequest = FeedSearchDto.create(creator1.getNickname().getValue(), null, null);

        // when
        final List<Feed> firstFeedRequest = feedRepository.findFeedsByCond(searchRequest, orderType,
                null, 2);
        final List<Feed> secondFeedRequest = feedRepository.findFeedsByCond(searchRequest, orderType,
                feed2.getId(), 3);

        // then
        assertAll(
                () -> assertThat(firstFeedRequest.size()).isEqualTo(3),
                () -> assertThat(firstFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(feed4, feed2, feed1)),

                () -> assertThat(secondFeedRequest.size()).isEqualTo(1),
                () -> assertThat(secondFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(feed1))
        );
    }

    @Test
    void 피드을_태그_이름으로_검색한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory category = 카테고리를_생성한다("헬스케어");

        final Feed feed1 = 피드을_태그와_저장한다("피드", creator, category,
                new FeedTags(List.of(
                        new FeedTag(new FeedTagName("자바")),
                        new FeedTag(new FeedTagName("스프링")))));

        피드을_저장한다("피드", creator, category);

        final Feed feed3 = 피드을_태그와_저장한다("피드", creator, category,
                new FeedTags(List.of(
                        new FeedTag(new FeedTagName("자바")))));

        피드을_태그와_저장한다("피드", creator, category, new FeedTags(List.of(
                new FeedTag(new FeedTagName("스프링")))));

        final FeedOrderType orderType = FeedOrderType.LATEST;
        final FeedSearchDto searchRequest = FeedSearchDto.create(null, null, " 자 바 ");

        // when
        final List<Feed> firstFeedRequest = feedRepository.findFeedsByCond(searchRequest, orderType,
                null, 1);
        final List<Feed> secondFeedRequest = feedRepository.findFeedsByCond(searchRequest, orderType,
                feed3.getId(), 1);

        // then
        assertAll(
                () -> assertThat(firstFeedRequest.size()).isEqualTo(2),
                () -> assertThat(firstFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(feed3, feed1)),

                () -> assertThat(secondFeedRequest.size()).isEqualTo(1),
                () -> assertThat(secondFeedRequest).usingRecursiveComparison()
                        .ignoringFields("id", "createdAt", "updatedAt")
                        .isEqualTo(List.of(feed1))
        );
    }

    @Test
    void 사용자가_생성한_피드을_조회한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory gameCategory = 카테고리를_생성한다("게임");
        final FeedCategory travelCategory = 카테고리를_생성한다("이커머스");
        final FeedCategory itCategory = 카테고리를_생성한다("IT");

        final Feed gameFeed = 피드을_저장한다("피드1", creator, gameCategory);
        final Feed travelFeed = 피드을_저장한다("피드2", creator, travelCategory);
        final Feed deletedGameFeed = 삭제된_피드을_저장한다("피드3", creator, itCategory);

        feedRepository.saveAll(List.of(gameFeed, travelFeed, deletedGameFeed));

        // when
        final List<Feed> feedsFirstPage = feedRepository.findFeedsWithCategoryByMemberOrderByLatest(creator,
                null, 2);
        final List<Feed> feedsSecondPage = feedRepository.findFeedsWithCategoryByMemberOrderByLatest(
                creator, feedsFirstPage.get(1).getId(), 2);

        // then
        assertAll(
                () -> assertThat(feedsFirstPage)
                        .isEqualTo(List.of(deletedGameFeed, travelFeed, gameFeed)),
                () -> assertThat(feedsSecondPage)
                        .isEqualTo(List.of(gameFeed))
        );
    }

    @Test
    void 사용자_이메로_피드을_조회한다() {
        // given
        final Member creator1 = 사용자를_생성한다("test@email.com", "페어1");
        final FeedCategory gameCategory = 카테고리를_생성한다("게임");
        final FeedCategory travelCategory = 카테고리를_생성한다("이커머스");

        final Feed gameFeed1 = 피드을_저장한다("피드1", creator1, gameCategory);
        final Feed travelFeed = 피드을_저장한다("피드2", creator1, travelCategory);

        final Member creator2 = 사용자를_생성한다("test2@email.com", "페어2");
        피드을_저장한다("피드3", creator2, gameCategory);

        // when
        final Feed savedFeed1 = feedRepository.findByIdAndMemberEmail(gameFeed1.getId(), "test@email.com")
                .get();
        final Feed savedFeed2 = feedRepository.findByIdAndMemberEmail(travelFeed.getId(),
                "test@email.com").get();

        // then
        assertAll(
                () -> assertThat(savedFeed1).isEqualTo(gameFeed1),
                () -> assertThat(savedFeed2).isEqualTo(travelFeed)
        );
    }

    @Test
    void 사용자_이메일로_피드을_조회시_없으면_빈_값을_감싸서_반환한다() {
        // given
        final Member creator1 = 사용자를_생성한다("test@email.com", "페어");
        final FeedCategory gameCategory = 카테고리를_생성한다("게임");
        final FeedCategory travelCategory = 카테고리를_생성한다("이커머스");

        피드을_저장한다("피드1", creator1, gameCategory);
        final Feed travelFeed = 피드을_저장한다("피드2", creator1, travelCategory);

        final Member creator2 = 사용자를_생성한다("test2@email.com", "페어2");
        피드을_저장한다("피드3", creator2, gameCategory);

        // when
        final Optional<Feed> savedFeed = feedRepository.findByIdAndMemberEmail(travelFeed.getId() + 1,
                "test@email.com");

        // then
        assertThat(savedFeed).isEmpty();
    }

    @Test
    void 삭제된_피드을_피드_본문과_함께_조회한다() {
        // given
        final Member creator = 사용자를_생성한다("test2@email.com", "페어");
        final FeedCategory gameCategory = 카테고리를_생성한다("게임");
        final FeedCategory travelCategory = 카테고리를_생성한다("이커머스");

        피드을_저장한다("피드1", creator, gameCategory);
        피드을_저장한다("피드2", creator, travelCategory);
        final Feed deletedFeed = 삭제된_피드을_저장한다("피드", creator, travelCategory);

        // when
        final List<Feed> feedsByStatus = feedRepository.findWithFeedContentByStatus(FeedStatus.DELETED);

        // then
        assertAll(
                () -> assertThat(feedsByStatus).isEqualTo(List.of(deletedFeed)),
                () -> assertThat(feedsByStatus.get(0).getContents().getValues().get(0).getContent())
                        .isEqualTo("피드 본문2")
        );
    }

    private Member 사용자를_생성한다(final String email, final String nickname) {
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberImage memberImage = new MemberImage("file-name", "file-path", ImageContentType.PNG);
        final Member creator = new Member(new Email(email), new EncryptedPassword(new Password("password1!")),
                new Nickname(nickname), memberImage, memberProfile, skills);
        return memberRepository.save(creator);
    }

    private FeedCategory 카테고리를_생성한다(final String name) {
        final FeedCategory feedCategory = new FeedCategory(name);
        return feedCategoryRepository.save(feedCategory);
    }

    private Feed 피드을_저장한다(final String title, final Member creator, final FeedCategory category) {
        final Feed feed = new Feed(title, "피드 소개글", 10, creator, category);
        feed.addContent(new FeedContent("피드 본문"));
        return feedRepository.save(feed);
    }

    private Feed 삭제된_피드을_저장한다(final String title, final Member creator, final FeedCategory category) {
        final Feed feed = new Feed(title, "피드 소개글2", 7, creator, category);
        feed.addContent(new FeedContent("피드 본문2"));
        feed.delete();
        return feedRepository.save(feed);
    }

    private Feed 피드을_태그와_저장한다(final String title, final Member creator, final FeedCategory category,
                                  final FeedTags feedTags) {
        final Feed feed = new Feed(title, "피드 소개글", 10, creator, category);
        feed.addTags(feedTags);
        return feedRepository.save(feed);
    }

    private Feed 노드_정보를_포함한_피드을_생성한다(final String title, final Member creator, final FeedCategory category) {
        final Feed feed = new Feed(title, "피드 소개글", 10, creator, category);
        final FeedNode feedNode1 = 피드_노드를_생성한다("피드 1주차", "피드 1주차 내용");
        final FeedNode feedNode2 = 피드_노드를_생성한다("피드 2주차", "피드 2주차 내용");
        final FeedContent feedContent = 피드_본문을_생성한다(List.of(feedNode1, feedNode2));
        feed.addContent(feedContent);
        return feedRepository.save(feed);
    }

    private FeedNode 피드_노드를_생성한다(final String title, final String content) {
        return new FeedNode(title, content);
    }

    private FeedContent 피드_본문을_생성한다(final List<FeedNode> feedNodes) {
        final FeedContent feedContent = new FeedContent("피드 본문");
        feedContent.addNodes(new FeedNodes(feedNodes));
        return feedContent;
    }
}
