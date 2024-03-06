package sidepair.persistence.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import sidepair.feed.domain.Feed;

public interface FeedRepository extends JpaRepository<Feed, Long>, FeedQueryRepository {

}
