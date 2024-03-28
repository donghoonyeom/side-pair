package sidepair.domain.member.vo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.domain.member.exception.MemberException;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"abc@naver.com", "dua541541@gmail.com"})
    void 정상적으로_이메일을_생성한다(final String email) {
        //given
        //when
        //then
        assertDoesNotThrow(() -> new Email(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "dua541541", "a bc", "abc @naver.com"})
    void 제약_조건에_맞지_않는_이메일일_경우_예외를_던진다(final String email) {
        //given
        //when
        //then
        assertThatThrownBy(() -> new Email(email))
                .isInstanceOf(MemberException.class);
    }
}
