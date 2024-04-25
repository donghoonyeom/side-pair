package sidepair.persistence.project;

import static sidepair.domain.member.QMember.member;
import static sidepair.domain.member.vo.QMemberImage.memberImage;
import static sidepair.domain.project.QProjectPendingMember.projectPendingMember;
import static sidepair.persistence.project.dto.ProjectMemberSortType.JOINED_DESC;

import com.querydsl.core.types.OrderSpecifier;
import java.util.List;
import sidepair.domain.project.ProjectPendingMember;
import sidepair.persistence.QuerydslRepositorySupporter;
import sidepair.persistence.project.dto.ProjectMemberSortType;

public class ProjectPendingMemberQueryRepositoryImpl extends QuerydslRepositorySupporter
        implements ProjectPendingMemberQueryRepository {

    public ProjectPendingMemberQueryRepositoryImpl() {
        super(ProjectPendingMember.class);
    }

    @Override
    public List<ProjectPendingMember> findByProjectIdOrderedBySortType(final Long projectId,
                                                                       final ProjectMemberSortType sortType) {
        return selectFrom(projectPendingMember)
                .innerJoin(projectPendingMember.member, member)
                .fetchJoin()
                .innerJoin(member.image, memberImage)
                .fetchJoin()
                .where(projectPendingMember.project.id.eq(projectId))
                .orderBy(sortCond(sortType))
                .fetch();
    }

    private OrderSpecifier<?> sortCond(final ProjectMemberSortType sortType) {
        if (sortType == JOINED_DESC) {
            return projectPendingMember.joinedAt.desc();
        }
        return projectPendingMember.joinedAt.asc();
    }
}
