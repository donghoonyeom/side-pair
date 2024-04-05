package sidepair.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
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
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.ProjectName;

class ProjectPendingMemberTest {

    @Test
    void 프로젝트의_리더이면_True를_반환한다() {
        // given
        final Member member = new Member(new Email("test@example.com"),
                new EncryptedPassword(new Password("password1")),
                new Nickname("nickname"), null, new MemberProfile(Position.BACKEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java")))));
        final Project project = new Project(new ProjectName("project"), new LimitedMemberCount(6),
                new FeedContent("content"), member);

        // when
        final ProjectPendingMember projectPendingMember = new ProjectPendingMember(ProjectRole.LEADER, project,
                member);

        // then
        assertThat(projectPendingMember.isLeader()).isTrue();
    }

    @Test
    void 프로젝트의_리더가_아니면_false를_반환한다() {
        // given
        final Member member = new Member(new Email("test@example.com"),
                new EncryptedPassword(new Password("password1")),
                new Nickname("nickname"), null, new MemberProfile(Position.BACKEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java")))));
        final Project project = new Project(new ProjectName("project"), new LimitedMemberCount(6),
                new FeedContent("content"), member);

        // when
        final ProjectPendingMember projectPendingMember = new ProjectPendingMember(ProjectRole.FOLLOWER, project,
                member);

        // then
        assertThat(projectPendingMember.isLeader()).isFalse();
    }

    @Test
    void 입력받은_멤버가_자신과_같은_멤버이면_true를_반환한다() {
        // given
        final Member member = new Member(1L, new Email("test@example.com"),
                null, new EncryptedPassword(new Password("password1!")),
                new Nickname("name"), null, null, null);
        final ProjectPendingMember projectPendingMember = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), null,
                member);

        // when
        final boolean result = projectPendingMember.isSameMember(member);

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

        final ProjectPendingMember projectPendingMember = new ProjectPendingMember(ProjectRole.LEADER,
                LocalDateTime.now(), null,
                member1);

        // when
        final boolean result = projectPendingMember.isSameMember(member2);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 팔로워가_리더로_변경된다() {
        // given
        final ProjectPendingMember projectPendingMember = new ProjectPendingMember(ProjectRole.FOLLOWER,
                LocalDateTime.now(), null,
                null);

        // when
        projectPendingMember.becomeLeader();

        // then
        assertThat(projectPendingMember.isLeader()).isTrue();
    }
}