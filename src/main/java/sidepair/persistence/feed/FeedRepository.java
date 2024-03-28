package sidepair.persistence.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import sidepair.domain.feed.Feed;

public interface FeedRepository extends JpaRepository<Feed, Long>, FeedQueryRepository {

}
