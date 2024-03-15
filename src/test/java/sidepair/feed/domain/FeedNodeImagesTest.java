package sidepair.feed.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.feed.exception.FeedException;
import sidepair.global.domain.ImageContentType;

class FeedNodeImagesTest {

    @Test
    void 정상적으로_피드_노드_이미지들을_생성한다() {
        //given
        final FeedNodeImage feedNodeImage1 = new FeedNodeImage("originalFIleName1.png", "server/file/path1",
                ImageContentType.PNG);
        final FeedNodeImage feedNodeImage2 = new FeedNodeImage("originalFIleName2.png", "server/file/path2",
                ImageContentType.PNG);

        //when
        //then
        assertDoesNotThrow(() -> new FeedNodeImages(List.of(feedNodeImage1, feedNodeImage2)));
    }

    @Test
    void 피드_노드_이미지들을_생성할때_2장_이상이면_예외를_던진다() {
        //given
        final FeedNodeImage feedNodeImage1 = new FeedNodeImage("originalFIleName1.png", "server/file/path1",
                ImageContentType.PNG);
        final FeedNodeImage feedNodeImage2 = new FeedNodeImage("originalFIleName2.png", "server/file/path2",
                ImageContentType.PNG);
        final FeedNodeImage feedNodeImage3 = new FeedNodeImage("originalFIleName3.png", "server/file/path3",
                ImageContentType.PNG);

        //when
        //then
        assertThatThrownBy(
                () -> new FeedNodeImages(List.of(feedNodeImage1, feedNodeImage2, feedNodeImage3)))
                .isInstanceOf(FeedException.class);
    }

    @Test
    void 피드_노드_이미지들을_추가할_때_2장_이상이면_예외를_던진다() {
        //given
        final FeedNodeImage feedNodeImage1 = new FeedNodeImage("originalFIleName1.png", "server/file/path1",
                ImageContentType.PNG);
        final FeedNodeImage feedNodeImage2 = new FeedNodeImage("originalFIleName2.png", "server/file/path2",
                ImageContentType.PNG);
        final FeedNodeImage feedNodeImage3 = new FeedNodeImage("originalFIleName3.png", "server/file/path3",
                ImageContentType.PNG);
        final FeedNodeImages feedNodeImages = new FeedNodeImages(
                List.of(feedNodeImage1, feedNodeImage2));

        //when
        //then
        assertThatThrownBy(() -> feedNodeImages.add(feedNodeImage3))
                .isInstanceOf(FeedException.class);

    }

    @Test
    void 피드_노드_이미지들을_여러장_추가할_때_2장_이상이면_예외를_던진다() {
        //given
        final FeedNodeImage feedNodeImage1 = new FeedNodeImage("originalFIleName1.png", "server/file/path1",
                ImageContentType.PNG);
        final FeedNodeImage feedNodeImage2 = new FeedNodeImage("originalFIleName2.png", "server/file/path2",
                ImageContentType.PNG);
        final FeedNodeImage feedNodeImage3 = new FeedNodeImage("originalFIleName3.png", "server/file/path3",
                ImageContentType.PNG);
        final FeedNodeImage feedNodeImage4 = new FeedNodeImage("originalFIleName4.png", "server/file/path4",
                ImageContentType.PNG);
        final FeedNodeImages feedNodeImages = new FeedNodeImages(
                List.of(feedNodeImage1, feedNodeImage2));

        //when
        //then
        assertThatThrownBy(
                () -> feedNodeImages.addAll(new FeedNodeImages(List.of(feedNodeImage3, feedNodeImage4))))
                .isInstanceOf(FeedException.class);

    }
}