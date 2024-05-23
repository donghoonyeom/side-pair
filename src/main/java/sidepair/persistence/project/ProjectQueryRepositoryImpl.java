package sidepair.persistence.project;

import static sidepair.domain.feed.QFeedContent.feedContent;
import static sidepair.domain.project.QProject.project;
import static sidepair.domain.project.QProjectFeedNode.projectFeedNode;
import static sidepair.domain.project.QProjectMember.projectMember;
import static sidepair.domain.project.QProjectPendingMember.projectPendingMember;
import static sidepair.domain.project.QProjectToDo.projectToDo;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import sidepair.domain.feed.Feed;
import sidepair.domain.member.Member;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectStatus;
import sidepair.persistence.QuerydslRepositorySupporter;

public class ProjectQueryRepositoryImpl extends QuerydslRepositorySupporter implements ProjectQueryRepository {

    public ProjectQueryRepositoryImpl() {
        super(Project.class);
    }

    @Override
    public Optional<Project> findProjectByFeedIdWithPessimisticLock(final Long feedId) {
        return Optional.ofNullable(selectFrom(project)
                .innerJoin(project.projectPendingMembers.values, projectPendingMember)
                .fetchJoin()
                .where(project.feedContent.feed.id.eq(feedId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }

    @Override
    public Optional<Project> findByIdWithFeedContent(final Long projectId) {
        return Optional.ofNullable(selectFrom(project)
                .innerJoin(project.feedContent, feedContent)
                .fetchJoin()
                .where(projectIdCond(projectId))
                .fetchFirst());
    }

    @Override
    public Optional<Project> findByIdWithContentAndTodos(final Long projectId) {
        return Optional.ofNullable(selectFrom(project)
                .innerJoin(project.feedContent, feedContent)
                .fetchJoin()
                .leftJoin(project.projectToDos.values, projectToDo)
                .fetchJoin()
                .where(projectIdCond(projectId))
                .fetchOne());
    }

    @Override
    public Optional<Project> findProjectByFeedAndCond(final Feed feed) {
        return Optional.ofNullable(selectFrom(project)
                .innerJoin(project.feedContent, feedContent)
                .where(feedCond(feed))
                .fetchOne());
    }

    @Override
    public Optional<Project> findByIdWithTodos(final Long projectId) {
        return Optional.ofNullable(selectFrom(project)
                .leftJoin(project.projectToDos.values, projectToDo)
                .fetchJoin()
                .where(projectIdCond(projectId))
                .fetchFirst());
    }

    @Override
    public List<Project> findByMember(final Member member) {
        return selectFrom(project)
                .leftJoin(project.projectPendingMembers.values, projectPendingMember)
                .leftJoin(project.projectMembers.values, projectMember)
                .where(projectPendingMember.member.eq(member)
                        .or(projectMember.member.eq(member)))
                .fetch();
    }

    @Override
    public List<Project> findByMemberAndStatus(final Member member, final ProjectStatus projectStatus) {
        return selectFrom(project)
                .leftJoin(project.projectPendingMembers.values, projectPendingMember)
                .leftJoin(project.projectMembers.values, projectMember)
                .where(projectPendingMember.member.eq(member)
                        .or(projectMember.member.eq(member)))
                .where(statusCond(projectStatus))
                .fetch();
    }

    @Override
    public Optional<Project> findByIdWithNodes(final Long projectId) {
        return Optional.ofNullable(selectFrom(project)
                .innerJoin(project.projectFeedNodes.values, projectFeedNode)
                .fetchJoin()
                .innerJoin(project.feedContent, feedContent)
                .fetchJoin()
                .where(projectIdCond(projectId))
                .fetchOne());
    }

    @Override
    public List<Project> findByFeed(final Feed feed) {
        return selectFrom(project)
                .innerJoin(project.feedContent, feedContent)
                .where(feedContent.feed.eq(feed))
                .fetch();
    }

    @Override
    public List<Project> findAllRecruitingProjectsByStartDateEarlierThan(final LocalDate date) {
        return selectFrom(project)
                .innerJoin(project.projectPendingMembers.values, projectPendingMember)
                .fetchJoin()
                .where(statusCond(ProjectStatus.RECRUITING))
                .where(equalOrEarlierStartDateThan(date))
                .fetch();
    }

    private BooleanExpression projectIdCond(final Long projectId) {
        return project.id.eq(projectId);
    }

    private BooleanExpression statusCond(final ProjectStatus status) {
        return project.status.eq(status);
    }


    private BooleanExpression feedCond(final Feed feed) {
        return project.feedContent.feed.eq(feed);
    }

    private BooleanExpression equalOrEarlierStartDateThan(final LocalDate date) {
        return project.startDate.loe(date);
    }
}
