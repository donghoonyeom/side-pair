package sidepair.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.project.vo.Period;
import sidepair.domain.project.vo.ProjectTodoContent;

class ProjectToDosTest {

    @Test
    void 아이디로_투두를_조회한다() {
        // given
        final ProjectToDo firstTodo = new ProjectToDo(1L, new ProjectTodoContent("투두1"),
                new Period(LocalDate.now(), LocalDate.now().plusDays(3)));
        final ProjectToDo secondTodo = new ProjectToDo(2L, new ProjectTodoContent("투두2"),
                new Period(LocalDate.now(), LocalDate.now().plusDays(5)));

        final ProjectToDos projectToDos = new ProjectToDos(List.of(
                firstTodo, secondTodo
        ));

        // when
        final ProjectToDo findProjectTodo = projectToDos.findById(1L).get();

        // then
        assertThat(findProjectTodo)
                .isEqualTo(firstTodo);
    }

    @Test
    void 아이디로_투두_조회시_없으면_빈값을_반환한다() {
        // given
        final ProjectToDo firstTodo = new ProjectToDo(1L, new ProjectTodoContent("투두1"),
                new Period(LocalDate.now(), LocalDate.now().plusDays(3)));
        final ProjectToDo secondTodo = new ProjectToDo(2L, new ProjectTodoContent("투두2"),
                new Period(LocalDate.now(), LocalDate.now().plusDays(5)));

        final ProjectToDos projectToDos = new ProjectToDos(List.of(
                firstTodo, secondTodo
        ));

        // expected
        assertThat(projectToDos.findById(3L))
                .isEmpty();
    }
}