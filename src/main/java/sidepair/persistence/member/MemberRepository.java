package sidepair.persistence.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sidepair.domain.member.Member;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryRepository {

    Optional<Member> findByNickname(Nickname nickname);

    Optional<Member> findByEmail(final Email email);

    Optional<Member> findByOauthId(final String oauthId);
}
