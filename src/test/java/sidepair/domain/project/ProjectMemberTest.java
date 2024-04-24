package sidepair.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;

class ProjectMemberTest {

    @Test
    void 프로젝트의_리더이면_true를_반환한다() {
        // given
        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), null, null);

        // when
        final boolean result = projectMember.isLeader();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 프로젝트의_리더가_아니면_false를_반환한다() {
        // given
        final ProjectMember projectMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), null,
                null);

        // when
        final boolean result = projectMember.isLeader();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 입력받은_멤버가_자신과_같은_멤버이면_true를_반환한다() {
        // given
        final Member member = new Member(1L, new Email("test1@example.com"),
                null, new EncryptedPassword(new Password("password1!")),
                new Nickname("name1"), null, null, null);
        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), null,
                member);

        // when
        final boolean result = projectMember.isSameMember(member);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 입력받은_멤버가_자신과_다른_멤버이면_false를_반환한다() {
        // given
        final Member member1 = new Member(1L, new Email("test1@example.com"),
                null, new EncryptedPassword(new Password("password1!")),
                new Nickname("name1"), null, null, null);
        final Member member2 = new Member(2L, new Email("test2@example.com"),
                null, new EncryptedPassword(new Password("password2!")),
                new Nickname("name2"), null, null, null);

        final ProjectMember projectMember = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), null,
                member1);

        // when
        final boolean result = projectMember.isSameMember(member2);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 팔로워가_리더로_변경된다() {
        // given
        final ProjectMember projectMember = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), null,
                null);

        // when
        projectMember.becomeLeader();

        // then
        assertThat(projectMember.isLeader()).isTrue();
    }
}