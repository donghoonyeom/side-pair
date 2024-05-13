package sidepair.persistence.member;

import java.util.Optional;
import sidepair.domain.feed.FeedApplicant;
import sidepair.domain.member.Member;

public interface MemberQueryRepository {

    Optional<Member> findWithMemberProfileAndImageByEmail(final String email);

    Optional<Member> findWithMemberProfileAndImageById(final Long memberId);

    Optional<Member> findWithMemberProfileAndImageByApplicant(final FeedApplicant applicant);
}
