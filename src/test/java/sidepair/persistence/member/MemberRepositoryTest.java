package sidepair.persistence.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sidepair.domain.ImageContentType;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.MemberProfile;
import sidepair.domain.member.MemberSkill;
import sidepair.domain.member.MemberSkills;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.MemberImage;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.domain.member.vo.SkillName;
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
        final MemberSkills skills = new MemberSkills(List.of(new MemberSkill(new SkillName("Java"))));
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
        final MemberSkills skills = findMember.getSkills();

        assertAll(
                () -> assertThat(findMember.getEmail().getValue()).isEqualTo("test@example.com"),
                () -> assertThat(memberProfile.getPosition()).isEqualTo(Position.BACKEND),
                () -> assertThat(skills.getValues().stream()
                        .map(MemberSkill::getName)
                        .map(SkillName::getValue)
                        .collect(Collectors.toList()))
                        .containsExactlyInAnyOrder("Java"),
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
