package sidepair.persistence.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryTest {

    private static final Long refreshTokenValidityInSeconds = 3600000L;

    private static Member member;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenRepositoryImpl refreshTokenRepository;

    @BeforeEach
    void init() {
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);
        refreshTokenRepository = new RefreshTokenRepositoryImpl(redisTemplate, refreshTokenValidityInSeconds);
    }


    @BeforeAll
    static void setUp() {
        final Email email = new Email("test@example.com");
        final Password password = new Password("password1!");
        final EncryptedPassword encryptedPassword = new EncryptedPassword(password);
        final Nickname nickname = new Nickname("nickname");
        final MemberSkills skills= new MemberSkills(
                List.of(new MemberSkill(1L, new SkillName("Java"))));
        final MemberProfile memberProfile = new MemberProfile(Position.BACKEND);
        member = new Member(email, encryptedPassword, nickname, null, memberProfile, skills);
    }

    @Test
    void 정상적으로_리프레시_토큰을_저장한다() {
        //given
        final String refreshToken = "refreshToken";
        final String memberEmail = member.getEmail().getValue();

        //when
        //then
        assertDoesNotThrow(() -> refreshTokenRepository.save(refreshToken, memberEmail));
    }

    @Test
    void 정상적으로_리프레시_토큰을_찾아온다() {
        //given
        final String refreshToken = "refreshToken";
        final String memberEmail = member.getEmail().getValue();

        when(valueOperations.get(refreshToken))
                .thenReturn(memberEmail);

        //when
        final String findMemberEmail = refreshTokenRepository.findMemberEmailByRefreshToken(refreshToken)
                .get();

        //then
        assertThat(findMemberEmail).isEqualTo(memberEmail);
    }

    @Test
    void 리프레시_토큰을_찾을때_없는경우_빈값을_보낸다() {
        //given
        final String refreshToken = "refreshToken";

        //when
        final Optional<String> memberEmail = refreshTokenRepository.findMemberEmailByRefreshToken(
                refreshToken);
        //then
        assertThat(memberEmail).isEmpty();
    }
}
