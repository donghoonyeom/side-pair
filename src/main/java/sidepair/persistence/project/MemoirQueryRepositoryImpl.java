package sidepair.persistence.project;

import static sidepair.domain.member.QMember.member;
import static sidepair.domain.member.vo.QMemberImage.memberImage;
import static sidepair.domain.project.QMemoir.memoir;
import static sidepair.domain.project.QProjectMember.projectMember;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.List;
import sidepair.domain.project.Memoir;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectFeedNode;
import sidepair.persistence.QuerydslRepositorySupporter;

public class MemoirQueryRepositoryImpl extends QuerydslRepositorySupporter implements MemoirQueryRepository {

    public MemoirQueryRepositoryImpl() {
        super(Memoir.class);
    }

    @Override
    public List<Memoir> findByRunningProjectFeedNodeWithMemberAndMemberImage(
            final ProjectFeedNode projectFeedNode) {
        return selectFrom(memoir)
                .innerJoin(memoir.projectMember, projectMember)
                .fetchJoin()
                .innerJoin(projectMember.member, member)
                .fetchJoin()
                .innerJoin(member.image, memberImage)
                .fetchJoin()
                .where(nodeCond(projectFeedNode))
                .orderBy(memoir.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Memoir> findByRunningProjectFeedNode(
            final ProjectFeedNode currentProjectFeedNode) {
        return selectFrom(memoir)
                .innerJoin(memoir.projectMember, projectMember)
                .fetchJoin()
                .innerJoin(projectMember.member, member)
                .fetchJoin()
                .where(nodeCond(currentProjectFeedNode))
                .orderBy(memoir.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Memoir> findByProjectWithMemberAndMemberImage(final Project project) {
        return selectFrom(memoir)
                .innerJoin(memoir.projectMember, projectMember)
                .fetchJoin()
                .innerJoin(projectMember.member, member)
                .fetchJoin()
                .innerJoin(member.image, memberImage)
                .fetchJoin()
                .where(projectCond(project))
                .orderBy(memoir.createdAt.desc())
                .fetch();
    }

    private BooleanExpression nodeCond(final ProjectFeedNode node) {
        return memoir.projectFeedNode.eq(node);
    }

    private BooleanExpression projectCond(final Project project) {
        return projectMember.project.eq(project);
    }
}