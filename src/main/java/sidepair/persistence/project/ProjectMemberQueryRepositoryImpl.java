package sidepair.persistence.project;

import static sidepair.domain.feed.QFeedContent.feedContent;
import static sidepair.domain.member.QMember.member;
import static sidepair.domain.member.vo.QMemberImage.memberImage;
import static sidepair.domain.project.QProject.project;
import static sidepair.domain.project.QProjectMember.projectMember;
import static sidepair.persistence.project.dto.ProjectMemberSortType.JOINED_ASC;
import static sidepair.persistence.project.dto.ProjectMemberSortType.PARTICIPATION_RATE;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.List;
import java.util.Optional;
import sidepair.domain.member.vo.Email;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectStatus;
import sidepair.persistence.QuerydslRepositorySupporter;
import sidepair.persistence.project.dto.ProjectMemberSortType;

public class ProjectMemberQueryRepositoryImpl extends QuerydslRepositorySupporter implements
        ProjectMemberQueryRepository {

    public ProjectMemberQueryRepositoryImpl() {
        super(ProjectMember.class);
    }

    @Override
    public Optional<ProjectMember> findByFeedIdAndMemberEmailAndProjectStatus(final Long feedId,
                                                                              final Email email,
                                                                              final ProjectStatus status) {
        return Optional.ofNullable(selectFrom(projectMember)
                .innerJoin(projectMember.project, project)
                .fetchJoin()
                .innerJoin(project.feedContent, feedContent)
                .fetchJoin()
                .innerJoin(projectMember.member, member)
                .fetchJoin()
                .where(
                        project.feedContent.feed.id.eq(feedId),
                        member.email.eq(email),
                        project.status.eq(status))
                .fetchOne());
    }

    @Override
    public List<ProjectMember> findByProjectIdOrderedBySortType(final Long projectId,
                                                                final ProjectMemberSortType sortType) {
        return selectFrom(projectMember)
                .innerJoin(projectMember.member, member)
                .fetchJoin()
                .innerJoin(member.image, memberImage)
                .fetchJoin()
                .where(projectMember.project.id.eq(projectId))
                .orderBy(sortCond(sortType))
                .fetch();
    }

    private OrderSpecifier<?> sortCond(final ProjectMemberSortType sortType) {
        if (sortType == null  || sortType == PARTICIPATION_RATE) {
            return projectMember.participationRate.desc();
        }
        if (sortType == JOINED_ASC) {
            return projectMember.joinedAt.asc();
        }
        return projectMember.joinedAt.desc();
    }

    @Override
    public Optional<ProjectMember> findProjectMember(final Long projectId, final Email memberEmail) {
        return Optional.ofNullable(selectFrom(projectMember)
                .innerJoin(projectMember.project, project)
                .where(
                        projectIdCond(projectId),
                        memberEmailCond(memberEmail))
                .fetchJoin()
                .fetchFirst());
    }

    private BooleanExpression projectIdCond(final Long projectId) {
        return project.id.eq(projectId);
    }

    private BooleanExpression memberEmailCond(final Email memberEmail) {
        return projectMember.member.email.eq(memberEmail);
    }
}
