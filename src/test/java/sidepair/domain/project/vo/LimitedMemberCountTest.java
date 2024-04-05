package sidepair.domain.project.vo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.domain.project.exeption.ProjectException;

class LimitedMemberCountTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5})
    void 제한_인원_수가_1이상_6이하일_때_정상적으로_생성된다(final int value) {
        //given
        //when
        //then
        assertDoesNotThrow(() -> new LimitedMemberCount(value));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7, 10, 100})
    void 제한_인원_수가_1미만_6초과일_때_예외를_던진다(final int value) {
        //given
        //when
        //then
        assertThatThrownBy(() -> new LimitedMemberCount(value))
                .isInstanceOf(ProjectException.class);
    }
}