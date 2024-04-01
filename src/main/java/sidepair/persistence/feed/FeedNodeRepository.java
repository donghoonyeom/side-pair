package sidepair.persistence.feed;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedNode;

public interface FeedNodeRepository extends JpaRepository<FeedNode, Long> {

    List<FeedNode> findAllByFeedContent(final FeedContent feedContent);
}
