package sidepair.member.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import sidepair.member.domain.EncryptedPassword;
import sidepair.member.domain.vo.Password;

class EncryptedPasswordTest {

    @Test
    void 비밀번호를_정상적으로_암호화한다() {
        //given
        final Password password1 = new Password("password1!");
        final Password password2 = new Password("password2");

        //when
        final EncryptedPassword encryptedPassword1 = new EncryptedPassword(password1);
        final EncryptedPassword encryptedPassword2 = new EncryptedPassword(password2);

        //then
        assertThat(encryptedPassword1).isNotEqualTo(encryptedPassword2);
    }
}