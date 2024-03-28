package sidepair.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.member.vo.SkillName;
import sidepair.domain.member.exception.MemberException;

class MemberSkillsTest {

    @Test
    void 기술의_수가_5개_이하면_정상적으로_생성된다() {
        // given
        final List<MemberSkill> values = List.of(
                new MemberSkill(new SkillName("자바")),
                new MemberSkill(new SkillName("스프링")),
                new MemberSkill(new SkillName("CSS")),
                new MemberSkill(new SkillName("HTML")),
                new MemberSkill(new SkillName("REDIS")));

        // when
        final MemberSkills memberSkills = assertDoesNotThrow(() -> new MemberSkills(values));

        // then
        assertThat(memberSkills)
                .isInstanceOf(MemberSkills.class);
    }

    @Test
    void 기술의_수가_5개_초과면_예외가_발생한다() {
        // given
        final List<MemberSkill> values = List.of(
                new MemberSkill(new SkillName("자바")),
                new MemberSkill(new SkillName("CSS")),
                new MemberSkill(new SkillName("AWS")),
                new MemberSkill(new SkillName("자바스크립트")),
                new MemberSkill(new SkillName("스프링")),
                new MemberSkill(new SkillName("HTML")));

        // expected
        assertThatThrownBy(() -> new MemberSkills(values))
                .isInstanceOf(MemberException.class);
    }

    @Test
    void 기술_이름에_중복이_있으면_예외가_발생한다() {
        // given
        final List<MemberSkill> values = List.of(
                new MemberSkill(new SkillName("자바")),
                new MemberSkill(new SkillName("자바")));

        // expected
        assertThatThrownBy(() -> new MemberSkills(values))
                .isInstanceOf(MemberException.class);
    }
}
