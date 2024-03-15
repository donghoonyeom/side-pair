package sidepair.feed.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.feed.exception.FeedException;

class FeedCategoryTest {

    @ParameterizedTest
    @ValueSource(strings = {"이름", "카테고리", "0123456789", "       0123456789        "})
    void 정상적으로_피드_카테고리를_생성한다(final String name) {
        //given
        //when
        //then
        assertDoesNotThrow(() -> new FeedCategory(name));
    }

    @Test
    void 카테고리_생성시_공백이_들어올_경우_예외를_던진다() {
        //given
        final String space = "";

        //when
        //then
        assertThatThrownBy(() -> new FeedCategory(space))
                .isInstanceOf(FeedException.class);
    }

    @Test
    void 카테고리_생성시_10글자_초과일_경우_예외를_던진다() {
        //given
        final String space = "12345678901";

        //when
        //then
        assertThatThrownBy(() -> new FeedCategory(space))
                .isInstanceOf(FeedException.class);
    }
}