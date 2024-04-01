package sidepair.persistence.feed;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedContent;

public interface FeedContentRepository extends JpaRepository<FeedContent, Long> {
    Optional<FeedContent> findFirstByFeedOrderByCreatedAtDesc(final Feed feed);

    @Query("select rc from FeedContent rc "
            + "join fetch rc.feed r "
            + "where rc.id = :feedContentId")
    Optional<FeedContent> findByIdWithFeed(@Param("feedContentId") final Long feedContentId);
}
