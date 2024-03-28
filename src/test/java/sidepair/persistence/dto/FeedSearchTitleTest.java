package sidepair.persistence.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.service.exception.BadRequestException;

class FeedSearchTitleTest {

    @ParameterizedTest
    @CsvSource(value = {"공 백:공백", "공백 :공백", " 공백:공백"}, delimiter = ':')
    void 검색어에_공백이_들어가면_제거한다(final String title, final String removedBlankTitle) {
        // when
        final FeedSearchTitle searchTitle = assertDoesNotThrow(() -> new FeedSearchTitle(title));

        // then
        assertThat(searchTitle.value())
                .isEqualTo(removedBlankTitle);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void 검색어가_1자이상이면_정상적으로_생성된다(final int length) {
        // given
        final String title = "a".repeat(length);

        // expected
        assertDoesNotThrow(() -> new FeedSearchTitle(title));
    }

    @ParameterizedTest
    @ValueSource(ints = {0})
    void 검색어가_1자미만이면_예외가_발생한다(final int length) {
        // given
        final String title = "a".repeat(length);

        // expected
        assertThatThrownBy(() -> new FeedSearchTitle(title))
                .isInstanceOf(BadRequestException.class);
    }
}