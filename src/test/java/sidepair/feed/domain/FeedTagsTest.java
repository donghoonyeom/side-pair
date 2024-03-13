package sidepair.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.feed.domain.vo.FeedTagName;
import sidepair.feed.exception.FeedException;

class FeedTagsTest {

    @Test
    void 피드_태그의_수가_5개_이하면_정상적으로_생성된다() {
        // given
        final List<FeedTag> values = List.of(
                new FeedTag(new FeedTagName("태그1")),
                new FeedTag(new FeedTagName("태그2")),
                new FeedTag(new FeedTagName("태그3")),
                new FeedTag(new FeedTagName("태그4")),
                new FeedTag(new FeedTagName("태그5")));

        // when
        final FeedTags feedTags = assertDoesNotThrow(() -> new FeedTags(values));

        // then
        assertThat(feedTags)
                .isInstanceOf(FeedTags.class);
    }

    @Test
    void 피드_태그의_수가_5개_초과면_예외가_발생한다() {
        // given
        final List<FeedTag> values = List.of(
                new FeedTag(new FeedTagName("태그1")),
                new FeedTag(new FeedTagName("태그2")),
                new FeedTag(new FeedTagName("태그3")),
                new FeedTag(new FeedTagName("태그4")),
                new FeedTag(new FeedTagName("태그5")),
                new FeedTag(new FeedTagName("태그6")));

        // expected
        assertThatThrownBy(() -> new FeedTags(values))
                .isInstanceOf(FeedException.class);
    }

    @Test
    void 피드_태그_이름에_중복이_있으면_예외가_발생한다() {
        // given
        final List<FeedTag> values = List.of(
                new FeedTag(new FeedTagName("태그1")),
                new FeedTag(new FeedTagName("태그1")));

        // expected
        assertThatThrownBy(() -> new FeedTags(values))
                .isInstanceOf(FeedException.class);
    }
}