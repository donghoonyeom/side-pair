    package sidepair.feed.domain;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.junit.jupiter.api.Assertions.*;

    import java.util.List;
    import org.junit.jupiter.api.Test;

    class FeedNodesTest {

        @Test
        void 피드_노드를_추가한다() {
            // given
            final FeedNodes feedNodes = new FeedNodes(List.of(new FeedNode("피드 노드 제목1", "피드 노드 내용")));

            // when
            feedNodes.add(new FeedNode("피드 노드 제목2", "피드 노드 내용"));

            // then
            assertThat(feedNodes.getValues()).hasSize(2);
        }

        @Test
        void 피드_노드들의_피드_본문을_업데이트한다() {
            // given
            final FeedNodes feedNodes = new FeedNodes(
                    List.of(new FeedNode("피드 노드 제목1", "피드 노드 내용1"), new FeedNode("피드 노드 제목2", "피드 노드 내용2")));
            final FeedContent feedContent = new FeedContent("피드 본문");

            // when
            feedNodes.updateAllFeedContent(feedContent);

            // then
            final List<FeedNode> nodes = feedNodes.getValues();
            assertAll(
                    () -> assertThat(nodes.get(0).getFeedContent()).isEqualTo(feedContent),
                    () -> assertThat(nodes.get(1).getFeedContent()).isEqualTo(feedContent)
            );
        }
    }