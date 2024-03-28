package sidepair.domain.feed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedContents;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.feed.FeedNodes;

class FeedContentsTest {

    @Test
    void 피드_본문을_추가한다() {
        // given
        final FeedNodes feedNodes = new FeedNodes(List.of(new FeedNode("피드 노드 제목", "피드 노드 내용")));
        final FeedContent feedContent = new FeedContent("피드 본문");
        feedContent.addNodes(feedNodes);
        final FeedContents feedContents = new FeedContents(List.of(feedContent));
        final FeedContent updatedFeedContent = new FeedContent("피드 본문 수정본");
        updatedFeedContent.addNodes(feedNodes);

        // when
        feedContents.add(updatedFeedContent);

        // then
        assertThat(feedContents.getValues()).usingRecursiveComparison()
                .isEqualTo(List.of(feedContent, updatedFeedContent));
    }
}