package sidepair.persistence.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sidepair.member.domain.Member;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.Nickname;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryRepository {

    Optional<Member> findByNickname(Nickname nickname);

    Optional<Member> findByEmail(final Email email);

}
