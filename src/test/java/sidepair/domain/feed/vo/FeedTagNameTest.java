package sidepair.domain.feed.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.domain.feed.exception.FeedException;

class FeedTagNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"테스트", "안녕하세요!10글자"})
    void 피드_태그_이름이_1글자에서_10글자_사이면_정상_생성된다(final String name) {
        // when
        final FeedTagName feedTagName = assertDoesNotThrow(() -> new FeedTagName(name));

        // then
        assertThat(feedTagName)
                .isInstanceOf(FeedTagName.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 11})
    void 피드_태그_이름이_1글자_미만이거나_10글자_초과면_예외가_발생한다(final int length) {
        // given
        final String name = "a".repeat(length);

        // expected
        assertThatThrownBy(() -> new FeedTagName(name))
                .isInstanceOf(FeedException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"테 스트:테스트", "공 백:공백", " 공백:공백", "안녕하세요 :안녕하세요"}, delimiter = ':')
    void 피드_태그_이름에_공백이_들어오면_제거한다(final String name, final String removedSpaceValue) {
        // given
        final FeedTagName expected = new FeedTagName(removedSpaceValue);

        // when
        final FeedTagName feedTagName = assertDoesNotThrow(() -> new FeedTagName(name));

        // expected
        assertThat(feedTagName)
                .isEqualTo(expected);
    }
}
