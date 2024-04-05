package sidepair.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.exception.UnexpectedDomainException;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.ProjectName;

class ProjectPendingMembersTest {

    private static final Member MEMBER1 = new Member(1L, new Email("test@email.com"),
            null, new EncryptedPassword(new Password("password123!")),
            new Nickname("닉네임1"), null, null, null);

    private static final Member MEMBER2 = new Member(2L, new Email("test2@example.com"),
            null, new EncryptedPassword(new Password("password2!")),
            new Nickname("닉네임2"), null, null, null);


    @Test
    void 프로젝트의_리더를_찾는다() {
        // given
        final Project project = new Project(new ProjectName("project"), new LimitedMemberCount(6),
                new FeedContent("content"), MEMBER1);

        // when
        final ProjectPendingMembers projectPendingMembers = new ProjectPendingMembers(List.of(
                new ProjectPendingMember(ProjectRole.LEADER, project, MEMBER2),
                new ProjectPendingMember(ProjectRole.FOLLOWER, project, MEMBER1)
        ));

        // then
        assertThat(projectPendingMembers.findProjectLeader()).isEqualTo(MEMBER2);
    }

    @Test
    void 프로젝트의_리더가_없으면_예외가_발생한다() {
        // given
        final Project project = new Project(new ProjectName("project"), new LimitedMemberCount(6),
                new FeedContent("content"), MEMBER1);

        // when
        final ProjectPendingMembers projectPendingMembers = new ProjectPendingMembers(List.of(
                new ProjectPendingMember(ProjectRole.FOLLOWER, project, MEMBER1),
                new ProjectPendingMember(ProjectRole.FOLLOWER, project, MEMBER2)
        ));

        // then
        assertThatThrownBy(() -> assertThat(projectPendingMembers.findProjectLeader()))
                .isInstanceOf(UnexpectedDomainException.class);
    }

    @Test
    void 입력받은_사용자를_프로젝트_사용자_중에서_찾는다() {
        // given
        final ProjectPendingMember projectPendingMember1 = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), null, MEMBER1);
        final ProjectPendingMember projectPendingMember2 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), null, MEMBER2);

        final ProjectPendingMembers projectPendingMembers = new ProjectPendingMembers(
                List.of(projectPendingMember1, projectPendingMember2));

        // when
        final ProjectPendingMember findProjectPendingMember = projectPendingMembers.findByMember(MEMBER1).get();

        // then
        assertThat(findProjectPendingMember).isEqualTo(projectPendingMember1);
    }

    @Test
    void 다음_리더가_될_사용자를_찾는다() {
        // given
        final ProjectPendingMember projectPendingMember1 = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), null,
                MEMBER1);
        final ProjectPendingMember projectPendingMember2 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), null,
                MEMBER2);

        final ProjectPendingMembers projectPendingMembers = new ProjectPendingMembers(
                List.of(projectPendingMember1, projectPendingMember2));

        // when
        final ProjectPendingMember nextLeader = projectPendingMembers.findNextLeader().get();

        // then
        assertThat(nextLeader).isEqualTo(projectPendingMember2);
    }

    @Test
    void 프로젝트_사용자의_수를_구한다() {
        // given
        final ProjectPendingMember projectPendingMember1 = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), null,
                MEMBER1);
        final ProjectPendingMember projectPendingMember2 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), null,
                MEMBER2);

        final ProjectPendingMembers projectPendingMembers = new ProjectPendingMembers(
                List.of(projectPendingMember1, projectPendingMember2));

        // when
        final int size = projectPendingMembers.size();

        // then
        assertThat(size).isEqualTo(2);
    }

    @Test
    void 프로젝트_사용자에서_입렵받은_사용자를_제거한다() {
        // given
        final ProjectPendingMember projectPendingMember1 = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), null,
                MEMBER1);
        final ProjectPendingMember projectPendingMember2 = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), null,
                MEMBER2);

        final ProjectPendingMembers projectPendingMembers = new ProjectPendingMembers(
                List.of(projectPendingMember1, projectPendingMember2));

        // when
        projectPendingMembers.remove(projectPendingMember1);

        // then
        assertThat(projectPendingMembers)
                .usingRecursiveComparison()
                .isEqualTo(new ProjectPendingMembers(List.of(projectPendingMember2)));
    }
}