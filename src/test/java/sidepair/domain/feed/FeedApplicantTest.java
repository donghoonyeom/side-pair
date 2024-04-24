package sidepair.domain.feed;

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

class FeedApplicantTest {

    @Test
    void 신청서_내용이_1000자를_넘으면_예외가_발생한다() {
        // given
        final String content = "a".repeat(1001);
        final MemberProfile profile = new MemberProfile(Position.FRONTEND);
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("CSS"))));
        final Member member = new Member(new Email("test@example.com"),
                new EncryptedPassword(new Password("password1")),
                new Nickname("nickname"), null, profile, skills);

        // expected
        assertThatThrownBy(() -> new FeedApplicant(content, member))
                .isInstanceOf(FeedException.class);
    }
}