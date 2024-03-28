package sidepair.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.feed.exception.FeedException;
import sidepair.member.domain.EncryptedPassword;
import sidepair.member.domain.Member;
import sidepair.member.domain.MemberProfile;
import sidepair.member.domain.MemberSkill;
import sidepair.member.domain.MemberSkills;
import sidepair.member.domain.Position;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;
import sidepair.member.domain.vo.SkillName;

class FeedTest {

    private final Member creator = 작성자를_생성한다();
    private final FeedCategory category = 카테고리를_생성한다();
    private final List<FeedNode> feedNodes = 피드_노드들을_생성한다();
    private final FeedContent feedContent = 피드_본문을_생성한다(feedNodes);

    @Test
    void 피드이_성공적으로_생성된다() {
        // expect
        assertDoesNotThrow(() -> new Feed("피드 제목", "피드 소개글", 30,
                creator, category));
    }

    @Test
    void 피드에_본문을_추가한다() {
        // given
        final Feed feed = new Feed("피드 제목", "피드 소개글", 30, creator, category);

        // when
        feed.addContent(feedContent);

        // then
        final FeedContents contents = feed.getContents();
        assertThat(contents.getValues()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 41})
    void 피드_제목의_길이가_1보다_작거나_40보다_크면_예외가_발생한다(final int titleLength) {
        // given
        final String title = "a".repeat(titleLength);

        // expect
        assertThatThrownBy(() -> new Feed(title, "피드 소개글", 30, creator, category))
                .isInstanceOf(FeedException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 151})
    void 피드_소개글의_길이가_1보다_작거나_150보다_크면_예외가_발생한다(final int introductionLength) {
        // given
        final String introduction = "a".repeat(introductionLength);

        // expect
        assertThatThrownBy(() -> new Feed("피드 제목", introduction, 30, creator, category))
                .isInstanceOf(FeedException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1001})
    void 피드_추천_소요_기간이_0보다_작고_1000보다_크면_예외가_발생한다(final int requiredPeriod) {
        // expect
        assertThatThrownBy(() -> new Feed("피드 제목", "피드 소개글", requiredPeriod, creator, category))
                .isInstanceOf(FeedException.class);
    }

    private Member 작성자를_생성한다() {
        final MemberProfile profile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(
                List.of(new MemberSkill(1L, new SkillName("Java"))));

        return new Member(new Email("test@test.com"), new EncryptedPassword(new Password("password1")),
                new Nickname("nickname"), null, profile, skills);
    }

    private FeedCategory 카테고리를_생성한다() {
        return new FeedCategory(1L, "헬스케어");
    }

    private List<FeedNode> 피드_노드들을_생성한다() {
        return List.of(new FeedNode("피드 1주차", "피드 1주차 내용"),
                new FeedNode("피드 2주차", "피드 2주차 내용"));
    }

    private FeedContent 피드_본문을_생성한다(final List<FeedNode> feedNodes) {
        final FeedContent feedContent = new FeedContent("피드 본문");
        feedContent.addNodes(new FeedNodes(feedNodes));
        return feedContent;
    }
}