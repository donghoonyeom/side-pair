package sidepair.persistence.feed;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedApplicant;
import sidepair.domain.member.Member;

public interface FeedApplicantRepository extends JpaRepository<FeedApplicant, Long>, FeedApplicantQueryRepository {
    Optional<FeedApplicant> findByFeedAndMember(final Feed feed, final Member member);
}
