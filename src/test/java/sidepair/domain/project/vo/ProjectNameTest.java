package sidepair.domain.project.vo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.domain.project.exeption.ProjectException;

class ProjectNameTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20})
    void 프로젝트의_이름_길이가_1이상_20이하일_때_정상적으로_생성된다(final int length) {
        //given
        final String value = "a".repeat(length);

        //when
        //then
        assertDoesNotThrow((() -> new ProjectName(value)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 21, 30, 100})
    void 프로젝트의_이름_길이가_1미만_20초과일_때_예외를_던진다(final int length) {
        //given
        final String value = "a".repeat(length);

        //when
        //then
        assertThatThrownBy(() -> new ProjectName(value))
                .isInstanceOf(ProjectException.class);
    }
}
