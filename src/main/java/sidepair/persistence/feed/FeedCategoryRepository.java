package sidepair.persistence.feed;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sidepair.feed.domain.FeedCategory;

public interface FeedCategoryRepository extends JpaRepository<FeedCategory, Long> {
    Optional<FeedCategory> findByName(final String name);
}
