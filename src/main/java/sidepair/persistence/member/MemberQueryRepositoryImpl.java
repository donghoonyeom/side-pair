package sidepair.persistence.member;

import static sidepair.member.domain.QMember.member;
import static sidepair.member.domain.QMemberProfile.memberProfile;
import static sidepair.member.domain.vo.QMemberImage.memberImage;

import java.util.Optional;
import sidepair.member.domain.Member;
import sidepair.persistence.QuerydslRepositorySupporter;

public class MemberQueryRepositoryImpl extends QuerydslRepositorySupporter implements MemberQueryRepository {

    public MemberQueryRepositoryImpl() {
        super(Member.class);
    }

    @Override
    public Optional<Member> findWithMemberProfileAndImageByEmail(final String email) {
        return Optional.ofNullable(selectFrom(member)
                .innerJoin(member.memberProfile, memberProfile)
                .fetchJoin()
                .innerJoin(member.image, memberImage)
                .fetchJoin()
                .where(member.email.value.eq(email))
                .fetchOne());
    }

    @Override
    public Optional<Member> findWithMemberProfileAndImageById(final Long memberId) {
        return Optional.ofNullable(selectFrom(member)
                .innerJoin(member.memberProfile, memberProfile)
                .fetchJoin()
                .innerJoin(member.image, memberImage)
                .fetchJoin()
                .where(member.id.eq(memberId))
                .fetchOne());
    }
}
