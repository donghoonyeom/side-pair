package sidepair.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;

class ProjectMembersTest {

    private static final Member MEMBER1 = new Member(1L, new Email("test@email.com"),
            null, new EncryptedPassword(new Password("password123!")),
            new Nickname("닉네임"), null, null, null);

    private static final Member MEMBER2 = new Member(2L, new Email("test2@example.com"),
            null, new EncryptedPassword(new Password("password2!")),
            new Nickname("name2"), null, null, null);

    @Test
    void 입력받은_사용자를_프로젝트_사용자_중에서_찾는다() {
        // given
        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), null,
                MEMBER1);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), null,
                MEMBER2);

        final ProjectMembers projectMembers = new ProjectMembers(List.of(projectMember1, projectMember2));

        // when
        final ProjectMember findProjectMember = projectMembers.findByMember(MEMBER1).get();

        // then
        assertThat(findProjectMember).isEqualTo(projectMember1);
    }

    @Test
    void 다음_리더가_될_사용자를_찾는다() {
        // given
        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), null,
                MEMBER1);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), null,
                MEMBER2);

        final ProjectMembers projectMembers = new ProjectMembers(List.of(projectMember1, projectMember2));

        // when
        final ProjectMember nextLeader = projectMembers.findNextLeader().get();

        // then
        assertThat(nextLeader).isEqualTo(projectMember2);
    }

    @Test
    void 프로젝트_사용자의_수를_구한다() {
        // given
        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), null,
                MEMBER1);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), null,
                MEMBER2);

        final ProjectMembers projectMembers = new ProjectMembers(List.of(projectMember1, projectMember2));

        // when
        final int size = projectMembers.size();

        // then
        assertThat(size).isEqualTo(2);
    }

    @Test
    void 프로젝트_사용자에서_입렵받은_사용자를_제거한다() {
        // given
        final ProjectMember projectMember1 = new ProjectMember(ProjectRole.LEADER, LocalDateTime.now(), null,
                MEMBER1);
        final ProjectMember projectMember2 = new ProjectMember(ProjectRole.FOLLOWER, LocalDateTime.now(), null,
                MEMBER2);

        final ProjectMembers projectMembers = new ProjectMembers(List.of(projectMember1, projectMember2));

        // when
        projectMembers.remove(projectMember1);

        // then
        assertThat(projectMembers)
                .usingRecursiveComparison()
                .isEqualTo(new ProjectMembers(List.of(projectMember2)));
    }
}
