package sidepair.persistence.member;

import java.util.Optional;
import sidepair.member.domain.Member;

public interface MemberQueryRepository {

    Optional<Member> findWithMemberProfileAndImageByEmail(final String email);

    Optional<Member> findWithMemberProfileAndImageById(final Long memberId);
}
