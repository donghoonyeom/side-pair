package sidepair.domain.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.feed.exception.FeedException;
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

class FeedContentTest {

    @Test
    void 피드_본문의_길이가_2000보다_크면_예외가_발생한다() {
        // given
        final String content = "a".repeat(2001);

        // expect
        assertThatThrownBy(() -> new FeedContent(content))
                .isInstanceOf(FeedException.class);
    }

    @Test
    void 피드_본문은_null값을_허용한다() {
        // given
        final String content = null;

        // expect
        assertDoesNotThrow(() -> new FeedContent(content));
    }

    @Test
    void 피드_본문에_노드들을_추가한다() {
        // given
        final FeedContent content = new FeedContent("content");

        // when
        content.addNodes(
                new FeedNodes(
                        List.of(new FeedNode("title1", "content1"), new FeedNode("title2", "content1"))));

        // then
        final FeedNodes nodes = content.getNodes();
        assertAll(
                () -> assertThat(nodes.getValues()).hasSize(2),
                () -> assertThat(nodes.getValues().get(0).getFeedContent()).isEqualTo(content),
                () -> assertThat(nodes.getValues().get(1).getFeedContent()).isEqualTo(content)
        );
    }

    @Test
    void 피드_본문에_노드를_추가할때_이름이_겹치면_예외를_던진다() {
        // given
        final FeedContent content = new FeedContent("content");

        // when
        // then
        final String title = "title";
        assertThatThrownBy(() -> content.addNodes(
                new FeedNodes(
                        List.of(new FeedNode(title, "content1"), new FeedNode(title, "content1")))));
    }

    @Test
    void 피드_본문의_피드인_경우_false를_반환한다() {
        // given
        final FeedContent content = new FeedContent("content");
        final MemberProfile profile = new MemberProfile(Position.BACKEND);
        final MemberSkills skills = new MemberSkills(
                List.of(new MemberSkill(1L, new SkillName("Java"))));
        final Member creator = new Member(new Email("test@test.com"),
                new EncryptedPassword(new Password("password1")), new Nickname("nickname"), null, profile, skills);
        final FeedCategory category = new FeedCategory(1L, "여가");
        final Feed feed = new Feed("피드 제목", "피드 소개글", 30, creator, category);

        // when
        feed.addContent(content);

        // then
        assertThat(content.isNotSameFeed(feed)).isFalse();
    }
}