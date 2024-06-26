package sidepair.persistence.member;


import static sidepair.domain.member.QMember.member;
import static sidepair.domain.member.QMemberProfile.memberProfile;
import static sidepair.domain.member.QMemberSkill.memberSkill;
import static sidepair.domain.member.vo.QMemberImage.memberImage;

import java.util.Optional;
import sidepair.domain.feed.FeedApplicant;
import sidepair.domain.member.Member;
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
                .leftJoin(member.skills.values, memberSkill)
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
                .leftJoin(member.skills.values, memberSkill)
                .where(member.id.eq(memberId))
                .fetchOne());
    }

    @Override
    public Optional<Member> findWithMemberProfileAndImageByApplicant(final FeedApplicant applicant) {
        return Optional.ofNullable(selectFrom(member)
                .innerJoin(member.memberProfile, memberProfile)
                .fetchJoin()
                .innerJoin(member.image, memberImage)
                .fetchJoin()
                .leftJoin(member.skills.values, memberSkill)
                .where(member.id.eq(applicant.getMember().getId()))
                .fetchOne());
    }
}
