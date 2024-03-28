package sidepair.persistence.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import sidepair.persistence.helper.RepositoryTest;

@RepositoryTest
class MemberRepositoryTest {

    private static Member member;

    private final MemberRepository memberRepository;

    public MemberRepositoryTest(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @BeforeAll
    static void setUp() {
        final Email email = new Email("test@example.com");
        final Password password = new Password("password1!");
        final EncryptedPassword encryptedPassword = new EncryptedPassword(password);
        final Nickname nickname = new Nickname("nickname");
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        final MemberImage memberImage = new MemberImage("originalFileName", "serverFilePath", ImageContentType.PNG);
        member = new Member(email, encryptedPassword, nickname, memberImage, memberProfile, skills);
    }

    @Test
    void 이메일로_사용자를_찾는다() {
        //given
        memberRepository.save(member);

        //when
        final Optional<Member> optionalMember = memberRepository.findByEmail(new Email("test@example.com"));

        //then
        assertThat(optionalMember).isNotEmpty();
    }

    @Test
    void 사용쟈의_이메일로_사용자의_프로필과_이미지를_함께_조회한다() {
        // given
        final Member savedMember = memberRepository.save(member);

        // when
        final Member findMember = memberRepository.findWithMemberProfileAndImageByEmail(
                savedMember.getEmail().getValue()).get();

        // then
        final MemberProfile memberProfile = findMember.getMemberProfile();
        final MemberImage memberImage = findMember.getImage();

        assertAll(
                () -> assertThat(member.getEmail().getValue()).isEqualTo("test@example.com"),
                () -> assertThat(memberProfile.getPosition()).isEqualTo(Position.BACKEND),
                () -> assertThat(memberImage.getServerFilePath()).isEqualTo("serverFilePath")
        );
    }

    @Test
    void 식별자_아이디로_사용자의_프로필과_이미지를_함께_조회한다() {
        // given
        final Member savedMember = memberRepository.save(member);

        // when
        final Member findMember = memberRepository.findWithMemberProfileAndImageById(savedMember.getId()).get();

        // then
        final MemberProfile memberProfile = findMember.getMemberProfile();
        final MemberImage memberImage = findMember.getImage();

        assertAll(
                () -> assertThat(member.getEmail().getValue()).isEqualTo("test@example.com"),
                () -> assertThat(memberProfile.getPosition()).isEqualTo(Position.BACKEND),
                () -> assertThat(memberImage.getServerFilePath()).isEqualTo("serverFilePath")
        );
    }
}
