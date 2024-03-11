package sidepair.feed.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sidepair.feed.exception.FeedException;

class FeedNodeTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 41})
    void 피드_노드의_제목의_길이가_1보다_작거나_40보다_크면_예외가_발생한다(final int titleLength) {
        // given
        final String title = "a".repeat(titleLength);

        // expect
        assertThatThrownBy(() -> new FeedNode(title, "피드 설명"))
                .isInstanceOf(FeedException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2001})
    void 피드_노드의_설명의_길이가_1보다_작거나_2000보다_크면_예외가_발생한다(final int contentLength) {
        // given
        final String content = "a".repeat(contentLength);

        // expect
        assertThatThrownBy(() -> new FeedNode("피드 제목", content))
                .isInstanceOf(FeedException.class);
    }
}