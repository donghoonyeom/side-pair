package sidepair.domain.project.vo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.domain.project.exeption.ProjectException;

class ProjectTodoContentTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20, 30, 40, 100, 200, 250})
    void 투두_컨텐츠의_길이가_1이상_250이하일_때_정상적으로_생성된다(final int length) {
        //given
        final String value = "a".repeat(length);

        //when
        //then
        assertDoesNotThrow((() -> new ProjectTodoContent(value)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 251, 252, 300})
    void 투두_컨텐츠의_길이가_1미만_250초과일_때_예외를_던진다(final int length) {
        //given
        final String value = "a".repeat(length);

        //when
        //then
        assertThatThrownBy(() -> new ProjectTodoContent(value))
                .isInstanceOf(ProjectException.class);
    }
}